export const AUTH_EXPIRED_EVENT = 'auth-expired'

const STORAGE_KEYS = ['token', 'user'] as const

const createAuthExpiredEvent = (detail: Record<string, unknown>): Event => {
  if (typeof CustomEvent === 'function') {
    return new CustomEvent(AUTH_EXPIRED_EVENT, { detail })
  }

  const event = new Event(AUTH_EXPIRED_EVENT)
  return Object.assign(event, { detail })
}

export const clearStoredSession = (storage: Pick<Storage, 'removeItem'> = localStorage) => {
  STORAGE_KEYS.forEach((key) => storage?.removeItem?.(key))
}

export const handleUnauthorizedSession = ({
  storage = localStorage,
  target = window,
  detail = { reason: 'unauthorized' },
}: {
  storage?: Pick<Storage, 'removeItem'>
  target?: { dispatchEvent?: (event: Event) => boolean }
  detail?: Record<string, unknown>
} = {}) => {
  clearStoredSession(storage)
  target?.dispatchEvent?.(createAuthExpiredEvent(detail))
}
