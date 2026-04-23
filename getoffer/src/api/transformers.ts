export type UserRole = 'USER' | 'ADMIN' | (string & {})
export type ArticleStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | (string & {})

interface RawAuthor {
  id?: number
  username?: string
  nickname?: string
}

interface RawStats {
  articleCount?: number
  favoriteCount?: number
  likeCount?: number
}

interface RawPageResult<T> {
  list?: T[]
  page?: number
  pageSize?: number
  total?: number
  hasMore?: boolean
}

interface RawArticle {
  id?: number
  title?: string
  author?: RawAuthor | string
  category?: string
  type?: number
  status?: ArticleStatus
  tags?: string[]
  createdAt?: string
  updatedAt?: string
  viewCount?: number
  favoriteCount?: number
  commentCount?: number
  isFavorited?: boolean
  content?: string
  canEdit?: boolean
}

interface RawUserProfile {
  id?: number
  username?: string
  nickname?: string
  phone?: string
  email?: string
  avatar?: string
  role?: UserRole
  registerTime?: string
  stats?: RawStats
}

interface RawAiSession {
  id?: number
  title?: string
  createdAt?: string
  updatedAt?: string
  lastMessagePreview?: string
}

interface RawAiMessage {
  id?: number
  role?: string
  content?: string
  createdAt?: string
  status?: string
}

interface RawAdminUser {
  id?: number
  username?: string
  nickname?: string
  phone?: string
  email?: string
  role?: UserRole
  registerTime?: string
}

interface RawAdminArticle {
  id?: number
  title?: string
  authorId?: number
  authorName?: string
  category?: string
  type?: number
  status?: ArticleStatus
  tags?: string[]
  createdAt?: string
  updatedAt?: string
}

interface RawAdminAiSession {
  id?: number
  userId?: number
  username?: string
  title?: string
  createdAt?: string
  updatedAt?: string
}

export interface ArticleItem {
  id?: number
  title: string
  author: string
  authorInfo: RawAuthor | string | null
  category: string
  type: number
  status: ArticleStatus
  tags: string[]
  createdAt: string
  updatedAt: string
  viewCount: number
  favoriteCount: number
  commentCount: number
  isFavorited: boolean
  content: string
  canEdit: boolean
}

export interface UserStats {
  articleCount: number
  favoriteCount: number
  likeCount: number
}

export interface UserProfile {
  id?: number
  username: string
  nickname: string
  phone: string
  email: string
  avatar: string
  role: UserRole
  registerTime: string
  stats: UserStats
}

export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
  hasMore: boolean
}

export interface AiSessionItem {
  id?: number
  title: string
  createdAt: string
  updatedAt: string
  lastMessagePreview: string
}

export interface AiMessageItem {
  id?: number
  role: string
  content: string
  createdAt: string
  status: string
}

export interface AdminDashboard {
  userCount: number
  articleCount: number
  aiSessionCount: number
}

export interface AdminUserItem {
  id?: number
  username: string
  nickname: string
  phone: string
  email: string
  role: UserRole
  registerTime: string
}

export interface AdminArticleItem {
  id?: number
  title: string
  authorId?: number
  authorName: string
  category: string
  type: number
  status: ArticleStatus
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface AdminAiSessionItem {
  id?: number
  userId?: number
  username: string
  title: string
  createdAt: string
  updatedAt: string
}

export interface MetadataItem {
  id?: number
  name: string
}

const formatDate = (value?: string) => {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return date.toISOString().slice(0, 10)
}

const formatDateTime = (value?: string) => {
  if (!value) {
    return ''
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }

  const iso = new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString()
  return iso.slice(0, 16).replace('T', ' ')
}

const toArticleType = (type?: number | string) => {
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

const normalizeArticleStatus = (status?: ArticleStatus): ArticleStatus => {
  if (status === 'PENDING' || status === 'REJECTED' || status === 'APPROVED') {
    return status
  }

  return 'APPROVED'
}

export const mapArticle = (article: RawArticle = {}): ArticleItem => ({
  id: article.id,
  title: article.title || '',
  author:
    (typeof article.author === 'object' ? article.author?.nickname || article.author?.username : article.author) ||
    '匿名用户',
  authorInfo: article.author || null,
  category: article.category || '',
  type: article.type ?? 1,
  status: normalizeArticleStatus(article.status),
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

export const mapPageResult = <TInput, TOutput>(
  pageResult: RawPageResult<TInput> = {},
  mapper: (item: TInput) => TOutput = ((item: TInput) => item as unknown as TOutput),
): PageResult<TOutput> => ({
  list: Array.isArray(pageResult.list) ? pageResult.list.map(mapper) : [],
  page: pageResult.page || 1,
  pageSize: pageResult.pageSize || 10,
  total: pageResult.total || 0,
  hasMore: Boolean(pageResult.hasMore),
})

export const mapUserProfile = (profile: RawUserProfile = {}): UserProfile => ({
  id: profile.id,
  username: profile.username || '',
  nickname: profile.nickname || '',
  phone: profile.phone || '',
  email: profile.email || '',
  avatar: profile.avatar || '',
  role: profile.role || 'USER',
  registerTime: formatDate(profile.registerTime),
  stats: {
    articleCount: profile.stats?.articleCount || 0,
    favoriteCount: profile.stats?.favoriteCount || 0,
    likeCount: profile.stats?.likeCount || 0,
  },
})

export const mapAiSession = (session: RawAiSession = {}): AiSessionItem => ({
  id: session.id,
  title: session.title || '新对话',
  createdAt: formatDateTime(session.createdAt),
  updatedAt: formatDateTime(session.updatedAt),
  lastMessagePreview: session.lastMessagePreview || '',
})

export const mapAiMessage = (message: RawAiMessage = {}): AiMessageItem => ({
  id: message.id,
  role: message.role || 'assistant',
  content: message.content || '',
  createdAt: formatDateTime(message.createdAt),
  status: message.status || 'success',
})

export const mapAdminDashboard = (dashboard: Partial<AdminDashboard> = {}): AdminDashboard => ({
  userCount: dashboard.userCount || 0,
  articleCount: dashboard.articleCount || 0,
  aiSessionCount: dashboard.aiSessionCount || 0,
})

export const mapAdminUser = (user: RawAdminUser = {}): AdminUserItem => ({
  id: user.id,
  username: user.username || '',
  nickname: user.nickname || '',
  phone: user.phone || '',
  email: user.email || '',
  role: user.role || 'USER',
  registerTime: formatDate(user.registerTime),
})

export const mapAdminArticle = (article: RawAdminArticle = {}): AdminArticleItem => ({
  id: article.id,
  title: article.title || '',
  authorId: article.authorId,
  authorName: article.authorName || '',
  category: article.category || '',
  type: article.type ?? 1,
  status: normalizeArticleStatus(article.status),
  tags: Array.isArray(article.tags) ? article.tags : [],
  createdAt: formatDateTime(article.createdAt),
  updatedAt: formatDateTime(article.updatedAt),
})

export const mapAdminAiSession = (session: RawAdminAiSession = {}): AdminAiSessionItem => ({
  id: session.id,
  userId: session.userId,
  username: session.username || '',
  title: session.title || '未命名会话',
  createdAt: formatDateTime(session.createdAt),
  updatedAt: formatDateTime(session.updatedAt),
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
}: {
  page?: number
  pageSize?: number
  type?: number | string
  category?: string
  tag?: string
  keyword?: string
  authorId?: number
  sort?: string
} = {}) => {
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
