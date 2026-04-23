import type { AxiosRequestConfig } from 'axios'
import http from './http'
import {
  buildArticleListQuery,
  mapAdminAiSession,
  mapAdminArticle,
  mapAdminDashboard,
  mapAdminUser,
  mapAiMessage,
  mapAiSession,
  mapArticle,
  mapPageResult,
  mapUserProfile,
  type AdminAiSessionItem,
  type AdminArticleItem,
  type AdminDashboard,
  type AdminUserItem,
  type AiMessageItem,
  type AiSessionItem,
  type ArticleItem,
  type MetadataItem,
  type PageResult,
  type UserProfile,
} from './transformers'

interface ApiEnvelope<T> {
  code: number
  message?: string
  data: T
}

interface AuthPayload {
  accessToken: string
  user: UserProfile
}

interface ArticleDraft {
  id?: number
  title: string
  category: string
  type: number
  tags: string[]
  content: string
}

interface ListParams {
  page?: number
  pageSize?: number
}

interface AdminArticleQuery extends ListParams {
  keyword?: string
  category?: string
  status?: string
}

const request = async <T>(config: AxiosRequestConfig): Promise<T> => {
  const response = await http<ApiEnvelope<T>>(config)
  const payload = response.data

  if (payload?.code !== 0) {
    throw new Error(payload?.message || '请求失败')
  }

  return payload.data
}

const pickArticlePayload = (data: Partial<ArticleDraft> = {}) => ({
  title: data.title,
  category: data.category,
  type: data.type,
  tags: Array.isArray(data.tags) ? data.tags : [],
  content: data.content,
})

const toPagedResult = <TInput, TOutput>(
  payload: unknown,
  mapper: (item: TInput) => TOutput,
): { data: TOutput[]; pagination: PageResult<TOutput> } => {
  const page = mapPageResult(payload as PageResult<TInput>, mapper)
  return {
    data: page.list,
    pagination: page,
  }
}

export const authApi = {
  async register(data: Record<string, unknown>) {
    const payload = await request<AuthPayload>({
      url: '/api/v1/auth/register',
      method: 'post',
      data,
    })

    return {
      data: {
        accessToken: payload.accessToken,
        user: mapUserProfile(payload.user),
      },
    }
  },

  async login(data: Record<string, unknown>) {
    const payload = await request<AuthPayload>({
      url: '/api/v1/auth/login',
      method: 'post',
      data,
    })

    return {
      data: {
        accessToken: payload.accessToken,
        user: mapUserProfile(payload.user),
      },
    }
  },

  async me() {
    const payload = await request<UserProfile>({
      url: '/api/v1/auth/me',
      method: 'get',
    })

    return {
      data: mapUserProfile(payload),
    }
  },

  async logout() {
    return request<Record<string, unknown>>({
      url: '/api/v1/auth/logout',
      method: 'post',
    })
  },
}

export const userApi = {
  async updateProfile(data: { nickname: string }) {
    const payload = await request<UserProfile>({
      url: '/api/v1/users/me/profile',
      method: 'patch',
      data,
    })

    return {
      data: mapUserProfile(payload),
    }
  },

  async uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)

    const payload = await request<{ avatar: string }>({
      url: '/api/v1/users/me/avatar',
      method: 'post',
      data: formData,
    })

    return {
      data: payload,
    }
  },
}

export const articleApi = {
  async getHot(params: { limit?: number; type?: string | number } = {}) {
    const payload = await request<ArticleItem[]>({
      url: '/api/v1/articles/hot',
      method: 'get',
      params: {
        limit: params.limit || 5,
        ...(params.type ? { type: params.type } : {}),
      },
    })

    return {
      data: Array.isArray(payload) ? payload.map(mapArticle) : [],
    }
  },

  async getList(
    params: ListParams & {
      type?: string | number
      category?: string
      tag?: string
      keyword?: string
      authorId?: number
      sort?: string
    } = {},
  ) {
    const payload = await request<PageResult<ArticleItem>>({
      url: '/api/v1/articles',
      method: 'get',
      params: buildArticleListQuery(params),
    })

    return toPagedResult(payload, mapArticle)
  },

  async getDetail(id?: string | number | null) {
    const payload = await request<ArticleItem>({
      url: `/api/v1/articles/${id}`,
      method: 'get',
    })

    return {
      data: mapArticle(payload),
    }
  },

  async create(data: ArticleDraft) {
    const payload = await request<ArticleItem>({
      url: '/api/v1/articles',
      method: 'post',
      data: pickArticlePayload(data),
    })

    return {
      data: mapArticle(payload),
    }
  },

  async update(articleId: number | string | undefined, data: Partial<ArticleDraft>) {
    const id = articleId || data?.id
    const payload = await request<ArticleItem>({
      url: `/api/v1/articles/${id}`,
      method: 'put',
      data: pickArticlePayload(data),
    })

    return {
      data: mapArticle(payload),
    }
  },

  async getMine(params: { page?: number; pageSize?: number; type?: string | number } = {}) {
    const payload = await request<PageResult<ArticleItem>>({
      url: '/api/v1/users/me/articles',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 20,
        ...(params.type ? { type: params.type } : {}),
      },
    })

    return toPagedResult(payload, mapArticle)
  },
}

