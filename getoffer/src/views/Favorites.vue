<template>
  <div>
    <Navbar />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="6" :md="5" class="sidebar-col">
        <CategorySidebar />
      </el-col>
      <el-col :xs="24" :sm="12" :md="14" class="content-col">
        <MobileCategoryDrawer />

        <div class="favorites-header">
          <h2>
            <el-icon class="favorites-icon"><StarFilled /></el-icon>
            我的收藏
            <el-badge :value="pagination.total" class="favorites-count" type="primary" />
          </h2>
        </div>

        <div v-if="!userStore.isLoggedIn" class="login-prompt">
          <el-empty description="请先登录查看收藏" />
          <el-button type="primary" @click="showLoginModal">去登录</el-button>
        </div>

        <template v-else>
          <ArticleCard
            v-for="article in favoriteArticles"
            :key="article.id"
            :article="article"
          />

          <el-empty v-if="!loading && favoriteArticles.length === 0" description="暂无收藏" />
          <div v-if="loading" class="loading-placeholder">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="pagination.page"
              :page-size="pagination.pageSize"
              :total="pagination.total"
              @current-change="handlePageChange"
            />
          </div>
        </template>
      </el-col>
      <el-col :xs="24" :sm="6" :md="5" class="hot-panel-col">
        <HotPanel />
      </el-col>
    </el-row>

    <LoginModal
      v-model:visible="loginModalVisible"
      :is-login="true"
      @login-success="fetchFavorites"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import CategorySidebar from '@/components/CategorySidebar.vue'
import MobileCategoryDrawer from '@/components/MobileCategoryDrawer.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import HotPanel from '@/components/HotPanel.vue'
import LoginModal from '@/components/LoginModal.vue'
import { favoriteApi } from '@/api/frontend'
import type { ArticleItem, PageResult } from '@/api/transformers'
import { useUserStore } from '@/stores/admin'
import { FAVORITES_PAGE_SIZE } from '@/utils/pagination'

const userStore = useUserStore()
const favoriteArticles = ref<ArticleItem[]>([])
const loading = ref(false)
const loginModalVisible = ref(false)
const currentPage = ref(1)
const pagination = ref<PageResult<ArticleItem>>({
  list: [],
  page: 1,
  pageSize: FAVORITES_PAGE_SIZE,
  total: 0,
  hasMore: false,
})

const fetchFavorites = async (page = currentPage.value) => {
  if (!userStore.isLoggedIn) {
    favoriteArticles.value = []
    pagination.value = {
      list: [],
      page: 1,
      pageSize: FAVORITES_PAGE_SIZE,
      total: 0,
      hasMore: false,
    }
    return
  }

  loading.value = true
  try {
    currentPage.value = page
    const response = await favoriteApi.getFavorites({
      page,
      pageSize: FAVORITES_PAGE_SIZE,
    })
    favoriteArticles.value = response.data
    pagination.value = response.pagination
  } catch (error) {
    favoriteArticles.value = []
    pagination.value = {
      list: [],
      page: 1,
      pageSize: FAVORITES_PAGE_SIZE,
      total: 0,
      hasMore: false,
    }
    ElMessage.error(error instanceof Error ? error.message : '获取收藏失败')
  } finally {
    loading.value = false
  }
}

const showLoginModal = () => {
  loginModalVisible.value = true
}

const handleFavoritesUpdated = () => {
  fetchFavorites(currentPage.value)
}

const handlePageChange = (page: number) => {
  fetchFavorites(page)
}

onMounted(() => {
  fetchFavorites()
  window.addEventListener('favorites-updated', handleFavoritesUpdated)
})

onUnmounted(() => {
  window.removeEventListener('favorites-updated', handleFavoritesUpdated)
})

watch(
  () => userStore.isLoggedIn,
  () => {
    currentPage.value = 1
    fetchFavorites(1)
  },
)
</script>

<style scoped>
.sidebar-col {
  display: block;
}

.favorites-header {
  margin-bottom: 20px;
}

.favorites-icon {
  color: #f59e0b;
}

.login-prompt {
  text-align: center;
  padding: 40px 0;
}

.loading-placeholder {
  margin-top: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin: 24px 0 8px;
}

@media (max-width: 1024px) {
  .hot-panel-col {
    display: none;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .content-col {
    flex: 0 0 75%;
    max-width: 75%;
  }
}

@media (max-width: 767px) {
  .sidebar-col {
    display: none;
  }
}
</style>
