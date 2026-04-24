import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const profileViewSource = readFileSync(
  resolve(process.cwd(), 'src/views/Profile.vue'),
  'utf8',
)

test('profile view uploads avatar from file selection change', () => {
  assert.match(profileViewSource, /:on-change="handleAvatarChange"/)
  assert.doesNotMatch(profileViewSource, /:before-upload="handleAvatarBeforeUpload"/)
})

test('profile view exposes a direct nickname edit entry in the header', () => {
  assert.match(profileViewSource, /@click="openInfoTab"/)
  assert.match(profileViewSource, /编辑昵称/)
})

test('profile view renders moderation status labels for personal articles', () => {
  assert.match(profileViewSource, /待审核/)
  assert.match(profileViewSource, /已通过/)
  assert.match(profileViewSource, /已拒绝/)
})

test('profile view renders like counter from user stats', () => {
  assert.match(profileViewSource, /userInfo\.stats\.likeCount/)
  assert.match(profileViewSource, /点赞/)
})