export const favoriteApi = {
  async add(articleId: number) {
    const payload = await request<{ favoriteCount?: number }>({
      url: `/api/v1/articles/${articleId}/favorite`,
      method: 'post',
    })

    return { data: payload }
  },

  async remove(articleId: number) {
    const payload = await request<{ favoriteCount?: number }>({
      url: `/api/v1/articles/${articleId}/favorite`,
      method: 'delete',
    })

    return { data: payload }
  },

  async getFavorites(params: ListParams = {}) {
    const payload = await request<PageResult<ArticleItem>>({
      url: '/api/v1/users/me/favorites',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 20,
      },
    })

    return toPagedResult(payload, mapArticle)
  },
}

export const metadataApi = {
  async getCategories() {
    const payload = await request<MetadataItem[]>({
      url: '/api/v1/metadata/categories',
      method: 'get',
    })

    return {
      data: Array.isArray(payload) ? payload : [],
    }
  },

  async getHotTags(limit = 10) {
    const payload = await request<MetadataItem[]>({
      url: '/api/v1/metadata/tags/hot',
      method: 'get',
      params: { limit },
    })

    return {
      data: Array.isArray(payload) ? payload : [],
    }
  },
}

export const aiApi = {
  async getSessions(params: ListParams = {}) {
    const payload = await request<PageResult<AiSessionItem>>({
      url: '/api/v1/ai/sessions',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 20,
      },
    })

    return toPagedResult(payload, mapAiSession)
  },

  async createSession(data: { title?: string } = {}) {
    const payload = await request<AiSessionItem>({
      url: '/api/v1/ai/sessions',
      method: 'post',
      data: {
        title: data.title || '',
      },
    })

    return {
      data: mapAiSession(payload),
    }
  },

  async getMessages(sessionId: number, params: ListParams = {}) {
    const payload = await request<PageResult<AiMessageItem>>({
      url: `/api/v1/ai/sessions/${sessionId}/messages`,
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 100,
      },
    })

    return toPagedResult(payload, mapAiMessage)
  },

  async deleteSession(sessionId: number) {
    const payload = await request<Record<string, unknown>>({
      url: `/api/v1/ai/sessions/${sessionId}`,
      method: 'delete',
    })

    return { data: payload }
  },
}

export const adminApi = {
  async getDashboard() {
    const payload = await request<AdminDashboard>({
      url: '/api/v1/admin/dashboard',
      method: 'get',
    })

    return {
      data: mapAdminDashboard(payload),
    }
  },

  async getUsers(params: ListParams = {}) {
    const payload = await request<PageResult<AdminUserItem>>({
      url: '/api/v1/admin/users',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 10,
      },
    })

    return toPagedResult(payload, mapAdminUser)
  },

  async getArticles(params: AdminArticleQuery = {}) {
    const payload = await request<PageResult<AdminArticleItem>>({
      url: '/api/v1/admin/articles',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 10,
        ...(params.keyword ? { keyword: params.keyword } : {}),
        ...(params.category ? { category: params.category } : {}),
        ...(params.status ? { status: params.status } : {}),
      },
    })

    return toPagedResult(payload, mapAdminArticle)
  },

  async approveArticle(articleId: number) {
    const payload = await request<AdminArticleItem>({
      url: `/api/v1/admin/articles/${articleId}/approve`,
      method: 'patch',
    })

    return { data: mapAdminArticle(payload) }
  },

  async rejectArticle(articleId: number) {
    const payload = await request<AdminArticleItem>({
      url: `/api/v1/admin/articles/${articleId}/reject`,
      method: 'patch',
    })

    return { data: mapAdminArticle(payload) }
  },

  async deleteArticle(articleId: number) {
    const payload = await request<Record<string, unknown>>({
      url: `/api/v1/admin/articles/${articleId}`,
      method: 'delete',
    })

    return { data: payload }
  },

  async getAiSessions(params: ListParams = {}) {
    const payload = await request<PageResult<AdminAiSessionItem>>({
      url: '/api/v1/admin/ai/sessions',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 10,
      },
    })

    return toPagedResult(payload, mapAdminAiSession)
  },
}
