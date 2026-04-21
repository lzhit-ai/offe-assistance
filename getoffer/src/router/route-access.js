export const resolveRouteAccess = (to, isLoggedIn) => {
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
