export const getDisplayName = (user = {}) => user.nickname?.trim() || user.username || 'User'

export const getAvatarFallback = (user = {}) => {
  const displayName = getDisplayName(user)
  return displayName?.charAt(0).toUpperCase() || 'U'
}

export const normalizeNicknameInput = (value = '') => value.trim()

export const validateNickname = (value = '') => {
  const nickname = normalizeNicknameInput(value)

  if (!nickname) {
    return { valid: false, message: 'Nickname is required' }
  }

  if (nickname.length > 10) {
    return { valid: false, message: 'Nickname must be 10 characters or fewer' }
  }

  return { valid: true, message: '' }
}

export const isAcceptedAvatarFile = (file, maxSize = 2 * 1024 * 1024) => {
  if (!file?.type?.startsWith('image/')) {
    return { valid: false, message: 'Please upload an image file' }
  }

  if (file.size > maxSize) {
    return { valid: false, message: 'Avatar must be 2MB or smaller' }
  }

  return { valid: true, message: '' }
}
