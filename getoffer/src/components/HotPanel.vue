<template>
  <div class="right-sidebar">
    <el-card class="panel" shadow="never">
      <template #header>
        <span>热门八股 TOP5</span>
      </template>
      <div
        v-for="(item, index) in hotList"
        :key="item.id"
        class="hot-item"
        @click="goToDetail(item.id)"
      >
        <span class="hot-rank">{{ index + 1 }}</span>
        <div class="hot-info">
          <div class="hot-title">{{ item.title }}</div>
          <div class="hot-stats">{{ item.viewCount }} 浏览 · {{ item.favoriteCount }} 收藏</div>
        </div>
      </div>
      <el-divider />
      <div class="favorite-entry" @click="goToFavorites">
        <el-icon class="favorite-icon"><StarFilled /></el-icon>
        <span>我的收藏</span>
        <el-badge :value="favoriteCount" class="favorites-count" type="primary" />
      </div>
    </el-card>

    <el-card class="panel" shadow="never">
      <template #header>
        <span>快速入口</span>
      </template>
      <div class="upload-tip" @click="goToUpload">
        <p>上传你的八股 / 面经</p>
        <p class="tip-text">支持 Markdown，支持分类与标签</p>
        <el-input placeholder="开始写文章..." readonly />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { StarFilled } from '@element-plus/icons-vue'
import { articleApi, favoriteApi } from '@/api/frontend'
import { useUserStore } from '@/stores/admin'

const router = useRouter()
const userStore = useUserStore()
const hotList = ref([])
const favoriteCount = ref(0)

const goToDetail = (id) => router.push(`/article/${id}`)
const goToFavorites = () => router.push('/favorites')
const goToUpload = () => router.push('/upload')

const fetchHotList = async () => {
  try {
    const response = await articleApi.getHot({ limit: 5 })
    hotList.value = response.data
  } catch (error) {
    console.error('fetch hot list failed', error)
  }
}

const fetchFavoriteCount = async () => {
  if (!userStore.isLoggedIn) {
    favoriteCount.value = 0
    return
  }

  try {
    const response = await favoriteApi.getFavorites({ page: 1, pageSize: 1 })
    favoriteCount.value = response.pagination?.total || response.data.length || 0
  } catch (error) {
    console.error('fetch favorite count failed', error)
  }
}

const handleFavoritesUpdated = () => {
  fetchFavoriteCount()
}

onMounted(async () => {
  await fetchHotList()
  await fetchFavoriteCount()
  window.addEventListener('favorites-updated', handleFavoritesUpdated)
})

onUnmounted(() => {
  window.removeEventListener('favorites-updated', handleFavoritesUpdated)
})

watch(
  () => userStore.isLoggedIn,
  () => {
    fetchFavoriteCount()
  },
)
</script>

<style scoped>
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel {
  border-radius: 20px;
  border: 1px solid #eef0f4;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
}

.hot-item:last-child {
  border-bottom: none;
}

.hot-rank {
  font-weight: 700;
  color: #a1a9b7;
  width: 20px;
}

.hot-info .hot-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.hot-stats {
  font-size: 12px;
  color: #8b919e;
}

.favorite-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 0;
}

.favorites-count {
  margin-left: auto;
  font-size: 14px;
  font-weight: normal;
}

.favorite-icon {
  color: #f59e0b;
}

.upload-tip {
  background: #fafaff;
  border-radius: 16px;
  padding: 16px;
  text-align: center;
  border: 1px dashed #cbd5e1;
  cursor: pointer;
}

.tip-text {
  font-size: 13px;
  color: #64748b;
  margin: 8px 0;
}

.ai-tip {
  margin-top: 16px;
}
</style>
