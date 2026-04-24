import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const articleCardSource = readFileSync(resolve(process.cwd(), 'src/components/ArticleCard.vue'), 'utf8')
const articleDetailSource = readFileSync(resolve(process.cwd(), 'src/views/ArticleDetail.vue'), 'utf8')
const frontendApiSource = readFileSync(resolve(process.cwd(), 'src/api/frontend.ts'), 'utf8')

test('frontend api exposes article comment endpoints', () => {
  assert.match(frontendApiSource, /\/api\/v1\/articles\/\$\{articleId\}\/comments/)
})

test('article card comment action jumps to article detail comments section', () => {
  assert.match(articleCardSource, /focus: 'comments'/)
  assert.match(articleCardSource, /ChatLineRound/)
})

test('article detail view contains comment section and comment submit entry', () => {
  assert.match(articleDetailSource, /评论区/)
  assert.match(articleDetailSource, /发表评论/)
  assert.match(articleDetailSource, /comment-section/)
})
