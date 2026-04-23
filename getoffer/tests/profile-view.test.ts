import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

test('profile view renders moderation status labels for personal articles', () => {
  const source = readFileSync(resolve(process.cwd(), 'src/views/Profile.vue'), 'utf8')

  assert.match(source, /待审核/)
  assert.match(source, /已通过/)
  assert.match(source, /已拒绝/)
})
