type FeatureAvailability = {
  enabled: boolean
  badge: string
  message: string
}

const FEATURE_AVAILABILITY: Record<string, FeatureAvailability> = {
  aiAssistant: {
    enabled: true,
    badge: '已开放',
    message: 'AI 助手现已接入真实后端，会保存你的会话和消息记录。',
  },
}

const DEFAULT_FEATURE_AVAILABILITY: FeatureAvailability = {
  enabled: true,
  badge: '',
  message: '',
}

export const getFeatureAvailability = (featureKey: string): FeatureAvailability =>
  FEATURE_AVAILABILITY[featureKey] || DEFAULT_FEATURE_AVAILABILITY
