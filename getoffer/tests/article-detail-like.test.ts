import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const articleDetailSource = readFileSync(resolve(process.cwd(), 'src/views/ArticleDetail.vue'), 'utf8')
const frontendApiSource = readFileSync(resolve(process.cwd(), 'src/api/frontend.ts'), 'utf8')

test('frontend api exposes article like endpoints', () => {
  assert.match(frontendApiSource, /\/api\/v1\/articles\/\$\{articleId\}\/like/)
  assert.match(frontendApiSource, /likeApi/)
  assert.match(frontendApiSource, /async add\(articleId/)
  assert.match(frontendApiSource, /async remove\(articleId/)
})

test('article detail view exposes like button states without self-like restriction', () => {
  assert.match(articleDetailSource, /点赞/)
  assert.match(articleDetailSource, /取消点赞/)
  assert.match(articleDetailSource, /toggleLike/)
  assert.doesNotMatch(articleDetailSource, /不能给自己的文章点赞/)
  assert.doesNotMatch(articleDetailSource, /:disabled="isAuthor"/)
})
