import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync, existsSync } from 'node:fs'
import { resolve } from 'node:path'

const projectRoot = process.cwd()

test('frontend api exposes admin dashboard and moderation endpoints', () => {
  const source = readFileSync(resolve(projectRoot, 'src/api/frontend.ts'), 'utf8')

  assert.match(source, /adminApi/)
  assert.match(source, /\/api\/v1\/admin\/dashboard/)
  assert.match(source, /\/api\/v1\/admin\/users\/\$\{userId\}\/role/)
  assert.match(source, /\/api\/v1\/admin\/articles/)
  assert.match(source, /updateUserRole/)
  assert.match(source, /approveArticle/)
  assert.match(source, /rejectArticle/)
})

test('admin page files exist for dashboard users articles and ai sessions', () => {
  assert.equal(existsSync(resolve(projectRoot, 'src/views/admin/AdminDashboard.vue')), true)
  assert.equal(existsSync(resolve(projectRoot, 'src/views/admin/AdminUsers.vue')), true)
  assert.equal(existsSync(resolve(projectRoot, 'src/views/admin/AdminArticles.vue')), true)
  assert.equal(existsSync(resolve(projectRoot, 'src/views/admin/AdminAiSessions.vue')), true)
})
