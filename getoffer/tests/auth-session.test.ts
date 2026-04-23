import test from 'node:test'
import assert from 'node:assert/strict'

import { AUTH_EXPIRED_EVENT, handleUnauthorizedSession } from '../src/auth/session.ts'

test('handleUnauthorizedSession clears stored credentials and emits an auth-expired event', () => {
  const removedKeys: string[] = []
  let dispatchedEvent: { type: string; detail?: Record<string, unknown> } | null = null

  const storage = {
    removeItem(key: string) {
      removedKeys.push(key)
    },
  }

  const target = {
    dispatchEvent(event: { type: string; detail?: Record<string, unknown> }) {
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
  assert.equal(dispatchedEvent?.type, AUTH_EXPIRED_EVENT)
  assert.deepEqual(dispatchedEvent?.detail, {
    reason: 'token-expired',
  })
})
