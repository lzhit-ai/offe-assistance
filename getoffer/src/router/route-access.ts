export interface RouteAccessInput {
  fullPath?: string
  meta?: {
    requiresAuth?: boolean
    requiresAdmin?: boolean
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
  isAdmin = false,
): RouteAccessRedirect | null => {
  if (!isLoggedIn && to?.meta?.requiresAuth) {
    return {
      path: '/',
      query: {
        login: '1',
        redirect: to?.fullPath || '/',
      },
    }
  }

  if (to?.meta?.requiresAdmin && !isAdmin) {
    return {
      path: '/',
      query: {
        login: '1',
        redirect: to?.fullPath || '/',
      },
    }
  }

  return null
}
