import http from './http'
import {
  mapAiMessage,
  mapAiSession,
  buildArticleListQuery,
  mapArticle,
  mapPageResult,
  mapUserProfile,
} from './transformers'

const request = async (config) => {
  const response = await http(config)
  const payload = response.data

  if (payload?.code !== 0) {
    throw new Error(payload?.message || '请求失败')
  }

  return payload.data
}

const pickArticlePayload = (data = {}) => ({
  title: data.title,
  category: data.category,
  type: data.type,
  tags: Array.isArray(data.tags) ? data.tags : [],
  content: data.content,
})

export const authApi = {
  async register(data) {
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

  async login(data) {
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
  async updateProfile(data) {
    const payload = await request({
      url: '/api/v1/users/me/profile',
      method: 'patch',
      data,
    })

    return {
      data: mapUserProfile(payload),
    }
  },

  async uploadAvatar(file) {
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
  async getHot(params = {}) {
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

  async getList(params = {}) {
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

  async getDetail(id) {
    const payload = await request({
      url: `/api/v1/articles/${id}`,
      method: 'get',
    })

    return {
      data: mapArticle(payload),
    }
  },

  async create(data) {
    const payload = await request({
      url: '/api/v1/articles',
      method: 'post',
      data: pickArticlePayload(data),
    })

    return {
      data: mapArticle(payload),
    }
  },

  async update(articleId, data) {
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

  async getMine(params = {}) {
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
  async add(articleId) {
    const payload = await request({
      url: `/api/v1/articles/${articleId}/favorite`,
      method: 'post',
    })

    return {
      data: payload,
    }
  },

  async remove(articleId) {
    const payload = await request({
      url: `/api/v1/articles/${articleId}/favorite`,
      method: 'delete',
    })

    return {
      data: payload,
    }
  },

  async getFavorites(params = {}) {
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
  async getSessions(params = {}) {
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

  async createSession(data = {}) {
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

  async getMessages(sessionId, params = {}) {
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

  async deleteSession(sessionId) {
    const payload = await request({
      url: `/api/v1/ai/sessions/${sessionId}`,
      method: 'delete',
    })

    return {
      data: payload,
    }
  },
}
