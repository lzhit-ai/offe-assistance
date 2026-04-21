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

export function resolveRouteAccess(
  to: RouteAccessInput,
  isLoggedIn: boolean,
): RouteAccessRedirect | null
