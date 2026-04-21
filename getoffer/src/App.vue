<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { AUTH_EXPIRED_EVENT } from '@/auth/session'
import { useUserStore } from '@/stores/admin'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const handleAuthExpired = () => {
  userStore.logout()
  ElMessage.warning('登录状态已失效，请重新登录')

  if (route.meta?.requiresAuth) {
    router.replace({
      path: '/',
      query: {
        login: '1',
        redirect: route.fullPath,
      },
    })
  }
}

onMounted(() => {
  window.addEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
})

onUnmounted(() => {
  window.removeEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
})
</script>

<style>
</style>
