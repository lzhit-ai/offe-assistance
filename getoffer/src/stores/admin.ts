import { defineStore } from 'pinia'
import type { UserProfile } from '@/api/transformers'

const USER_STORAGE_KEY = 'user'

const readStoredUser = (): UserProfile | null => {
  const raw = localStorage.getItem(USER_STORAGE_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

interface UserState {
  user: UserProfile | null
  token: string
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: readStoredUser(),
    token: localStorage.getItem('token') || '',
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },
  actions: {
    login(token: string, user: UserProfile) {
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))
    },
    setUser(user: UserProfile) {
      this.user = user
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem(USER_STORAGE_KEY)
    },
  },
})
