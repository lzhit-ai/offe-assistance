const FEATURE_AVAILABILITY = {
  aiAssistant: {
    enabled: true,
    badge: '已开放',
    message: 'AI 助手现已接入真实后端，会保存你的会话和消息记录。',
  },
}

export const getFeatureAvailability = (featureKey) =>
  FEATURE_AVAILABILITY[featureKey] || {
    enabled: true,
    badge: '',
    message: '',
  }
