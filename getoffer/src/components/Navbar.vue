<template>
  <div class="navbar-container">
    <el-header
      class="navbar"
      :class="{
        'navbar-fixed': isFixed,
        'navbar-visible': isNavVisible,
      }"
    >
      <router-link to="/" class="logo">
        <img src="/logo-favicon.png" alt="面经·八股 logo" class="brand-logo">
        <span class="brand-text">面经 · 八股</span>
      </router-link>

      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        router
        background-color="transparent"
        class="nav-menu"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/articles/tech">八股题</el-menu-item>
        <el-menu-item index="/articles/interview">面试经历</el-menu-item>
        <el-menu-item index="/favorites">收藏</el-menu-item>
        <el-menu-item index="/ai">AI 助手</el-menu-item>
      </el-menu>

      <div class="user-area">
        <div class="desktop-actions">
          <el-button v-if="userStore.isLoggedIn" type="default" round @click="goToUpload">
            上传
          </el-button>

          <template v-if="!userStore.isLoggedIn">
            <el-button type="primary" round @click="showLoginModal(true)">登录</el-button>
            <el-button type="default" round @click="showLoginModal(false)">注册</el-button>
          </template>

          <el-dropdown v-else trigger="click">
            <div class="user-avatar-wrapper">
              <el-avatar :size="36" :src="userAvatar" class="user-avatar">
                {{ avatarFallback }}
              </el-avatar>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  {{ displayName }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="goToProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="mobile-user-actions">
          <el-button
            v-if="!userStore.isLoggedIn"
            class="mobile-login-trigger"
            type="primary"
            round
            @click="showLoginModal(true)"
          >
            登录
          </el-button>

          <router-link v-else to="/profile" class="mobile-avatar-link" aria-label="个人中心">
            <el-avatar :size="36" :src="userAvatar" class="user-avatar">
              {{ avatarFallback }}
            </el-avatar>
          </router-link>

          <el-button
            class="mobile-menu-trigger"
            circle
            text
            bg
            aria-label="打开菜单"
            @click="mobileDrawerVisible = true"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
        </div>
      </div>

      <el-drawer
        v-model="mobileDrawerVisible"
        direction="rtl"
        size="300px"
        class="mobile-nav-drawer"
        title="导航"
      >
        <div class="mobile-nav">
          <div class="mobile-nav-section">
            <el-button text class="mobile-nav-link" @click="navigateTo('/')">首页</el-button>
            <el-button text class="mobile-nav-link" @click="navigateTo('/articles/tech')">八股题</el-button>
            <el-button text class="mobile-nav-link" @click="navigateTo('/articles/interview')">
              面试经历
            </el-button>
            <el-button text class="mobile-nav-link" @click="navigateTo('/favorites')">收藏</el-button>
            <el-button text class="mobile-nav-link" @click="navigateTo('/ai')">AI 助手</el-button>
            <el-button text class="mobile-nav-link" @click="goToUpload">上传</el-button>
          </div>

          <div class="mobile-nav-section mobile-user-section">
            <template v-if="userStore.isLoggedIn">
              <div class="mobile-user-card">
                <el-avatar :size="40" :src="userAvatar" class="user-avatar">
                  {{ avatarFallback }}
                </el-avatar>
                <div class="mobile-user-info">
                  <strong>{{ displayName }}</strong>
                  <span>{{ userStore.user?.username }}</span>
                </div>
              </div>

              <el-button text class="mobile-nav-link" @click="goToProfile">个人中心</el-button>
              <el-button text class="mobile-nav-link danger-link" @click="handleLogout">
                退出登录
              </el-button>
            </template>

            <template v-else>
              <el-button text class="mobile-nav-link" @click="openLoginFromDrawer(true)">登录</el-button>
              <el-button text class="mobile-nav-link" @click="openLoginFromDrawer(false)">注册</el-button>
            </template>
          </div>
        </div>
      </el-drawer>

      <LoginModal
        v-model:visible="loginModalVisible"
        :is-login="isLoginModal"
        @login-success="handleLoginSuccess"
      />
    </el-header>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { LocationQueryRaw } from 'vue-router'
import { ArrowDown, Menu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import LoginModal from './LoginModal.vue'
import { authApi } from '@/api/frontend'
import { useUserStore } from '@/stores/admin'
import { NAVBAR_THRESHOLD, getNavbarPhase } from '@/utils/navbar-visibility'
import { getAvatarFallback, getDisplayName, resolveAvatarUrl } from '@/utils/user-profile'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const ANIMATION_DURATION_MS = 320

const isFixed = ref(false)
const isNavVisible = ref(false)
const loginModalVisible = ref(false)
const isLoginModal = ref(true)
const pendingRedirect = ref('')
const mobileDrawerVisible = ref(false)
const hideTimer = ref<number | null>(null)

const clearHideTimer = () => {
  if (hideTimer.value) {
    window.clearTimeout(hideTimer.value)
    hideTimer.value = null
  }
}

const showFixedNavbar = () => {
  clearHideTimer()

  if (!isFixed.value) {
    isFixed.value = true
    isNavVisible.value = false
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        isNavVisible.value = true
      })
    })
    return
  }

  isNavVisible.value = true
}

const hideFixedNavbar = () => {
  if (!isFixed.value) {
    return
  }

  clearHideTimer()
  isNavVisible.value = false
  hideTimer.value = window.setTimeout(() => {
    if (getNavbarPhase(window.scrollY, NAVBAR_THRESHOLD) === 'static' && !isNavVisible.value) {
      isFixed.value = false
    }
  }, ANIMATION_DURATION_MS)
}

