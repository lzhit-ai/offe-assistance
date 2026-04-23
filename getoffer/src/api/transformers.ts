type AnyRecord = Record<string, any>

type PageResult<T> = {
  list: T[]
  page: number
  pageSize: number
  total: number
  hasMore: boolean
}

type ArticleListQueryInput = {
  page?: number
  pageSize?: number
  type?: string | number
  category?: string
  tag?: string
  keyword?: string
  authorId?: string | number
  sort?: string
}

const formatDate = (value: string | number | Date | null | undefined): string => {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return date.toISOString().slice(0, 10)
}

const formatDateTime = (value: unknown): string => {
  if (!value || typeof value !== 'string') {
    return ''
  }

  return value.replace('T', ' ').slice(0, 16)
}

const toArticleType = (type: string | number | undefined): number | undefined => {
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

export const mapArticle = (article: AnyRecord = {}): AnyRecord => ({
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

export const mapPageResult = <T>(
  pageResult: AnyRecord = {},
  mapper: (item: AnyRecord) => T = (item) => item as T,
): PageResult<T> => ({
  list: Array.isArray(pageResult.list) ? pageResult.list.map(mapper) : [],
  page: pageResult.page || 1,
  pageSize: pageResult.pageSize || 10,
  total: pageResult.total || 0,
  hasMore: Boolean(pageResult.hasMore),
})

export const mapUserProfile = (profile: AnyRecord = {}): AnyRecord => ({
  ...profile,
  nickname: profile.nickname || '',
  avatar: profile.avatar || '',
  registerTime: formatDate(profile.registerTime),
  stats: {
    articleCount: profile.stats?.articleCount || 0,
    favoriteCount: profile.stats?.favoriteCount || 0,
    likeCount: profile.stats?.likeCount || 0,
  },
})

export const mapAiSession = (session: AnyRecord = {}): AnyRecord => ({
  id: session.id,
  title: session.title || '新对话',
  createdAt: formatDateTime(session.createdAt),
  updatedAt: formatDateTime(session.updatedAt),
  lastMessagePreview: session.lastMessagePreview || '',
})

export const mapAiMessage = (message: AnyRecord = {}): AnyRecord => ({
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
}: ArticleListQueryInput = {}): Record<string, string | number> => {
  const query: Record<string, string | number> = {
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
