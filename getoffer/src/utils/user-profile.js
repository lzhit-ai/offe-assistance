export const getDisplayName = (user = {}) => user.nickname?.trim() || user.username || '用户'

export const getAvatarFallback = (user = {}) => {
  const displayName = getDisplayName(user)
  return displayName?.charAt(0).toUpperCase() || 'U'
}

export const normalizeNicknameInput = (value = '') => value.trim()

export const validateNickname = (value = '') => {
  const nickname = normalizeNicknameInput(value)

  if (!nickname) {
    return { valid: false, message: '昵称不能为空' }
  }

  if (nickname.length > 10) {
    return { valid: false, message: '昵称最长 10 个字符' }
  }

  return { valid: true, message: '' }
}

export const isAcceptedAvatarFile = (file, maxSize = 2 * 1024 * 1024) => {
  if (!file?.type?.startsWith('image/')) {
    return { valid: false, message: '请上传图片文件' }
  }

  if (file.size > maxSize) {
    return { valid: false, message: '头像大小不能超过 2MB' }
  }

  return { valid: true, message: '' }
}
