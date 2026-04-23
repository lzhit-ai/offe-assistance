import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync } from 'node:fs'
import { resolve } from 'node:path'

const projectRoot = process.cwd()

test('source modules migrated from js to ts keep TypeScript entry files', () => {
  const migratedFiles = [
    ['src/stores/admin.ts', 'src/stores/admin.js'],
    ['src/api/http.ts', 'src/api/http.js'],
    ['src/api/frontend.ts', 'src/api/frontend.js'],
  ]

  for (const [tsPath, jsPath] of migratedFiles) {
    assert.equal(existsSync(resolve(projectRoot, tsPath)), true, `${tsPath} should exist`)
    assert.equal(existsSync(resolve(projectRoot, jsPath)), false, `${jsPath} should be removed`)
  }
})
