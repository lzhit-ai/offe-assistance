import { defineStore } from 'pinia'

const USER_STORAGE_KEY = 'user'

type StoredUser = Record<string, unknown>

const readStoredUser = (): StoredUser | null => {
  const raw = localStorage.getItem(USER_STORAGE_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as StoredUser
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: readStoredUser() as StoredUser | null,
    token: localStorage.getItem('token') || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    login(token: string, user: StoredUser) {
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))
    },
    setUser(user: StoredUser) {
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
