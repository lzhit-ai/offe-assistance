import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const indexHtmlSource = readFileSync(resolve(process.cwd(), 'index.html'), 'utf8')
const faviconPath = resolve(process.cwd(), 'public/logo-favicon.png')

test('site favicon points to the custom logo asset', () => {
  assert.match(indexHtmlSource, /rel="icon"/)
  assert.match(indexHtmlSource, /href="\/logo-favicon\.png"/)
  assert.equal(existsSync(faviconPath), true)
})
