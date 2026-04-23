import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const source = readFileSync(resolve(process.cwd(), 'src/views/admin/AdminUsers.vue'), 'utf8')

test('admin users view exposes permission adjustment actions', () => {
  assert.match(source, /设为管理员/)
  assert.match(source, /降为普通用户/)
  assert.match(source, /不可修改自己/)
  assert.match(source, /updateUserRole/)
})
