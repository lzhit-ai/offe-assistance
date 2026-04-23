import test from 'node:test'
import assert from 'node:assert/strict'

import {
  buildArticleListQuery,
  mapArticle,
  mapPageResult,
  mapUserProfile,
} from '../src/api/transformers.ts'

test('mapArticle normalizes backend article fields for the current UI', () => {
  const mapped = mapArticle({
    id: 7,
    title: 'Spring Security JWT 实战',
    author: {
      id: 3,
      username: 'alice',
    },
    category: '后端',
    type: 1,
    status: 'PENDING',
    tags: ['Java', 'Spring Security'],
    createdAt: '2026-04-20T10:30:00',
    updatedAt: '2026-04-20T12:00:00',
    viewCount: 45,
    favoriteCount: 9,
    commentCount: 2,
    isFavorited: true,
    content: '# hello',
    canEdit: true,
  })

  assert.deepEqual(mapped, {
    id: 7,
    title: 'Spring Security JWT 实战',
    author: 'alice',
    authorInfo: {
      id: 3,
      username: 'alice',
    },
    category: '后端',
    type: 1,
    status: 'PENDING',
    tags: ['Java', 'Spring Security'],
    createdAt: '2026-04-20',
    updatedAt: '2026-04-20',
    viewCount: 45,
    favoriteCount: 9,
    commentCount: 2,
    isFavorited: true,
    content: '# hello',
    canEdit: true,
  })
})

test('mapPageResult maps paged backend results and preserves pagination info', () => {
  const result = mapPageResult(
    {
      list: [
        {
          id: 1,
          title: 'Vue3',
          author: { id: 1, username: 'bob' },
          category: '前端',
          type: 1,
          status: 'APPROVED',
          tags: ['Vue3'],
          createdAt: '2026-04-20T09:00:00',
          viewCount: 10,
          favoriteCount: 1,
          commentCount: 0,
          isFavorited: false,
        },
      ],
      page: 1,
      pageSize: 10,
      total: 1,
      hasMore: false,
    },
    mapArticle,
  )

  assert.equal(result.list.length, 1)
  assert.equal(result.list[0].author, 'bob')
  assert.equal(result.total, 1)
  assert.equal(result.hasMore, false)
})

test('mapUserProfile formats register time for profile page consumption', () => {
  const profile = mapUserProfile({
    id: 5,
    username: 'carol',
    phone: '13800138000',
    email: 'carol@example.com',
    avatar: '',
    registerTime: '2026-04-19T08:00:00',
    stats: {
      articleCount: 3,
      favoriteCount: 4,
      likeCount: 0,
    },
  })

  assert.equal(profile.registerTime, '2026-04-19')
  assert.equal(profile.stats.articleCount, 3)
})

test('mapUserProfile preserves role for admin routing', () => {
  const profile = mapUserProfile({
    username: 'root',
    role: 'ADMIN',
    registerTime: '2026-04-23T08:00:00',
  })

  assert.equal(profile.role, 'ADMIN')
})

test('buildArticleListQuery omits empty filters and maps article type route values', () => {
  assert.deepEqual(
    buildArticleListQuery({
      page: 2,
      pageSize: 5,
      type: 'interview',
      category: '后端',
      tag: '',
      keyword: 'jwt',
      sort: 'hot',
    }),
    {
      page: 2,
      pageSize: 5,
      type: 2,
      category: '后端',
      keyword: 'jwt',
      sort: 'hot',
    },
  )
})
