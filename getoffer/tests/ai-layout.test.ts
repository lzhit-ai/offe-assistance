import test from 'node:test'
import assert from 'node:assert/strict'

import {
  AI_PAGE_GUTTER,
  AI_PANEL_MIN_HEIGHT,
  AI_SHELL_HEIGHT,
} from '../src/views/ai-layout.ts'

test('ai layout tokens keep the page full-width and fixed-height', () => {
  assert.equal(AI_PAGE_GUTTER, 0)
  assert.equal(AI_PANEL_MIN_HEIGHT, 720)
  assert.equal(AI_SHELL_HEIGHT, 'calc(100vh - 92px)')
})
