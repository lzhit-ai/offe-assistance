import test from 'node:test'
import assert from 'node:assert/strict'

import { AUTH_EXPIRED_EVENT, handleUnauthorizedSession } from '../src/auth/session.js'

test('handleUnauthorizedSession clears stored credentials and emits an auth-expired event', () => {
  const removedKeys = []
  let dispatchedEvent = null

  const storage = {
    removeItem(key) {
      removedKeys.push(key)
    },
  }

  const target = {
    dispatchEvent(event) {
      dispatchedEvent = event
      return true
    },
  }

  handleUnauthorizedSession({
    storage,
    target,
    detail: {
      reason: 'token-expired',
    },
  })

  assert.deepEqual(removedKeys, ['token', 'user'])
  assert.equal(dispatchedEvent.type, AUTH_EXPIRED_EVENT)
  assert.deepEqual(dispatchedEvent.detail, {
    reason: 'token-expired',
  })
})
