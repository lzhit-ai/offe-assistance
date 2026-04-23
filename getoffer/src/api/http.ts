import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { handleUnauthorizedSession } from '@/auth/session'

interface ErrorPayload {
  message?: string
  error?: string
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorPayload>) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      '请求失败'

    if (error.response?.status === 401) {
      handleUnauthorizedSession({
        detail: {
          reason: 'unauthorized',
        },
      })
    }

    return Promise.reject(new Error(message))
  },
)

export default http
