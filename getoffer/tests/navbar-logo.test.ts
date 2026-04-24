import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const navbarSource = readFileSync(resolve(process.cwd(), 'src/components/Navbar.vue'), 'utf8')

test('navbar brand shows the custom logo before the title text', () => {
  assert.match(navbarSource, /brand-logo/)
  assert.match(navbarSource, /src="\/logo-favicon\.png"/)
  assert.match(navbarSource, /brand-text/)
})
