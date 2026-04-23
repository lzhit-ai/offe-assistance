interface FeatureAvailability {
  enabled: boolean
  badge: string
  message: string
}

const FEATURE_AVAILABILITY: Record<string, FeatureAvailability> = {
  aiAssistant: {
    enabled: true,
    badge: '已开放',
    message: 'AI 助手已经接入真实后端，会保存你的会话和消息记录。',
  },
}

export const getFeatureAvailability = (featureKey: string): FeatureAvailability =>
  FEATURE_AVAILABILITY[featureKey] || {
    enabled: true,
    badge: '',
    message: '',
  }
