<template>
  <div>
    <Navbar />
    <div class="profile-container" v-loading="loading">
      <div class="profile-header">
        <div class="profile-avatar">
          <el-avatar :size="80" :src="userAvatar">
            {{ userStore.user?.username?.charAt(0).toUpperCase() || 'U' }}
          </el-avatar>
        </div>
        <div class="profile-info">
          <h2>{{ userInfo.username || '用户' }}</h2>
          <p class="profile-email">{{ userInfo.email || '未设置邮箱' }}</p>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.stats.articleCount }}</span>
              <span class="stat-label">发布文章</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.stats.favoriteCount }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.stats.likeCount }}</span>
              <span class="stat-label">点赞</span>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-content">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="我的文章" name="articles">
            <div v-if="!loading && userArticles.length === 0" class="empty-state">
              <el-empty description="您还没有发布过文章" />
              <el-button type="primary" @click="goToUpload">去发布</el-button>
            </div>
            <div v-else class="article-list">
              <ArticleCard
                v-for="article in userArticles"
                :key="article.id"
                :article="article"
              />
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
          </el-tab-pane>
          <el-tab-pane label="个人信息" name="info">
            <div class="info-form">
              <el-form :model="userInfo" label-width="100px">
                <el-form-item label="用户名">
                  <el-input v-model="userInfo.username" disabled />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="userInfo.email" disabled />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="userInfo.phone" disabled />
                </el-form-item>
                <el-form-item label="注册时间">
                  <el-input v-model="userInfo.registerTime" disabled />
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { articleApi, authApi } from '@/api/frontend'
import { useUserStore } from '@/stores/admin'
import ArticleCard from '@/components/ArticleCard.vue'
import Navbar from '@/components/Navbar.vue'
import { PROFILE_ARTICLES_PAGE_SIZE } from '@/utils/pagination'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('articles')
const loading = ref(false)
const currentPage = ref(1)

const userInfo = ref({
  username: userStore.user?.username || '',
  email: userStore.user?.email || '',
  phone: userStore.user?.phone || '',
  registerTime: userStore.user?.registerTime || '',
  stats: {
    articleCount: userStore.user?.stats?.articleCount || 0,
    favoriteCount: userStore.user?.stats?.favoriteCount || 0,
    likeCount: userStore.user?.stats?.likeCount || 0,
  },
})

const userArticles = ref([])
const pagination = ref({
  page: 1,
  pageSize: PROFILE_ARTICLES_PAGE_SIZE,
  total: 0,
})

const userAvatar = computed(() => '')

const goToUpload = () => {
  router.push('/upload')
}

const loadProfile = async (page = currentPage.value) => {
  loading.value = true
  try {
    currentPage.value = page
    const [profileResponse, articleResponse] = await Promise.all([
      authApi.me(),
      articleApi.getMine({ page, pageSize: PROFILE_ARTICLES_PAGE_SIZE }),
    ])

    userInfo.value = profileResponse.data
    userArticles.value = articleResponse.data
    pagination.value = articleResponse.pagination
    userStore.setUser(profileResponse.data)
  } catch (error) {
    userStore.logout()
    ElMessage.error(error.message || '获取个人信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  loadProfile(page)
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }

  loadProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
  padding: 30px;
  background-color: #f8fafc;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.profile-avatar {
  margin-right: 30px;
}

.profile-avatar .el-avatar {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: white;
  font-size: 32px;
  font-weight: 600;
}

.profile-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.profile-email {
  margin: 0 0 20px 0;
  color: #64748b;
  font-size: 14px;
}

.profile-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #2563eb;
  margin-bottom: 4px;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #64748b;
}

.profile-content {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 30px;
}

.profile-tabs {
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-state .el-button {
  margin-top: 20px;
}

.article-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.info-form {
  max-width: 600px;
  margin-top: 20px;
}

@media (max-width: 1200px) and (min-width: 769px) {
  .article-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .profile-avatar {
    margin-right: 0;
    margin-bottom: 20px;
  }

  .profile-stats {
    justify-content: center;
  }

  .article-list {
    grid-template-columns: 1fr;
  }
}
</style>
