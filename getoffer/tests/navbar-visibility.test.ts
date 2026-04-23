import test from 'node:test'
import assert from 'node:assert/strict'

import {
  NAVBAR_THRESHOLD,
  createNavbarVisibilityState,
  getNavbarPhase,
} from '../src/utils/navbar-visibility.ts'

test('navbar threshold remains at 240px', () => {
  assert.equal(NAVBAR_THRESHOLD, 240)
})

test('navbar starts as non-fixed and hidden for animated fixed mode', () => {
  assert.deepEqual(createNavbarVisibilityState(), {
    isFixed: false,
    isVisible: false,
  })
})

test('getNavbarPhase switches to fixed only after crossing the threshold', () => {
  assert.equal(getNavbarPhase(0), 'static')
  assert.equal(getNavbarPhase(240), 'static')
  assert.equal(getNavbarPhase(241), 'fixed')
  assert.equal(getNavbarPhase(600), 'fixed')
})
