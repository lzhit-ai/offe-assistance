import test from 'node:test'
import assert from 'node:assert/strict'

import {
  getDisplayName,
  getAvatarFallback,
  resolveAvatarUrl,
  normalizeNicknameInput,
  validateNickname,
  isAcceptedAvatarFile,
} from '../src/utils/user-profile.ts'

test('getDisplayName prefers nickname over username', () => {
  assert.equal(getDisplayName({ username: 'login_name', nickname: 'display_name' }), 'display_name')
  assert.equal(getDisplayName({ username: 'login_name', nickname: '' }), 'login_name')
})

test('getDisplayName falls back to 用户 when nickname and username are absent', () => {
  assert.equal(getDisplayName({}), '用户')
})

test('getAvatarFallback uses the first visible character', () => {
  assert.equal(getAvatarFallback({ username: 'login_name', nickname: 'display_name' }), 'D')
  assert.equal(getAvatarFallback({ username: 'login_name', nickname: '' }), 'L')
})

test('resolveAvatarUrl prefixes relative avatar paths with the api base url', () => {
  assert.equal(
    resolveAvatarUrl('/uploads/avatars/demo.png', 'http://localhost:8080'),
    'http://localhost:8080/uploads/avatars/demo.png',
  )
})

test('resolveAvatarUrl keeps absolute avatar urls unchanged', () => {
  assert.equal(
    resolveAvatarUrl('https://cdn.example.com/avatar.png', 'http://localhost:8080'),
    'https://cdn.example.com/avatar.png',
  )
})

test('normalizeNicknameInput trims surrounding whitespace', () => {
  assert.equal(normalizeNicknameInput('  new_name  '), 'new_name')
})

test('validateNickname rejects values longer than ten characters', () => {
  assert.equal(validateNickname('1234567890').valid, true)
  assert.equal(validateNickname('12345678901').valid, false)
})

test('validateNickname rejects empty values with a Chinese message', () => {
  assert.deepEqual(validateNickname('   '), {
    valid: false,
    message: '昵称不能为空',
  })
})

test('isAcceptedAvatarFile only accepts image files under the size limit', () => {
  assert.equal(isAcceptedAvatarFile({ type: 'image/png', size: 1024 }).valid, true)
  assert.equal(isAcceptedAvatarFile({ type: 'text/plain', size: 1024 }).valid, false)
})
