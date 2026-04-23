import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync } from 'node:fs'
import { resolve } from 'node:path'

const projectRoot = process.cwd()

const migratedModules = [
  'src/api/ai-stream',
  'src/api/frontend',
  'src/api/http',
  'src/api/transformers',
  'src/auth/session',
  'src/features/availability',
  'src/router/route-access',
  'src/stores/admin',
  'src/utils/navbar-visibility',
  'src/utils/pagination',
  'src/utils/user-profile',
  'src/views/ai-layout',
]

test('frontend support modules are implemented in TypeScript instead of JavaScript', () => {
  for (const modulePath of migratedModules) {
    assert.equal(existsSync(resolve(projectRoot, `${modulePath}.ts`)), true, `${modulePath}.ts should exist`)
    assert.equal(existsSync(resolve(projectRoot, `${modulePath}.js`)), false, `${modulePath}.js should be removed`)
  }
})
