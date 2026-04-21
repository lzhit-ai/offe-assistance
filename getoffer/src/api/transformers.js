const formatDate = (value) => {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return date.toISOString().slice(0, 10)
}

const formatDateTime = (value) => {
  if (!value || typeof value !== 'string') {
    return ''
  }

  return value.replace('T', ' ').slice(0, 16)
}

const toArticleType = (type) => {
  if (type === 'tech') {
    return 1
  }
  if (type === 'interview') {
    return 2
  }
  if (typeof type === 'number') {
    return type
  }
  if (typeof type === 'string' && type.trim() !== '') {
    const parsed = Number(type)
    return Number.isNaN(parsed) ? undefined : parsed
  }
  return undefined
}

export const mapArticle = (article = {}) => ({
  id: article.id,
  title: article.title || '',
  author: article.author?.username || article.author || '匿名用户',
  authorInfo: article.author || null,
  category: article.category || '',
  type: article.type ?? 1,
  tags: Array.isArray(article.tags) ? article.tags : [],
  createdAt: formatDate(article.createdAt),
  updatedAt: formatDate(article.updatedAt),
  viewCount: article.viewCount || 0,
  favoriteCount: article.favoriteCount || 0,
  commentCount: article.commentCount || 0,
  isFavorited: Boolean(article.isFavorited),
  content: article.content || '',
  canEdit: Boolean(article.canEdit),
})

export const mapPageResult = (pageResult = {}, mapper = (item) => item) => ({
  list: Array.isArray(pageResult.list) ? pageResult.list.map(mapper) : [],
  page: pageResult.page || 1,
  pageSize: pageResult.pageSize || 10,
  total: pageResult.total || 0,
  hasMore: Boolean(pageResult.hasMore),
})

export const mapUserProfile = (profile = {}) => ({
  ...profile,
  registerTime: formatDate(profile.registerTime),
  stats: {
    articleCount: profile.stats?.articleCount || 0,
    favoriteCount: profile.stats?.favoriteCount || 0,
    likeCount: profile.stats?.likeCount || 0,
  },
})

export const mapAiSession = (session = {}) => ({
  id: session.id,
  title: session.title || '新对话',
  createdAt: formatDateTime(session.createdAt),
  updatedAt: formatDateTime(session.updatedAt),
  lastMessagePreview: session.lastMessagePreview || '',
})

export const mapAiMessage = (message = {}) => ({
  id: message.id,
  role: message.role || 'assistant',
  content: message.content || '',
  createdAt: formatDateTime(message.createdAt),
  status: message.status || 'success',
})

export const buildArticleListQuery = ({
  page = 1,
  pageSize = 20,
  type,
  category,
  tag,
  keyword,
  authorId,
  sort,
} = {}) => {
  const query = {
    page,
    pageSize,
  }

  const normalizedType = toArticleType(type)
  if (normalizedType) {
    query.type = normalizedType
  }
  if (category) {
    query.category = category
  }
  if (tag) {
    query.tag = tag
  }
  if (keyword) {
    query.keyword = keyword
  }
  if (authorId) {
    query.authorId = authorId
  }
  if (sort) {
    query.sort = sort
  }

  return query
}
