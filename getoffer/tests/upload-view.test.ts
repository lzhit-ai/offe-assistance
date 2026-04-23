import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

test('upload view tells the user articles require admin approval after submission', () => {
  const source = readFileSync(resolve(process.cwd(), 'src/views/Upload.vue'), 'utf8')

  assert.match(source, /已提交审核|提交审核/)
  assert.match(source, /后台审核/)
})
