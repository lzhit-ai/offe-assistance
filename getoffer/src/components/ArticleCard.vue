<template>
  <el-card
    class="article-card"
    shadow="hover"
    @click="goToDetail"
  >
    <div class="card-header">
      <el-tag size="small" type="primary">{{ article.category }}</el-tag>
      <el-tag size="small" :type="article.type === 1 ? 'warning' : 'success'">
        {{ article.type === 1 ? '八股' : '面经' }}
      </el-tag>
    </div>

    <h3 class="card-title">{{ article.title }}</h3>

    <div class="card-meta">
      <span><el-icon><User /></el-icon> {{ article.author }}</span>
      <span><el-icon><Calendar /></el-icon> {{ article.createdAt }}</span>
      <span><el-icon><View /></el-icon> {{ article.viewCount }} 浏览</span>
    </div>

    <div class="tags">
      <el-tag
        v-for="tag in article.tags"
        :key="tag"
        size="small"
        effect="plain"
        class="tag"
      >
        {{ tag }}
      </el-tag>
    </div>

    <div class="action-icons">
      <el-button
        text
        @click.stop="toggleFavorite"
      >
        <el-icon :class="{ favorited: isFavorited }">
          <component :is="isFavorited ? StarFilled : Star" />
        </el-icon>
        收藏 {{ favoriteCount }}
      </el-button>
      <el-button text :icon="ChatLineRound" @click.stop="goToComments">
        {{ article.commentCount || 0 }}
      </el-button>
      <el-button
        v-if="isAuthor"
        text
        type="primary"
        @click.stop="goToEdit"
      >
        <el-icon><Edit /></el-icon>
        编辑
      </el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Calendar, View, Star, ChatLineRound, StarFilled, Edit } from '@element-plus/icons-vue'
import { favoriteApi } from '@/api/frontend'
import type { ArticleItem } from '@/api/transformers'
import { useUserStore } from '@/stores/admin'

const props = defineProps<{
  article: ArticleItem
}>()

const router = useRouter()
const userStore = useUserStore()
const isFavorited = ref(false)
const favoriteCount = ref(0)

const syncFavoriteState = () => {
  isFavorited.value = Boolean(props.article?.isFavorited)
  favoriteCount.value = props.article?.favoriteCount || 0
}

watch(
  () => props.article,
  () => {
    syncFavoriteState()
  },
  { immediate: true, deep: true },
)

const isAuthor = computed(() => {
  return userStore.isLoggedIn && userStore.user?.username === props.article.author
})

const goToDetail = () => {
  router.push(`/article/${props.article.id}`)
}

const goToComments = () => {
  router.push({
    path: `/article/${props.article.id}`,
    query: { focus: 'comments' },
  })
}

const emitFavoriteUpdated = () => {
  window.dispatchEvent(new Event('favorites-updated'))
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (isFavorited.value) {
      const response = await favoriteApi.remove(props.article.id || 0)
      isFavorited.value = false
      favoriteCount.value = response.data.favoriteCount ?? Math.max(0, favoriteCount.value - 1)
      ElMessage.success('已取消收藏')
    } else {
      const response = await favoriteApi.add(props.article.id || 0)
      isFavorited.value = true
      favoriteCount.value = response.data.favoriteCount ?? favoriteCount.value + 1
      ElMessage.success('收藏成功')
    }

    emitFavoriteUpdated()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

const goToEdit = () => {
  router.push({
    path: '/upload',
    query: { id: props.article.id },
  })
}
</script>

<style scoped>
.article-card {
  margin-bottom: 16px;
  border-radius: 20px;
  border: 1px solid #eef0f4;
  cursor: pointer;
  transition: all 0.2s;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 650;
  margin: 8px 0;
  line-height: 1.4;
}

.card-meta {
  display: flex;
  gap: 18px;
  color: #6b7280;
  font-size: 13px;
  margin: 12px 0;
}

.card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  border-radius: 20px;
}

.action-icons {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.favorited {
  color: #f59e0b;
}
</style>
