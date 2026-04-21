import test from 'node:test'
import assert from 'node:assert/strict'

import {
  ARTICLE_LIST_PAGE_SIZE,
  FAVORITES_PAGE_SIZE,
  PROFILE_ARTICLES_PAGE_SIZE,
  parsePage,
  resetPageInQuery,
  withPageInQuery,
} from '../src/utils/pagination.js'

test('pagination page sizes match the agreed UI rules', () => {
  assert.equal(ARTICLE_LIST_PAGE_SIZE, 5)
  assert.equal(FAVORITES_PAGE_SIZE, 5)
  assert.equal(PROFILE_ARTICLES_PAGE_SIZE, 6)
})

test('parsePage normalizes invalid route values to page 1', () => {
  assert.equal(parsePage(undefined), 1)
  assert.equal(parsePage(''), 1)
  assert.equal(parsePage('abc'), 1)
  assert.equal(parsePage('0'), 1)
  assert.equal(parsePage('-2'), 1)
  assert.equal(parsePage('3'), 3)
})

test('withPageInQuery writes the page only when it is greater than 1', () => {
  assert.deepEqual(
    withPageInQuery({ category: '前端', keyword: 'vue' }, 3),
    { category: '前端', keyword: 'vue', page: '3' },
  )

  assert.deepEqual(
    withPageInQuery({ category: '前端', page: '4' }, 1),
    { category: '前端' },
  )
})

test('resetPageInQuery removes stale page information while keeping filters', () => {
  assert.deepEqual(
    resetPageInQuery({ category: '前端', tag: 'Vue', page: '5' }),
    { category: '前端', tag: 'Vue' },
  )
})
