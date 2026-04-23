type UserIdentity = {
  username?: string
  nickname?: string
}

type ValidationResult = {
  valid: boolean
  message: string
}

type AvatarFileLike = {
  type?: string | null
  size?: number
}

const isAbsoluteUrl = (value = ''): boolean => /^[a-z][a-z\d+\-.]*:/i.test(value) || value.startsWith('//')

export const getDisplayName = (user: UserIdentity = {}): string =>
  user.nickname?.trim() || user.username || '用户'

export const getAvatarFallback = (user: UserIdentity = {}): string => {
  const displayName = getDisplayName(user)
  return displayName?.charAt(0).toUpperCase() || 'U'
}

export const resolveAvatarUrl = (avatar = '', apiBaseUrl = ''): string => {
  const normalizedAvatar = avatar.trim()
  if (!normalizedAvatar) {
    return ''
  }

  if (isAbsoluteUrl(normalizedAvatar)) {
    return normalizedAvatar
  }

  const normalizedBase = apiBaseUrl.trim()
  if (!normalizedBase) {
    return normalizedAvatar
  }

  return new URL(
    normalizedAvatar,
    normalizedBase.endsWith('/') ? normalizedBase : `${normalizedBase}/`,
  ).toString()
}

export const normalizeNicknameInput = (value = ''): string => value.trim()

export const validateNickname = (value = ''): ValidationResult => {
  const nickname = normalizeNicknameInput(value)

  if (!nickname) {
    return { valid: false, message: '昵称不能为空' }
  }

  if (nickname.length > 10) {
    return { valid: false, message: '昵称最长 10 个字符' }
  }

  return { valid: true, message: '' }
}

export const isAcceptedAvatarFile = (
  file: AvatarFileLike | null | undefined,
  maxSize = 2 * 1024 * 1024,
): ValidationResult => {
  if (!file?.type?.startsWith('image/')) {
    return { valid: false, message: '请上传图片文件' }
  }

  if ((file.size || 0) > maxSize) {
    return { valid: false, message: '头像大小不能超过 2MB' }
  }

  return { valid: true, message: '' }
}
