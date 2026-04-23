import type { AxiosRequestConfig } from 'axios'

import http from './http'
import {
  mapAiMessage,
  mapAiSession,
  buildArticleListQuery,
  mapArticle,
  mapPageResult,
  mapUserProfile,
} from './transformers'

type AnyRecord = Record<string, any>

const request = async (config: AxiosRequestConfig): Promise<any> => {
  const response = await http(config)
  const payload = response.data

  if (payload?.code !== 0) {
    throw new Error(payload?.message || '请求失败')
  }

  return payload.data
}

const pickArticlePayload = (data: AnyRecord = {}): AnyRecord => ({
  title: data.title,
  category: data.category,
  type: data.type,
  tags: Array.isArray(data.tags) ? data.tags : [],
  content: data.content,
})

export const authApi = {
  async register(data: AnyRecord) {
    const payload = await request({
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

  async login(data: AnyRecord) {
    const payload = await request({
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
    const payload = await request({
      url: '/api/v1/auth/me',
      method: 'get',
    })

    return {
      data: mapUserProfile(payload),
    }
  },

  async logout() {
    return request({
      url: '/api/v1/auth/logout',
      method: 'post',
    })
  },
}

export const userApi = {
  async updateProfile(data: AnyRecord) {
    const payload = await request({
      url: '/api/v1/users/me/profile',
      method: 'patch',
      data,
    })

    return {
      data: mapUserProfile(payload),
    }
  },

  async uploadAvatar(file: File | Blob) {
    const formData = new FormData()
    formData.append('file', file)

    const payload = await request({
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
  async getHot(params: AnyRecord = {}) {
    const payload = await request({
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

  async getList(params: AnyRecord = {}) {
    const payload = await request({
      url: '/api/v1/articles',
      method: 'get',
      params: buildArticleListQuery(params),
    })
    const page = mapPageResult(payload, mapArticle)

    return {
      data: page.list,
      pagination: page,
    }
  },

  async getDetail(id: number | string) {
    const payload = await request({
      url: `/api/v1/articles/${id}`,
      method: 'get',
    })

    return {
      data: mapArticle(payload),
    }
  },

  async create(data: AnyRecord) {
    const payload = await request({
      url: '/api/v1/articles',
      method: 'post',
      data: pickArticlePayload(data),
    })

    return {
      data: mapArticle(payload),
    }
  },

  async update(articleId: number | string, data: AnyRecord) {
    const id = articleId || data?.id
    const payload = await request({
      url: `/api/v1/articles/${id}`,
      method: 'put',
      data: pickArticlePayload(data),
    })

    return {
      data: mapArticle(payload),
    }
  },

  async getMine(params: AnyRecord = {}) {
    const payload = await request({
      url: '/api/v1/users/me/articles',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 20,
        ...(params.type ? { type: params.type } : {}),
      },
    })
    const page = mapPageResult(payload, mapArticle)

    return {
      data: page.list,
      pagination: page,
    }
  },
}

export const favoriteApi = {
  async add(articleId: number | string) {
    const payload = await request({
      url: `/api/v1/articles/${articleId}/favorite`,
      method: 'post',
    })

    return {
      data: payload,
    }
  },

  async remove(articleId: number | string) {
    const payload = await request({
      url: `/api/v1/articles/${articleId}/favorite`,
      method: 'delete',
    })

    return {
      data: payload,
    }
  },

  async getFavorites(params: AnyRecord = {}) {
    const payload = await request({
      url: '/api/v1/users/me/favorites',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 20,
      },
    })
    const page = mapPageResult(payload, mapArticle)

    return {
      data: page.list,
      pagination: page,
    }
  },
}

export const metadataApi = {
  async getCategories() {
    const payload = await request({
      url: '/api/v1/metadata/categories',
      method: 'get',
    })

    return {
      data: Array.isArray(payload) ? payload : [],
    }
  },

  async getHotTags(limit = 10) {
    const payload = await request({
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
  async getSessions(params: AnyRecord = {}) {
    const payload = await request({
      url: '/api/v1/ai/sessions',
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 20,
      },
    })
    const page = mapPageResult(payload, mapAiSession)

    return {
      data: page.list,
      pagination: page,
    }
  },

  async createSession(data: AnyRecord = {}) {
    const payload = await request({
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

  async getMessages(sessionId: number | string, params: AnyRecord = {}) {
    const payload = await request({
      url: `/api/v1/ai/sessions/${sessionId}/messages`,
      method: 'get',
      params: {
        page: params.page || 1,
        pageSize: params.pageSize || 100,
      },
    })
    const page = mapPageResult(payload, mapAiMessage)

    return {
      data: page.list,
      pagination: page,
    }
  },

  async deleteSession(sessionId: number | string) {
    const payload = await request({
      url: `/api/v1/ai/sessions/${sessionId}`,
      method: 'delete',
    })

    return {
      data: payload,
    }
  },
}
