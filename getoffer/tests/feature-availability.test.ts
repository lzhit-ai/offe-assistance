import test from 'node:test'
import assert from 'node:assert/strict'

import { getFeatureAvailability } from '../src/features/availability.ts'

test('getFeatureAvailability marks AI assistant as enabled', () => {
  assert.deepEqual(getFeatureAvailability('aiAssistant'), {
    enabled: true,
    badge: '已开放',
    message: 'AI 助手已经接入真实后端，会保存你的会话和消息记录。',
  })
})
