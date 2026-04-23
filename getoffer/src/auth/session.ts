export const AUTH_EXPIRED_EVENT = 'auth-expired'

const STORAGE_KEYS = ['token', 'user']

type AuthExpiredDetail = {
  reason: string
} & Record<string, unknown>

type StorageLike = {
  removeItem?: (key: string) => void
}

type DispatchTarget = {
  dispatchEvent?: (event: Event) => unknown
}

const createAuthExpiredEvent = (detail: AuthExpiredDetail): Event => {
  if (typeof CustomEvent === 'function') {
    return new CustomEvent(AUTH_EXPIRED_EVENT, { detail })
  }

  return {
    type: AUTH_EXPIRED_EVENT,
    detail,
  } as unknown as Event
}

export const clearStoredSession = (storage: StorageLike = localStorage): void => {
  STORAGE_KEYS.forEach((key) => storage?.removeItem?.(key))
}

export const handleUnauthorizedSession = ({
  storage = localStorage,
  target = window,
  detail = { reason: 'unauthorized' },
}: {
  storage?: StorageLike
  target?: DispatchTarget
  detail?: AuthExpiredDetail
} = {}): void => {
  clearStoredSession(storage)
  target?.dispatchEvent?.(createAuthExpiredEvent(detail))
}
