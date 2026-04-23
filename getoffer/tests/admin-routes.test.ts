import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync, existsSync } from 'node:fs'
import { resolve } from 'node:path'

const projectRoot = process.cwd()

test('router defines dedicated admin login and admin layout routes', () => {
  const source = readFileSync(resolve(projectRoot, 'src/router/index.ts'), 'utf8')

  assert.match(source, /\/admin\/login/)
  assert.match(source, /path:\s*'\/admin'/)
  assert.match(source, /requiresAdmin:\s*true/)
  assert.match(source, /adminUsers/)
  assert.match(source, /adminArticles/)
  assert.match(source, /adminAiSessions/)
})

test('admin view shell files exist', () => {
  assert.equal(existsSync(resolve(projectRoot, 'src/views/admin/AdminLogin.vue')), true)
  assert.equal(existsSync(resolve(projectRoot, 'src/views/admin/AdminLayout.vue')), true)
  assert.equal(existsSync(resolve(projectRoot, 'src/components/admin/AdminSidebar.vue')), true)
})