const handleScroll = () => {
  const phase = getNavbarPhase(window.scrollY, NAVBAR_THRESHOLD)

  if (phase === 'fixed') {
    showFixedNavbar()
    return
  }

  hideFixedNavbar()
}

const hydrateCurrentUser = async () => {
  if (!userStore.token || userStore.user) {
    return
  }

  try {
    const response = await authApi.me()
    userStore.setUser(response.data)
  } catch {
    userStore.logout()
  }
}

const consumeLoginIntent = async (query: Record<string, unknown>) => {
  if (query.login !== '1') {
    return
  }

  pendingRedirect.value = typeof query.redirect === 'string' ? query.redirect : ''
  showLoginModal(true)

  const nextQuery: LocationQueryRaw = {}
  Object.entries(query).forEach(([key, value]) => {
    if (typeof value === 'string' || Array.isArray(value) || typeof value === 'number' || value == null) {
      nextQuery[key] = value as string | string[] | number | null | undefined
    }
  })
  delete nextQuery.login
  delete nextQuery.redirect

  await router.replace({
    path: route.path,
    query: nextQuery,
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll()
  hydrateCurrentUser()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  clearHideTimer()
})

watch(
  () => route.query,
  (query) => {
    consumeLoginIntent(query as Record<string, unknown>)
  },
  { immediate: true },
)

const activeMenu = computed(() => {
  if (route.name === 'home') return '/'
  if (route.params.type === 'tech') return '/articles/tech'
  if (route.params.type === 'interview') return '/articles/interview'
  if (route.name === 'favorites') return '/favorites'
  if (route.name === 'ai') return '/ai'
  return '/'
})

const userAvatar = computed(() => resolveAvatarUrl(userStore.user?.avatar || ''))
const displayName = computed(() => getDisplayName(userStore.user))
const avatarFallback = computed(() => getAvatarFallback(userStore.user))

const showLoginModal = (isLogin: boolean) => {
  isLoginModal.value = isLogin
  loginModalVisible.value = true
}

const openLoginFromDrawer = (isLogin: boolean) => {
  mobileDrawerVisible.value = false
  showLoginModal(isLogin)
}

const handleLoginSuccess = () => {
  const target = pendingRedirect.value
  pendingRedirect.value = ''

  if (target) {
    router.push(target)
  }
}

const navigateTo = (path: string) => {
  mobileDrawerVisible.value = false
  router.push(path)
}

const goToUpload = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    mobileDrawerVisible.value = false
    showLoginModal(true)
    return
  }

  mobileDrawerVisible.value = false
  router.push('/upload')
}

const goToProfile = () => {
  mobileDrawerVisible.value = false
  router.push('/profile')
}

const handleLogout = async () => {
  try {
    await authApi.logout()
  } catch (error) {
    console.error('logout failed', error)
  } finally {
    pendingRedirect.value = ''
    mobileDrawerVisible.value = false
    userStore.logout()
    router.push('/')
  }
}
</script>

<style scoped>
.navbar-container {
  height: 64px;
  margin-bottom: 28px;
  position: relative;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 0 28px;
  border-radius: 48px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid #eef0f4;
  height: 64px;
  box-sizing: border-box;
  width: 100%;
  position: static;
}

.navbar-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  border-radius: 0;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  transform: translateY(-100%);
  opacity: 0;
  will-change: transform, opacity;
  transition:
    transform 320ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 220ms ease;
}

.navbar-visible {
  transform: translateY(0);
  opacity: 1;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 20px;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-decoration: none;
  white-space: nowrap;
}

.brand-logo {
  width: 28px;
  height: 28px;
  object-fit: contain;
  flex-shrink: 0;
}

.brand-text {
  background: inherit;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.nav-menu {
  border-bottom: none !important;
  flex: 1;
  margin-left: 40px;
}

.nav-menu .el-menu-item {
  font-weight: 500;
  color: #3c3f4a;
  height: 64px;
  line-height: 64px;
  border-bottom: 2px solid transparent;
}

.nav-menu .el-menu-item.is-active {
  color: #2563eb !important;
  border-bottom-color: #2563eb !important;
  background: transparent !important;
}

.user-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.desktop-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.mobile-user-actions {
  display: none;
  align-items: center;
  gap: 10px;
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px;
  border-radius: 20px;
  transition: background-color 0.2s;
}

.user-avatar-wrapper:hover {
  background-color: #f3f4f6;
}

.user-avatar {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: white;
  font-weight: 600;
}

.dropdown-icon {
  font-size: 12px;
  color: #6b7280;
}

.mobile-avatar-link {
  display: inline-flex;
}

.mobile-nav {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.mobile-nav-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mobile-nav-link {
  justify-content: flex-start;
  width: 100%;
  padding: 12px 0;
  font-size: 15px;
  color: #1f2937;
}

.mobile-user-section {
  padding-top: 20px;
  border-top: 1px solid #eef0f4;
}

.mobile-user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 18px;
  background: #f8fafc;
}

.mobile-user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #475569;
}

.mobile-user-info strong {
  color: #0f172a;
}

.danger-link {
  color: #dc2626;
}

@media (max-width: 960px) {
  .navbar {
    padding: 0 16px;
    gap: 12px;
  }

  .nav-menu {
    margin-left: 12px;
  }
}

@media (max-width: 767px) {
  .navbar-container {
    height: 56px;
    margin-bottom: 20px;
  }

  .navbar {
    height: 56px;
    border-radius: 28px;
    padding: 0 14px;
  }

  .logo {
    font-size: 18px;
    gap: 8px;
  }

  .brand-logo {
    width: 24px;
    height: 24px;
  }

  .nav-menu,
  .desktop-actions {
    display: none;
  }

  .mobile-user-actions {
    display: flex;
  }
}
</style>
