export const AUTH_EXPIRED_EVENT = 'auth-expired'

const STORAGE_KEYS = ['token', 'user']

const createAuthExpiredEvent = (detail) => {
  if (typeof CustomEvent === 'function') {
    return new CustomEvent(AUTH_EXPIRED_EVENT, { detail })
  }

  return {
    type: AUTH_EXPIRED_EVENT,
    detail,
  }
}

export const clearStoredSession = (storage = localStorage) => {
  STORAGE_KEYS.forEach((key) => storage?.removeItem?.(key))
}

export const handleUnauthorizedSession = ({
  storage = localStorage,
  target = window,
  detail = { reason: 'unauthorized' },
} = {}) => {
  clearStoredSession(storage)
  target?.dispatchEvent?.(createAuthExpiredEvent(detail))
}
