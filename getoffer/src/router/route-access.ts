export interface RouteAccessInput {
  fullPath?: string
  meta?: {
    requiresAuth?: boolean
  }
}

export interface RouteAccessRedirect {
  path: string
  query: {
    login: string
    redirect: string
  }
}

export const resolveRouteAccess = (
  to: RouteAccessInput,
  isLoggedIn: boolean,
): RouteAccessRedirect | null => {
  if (isLoggedIn || !to?.meta?.requiresAuth) {
    return null
  }

  return {
    path: '/',
    query: {
      login: '1',
      redirect: to?.fullPath || '/',
    },
  }
}
