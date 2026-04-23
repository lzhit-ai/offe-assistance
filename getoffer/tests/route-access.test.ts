import test from 'node:test'
import assert from 'node:assert/strict'

import { resolveRouteAccess } from '../src/router/route-access.ts'

test('resolveRouteAccess redirects guests away from protected routes', () => {
  const redirect = resolveRouteAccess(
    {
      fullPath: '/favorites?page=2',
      meta: {
        requiresAuth: true,
      },
    },
    false,
  )

  assert.deepEqual(redirect, {
    path: '/',
    query: {
      login: '1',
      redirect: '/favorites?page=2',
    },
  })
})

test('resolveRouteAccess allows logged-in users through protected routes', () => {
  const redirect = resolveRouteAccess(
    {
      fullPath: '/profile',
      meta: {
        requiresAuth: true,
      },
    },
    true,
  )

  assert.equal(redirect, null)
})

test('resolveRouteAccess ignores public routes', () => {
  const redirect = resolveRouteAccess(
    {
      fullPath: '/articles/tech',
      meta: {},
    },
    false,
  )

  assert.equal(redirect, null)
})

test('resolveRouteAccess redirects non-admin users away from admin routes', () => {
  const redirect = resolveRouteAccess(
    {
      fullPath: '/admin/dashboard',
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
      },
    },
    true,
    false,
  )

  assert.deepEqual(redirect, {
    path: '/',
    query: {
      login: '1',
      redirect: '/admin/dashboard',
    },
  })
})
