<template>
  <div>
    <Navbar />
    <el-card class="detail-container" shadow="never" v-loading="loading">
      <template v-if="article">
        <h1>{{ article.title }}</h1>
        <div class="meta">
          <span><el-icon><User /></el-icon> {{ article.author }}</span>
          <span><el-icon><Calendar /></el-icon> {{ article.createdAt }}</span>
          <span><el-icon><View /></el-icon> {{ article.viewCount }} 浏览</span>
        </div>
        <div class="tags">
          <el-tag v-for="tag in article.tags" :key="tag" size="small">{{ tag }}</el-tag>
        </div>
        <el-divider />
        <div class="content markdown-body" v-html="renderedContent"></div>
        <el-divider />
        <div class="actions">
          <el-button
            :type="isFavorited ? 'default' : 'primary'"
            @click="toggleFavorite"
            :icon="Star"
          >
            {{ isFavorited ? '取消收藏' : '收藏' }}（{{ article.favoriteCount || 0 }}）
          </el-button>
          <el-button
            :type="isLiked ? 'default' : 'success'"
            @click="toggleLike"
            :icon="Pointer"
          >
            {{ isLiked ? '取消点赞' : '点赞' }}（{{ article.likeCount || 0 }}）
          </el-button>
        </div>
      </template>
      <el-empty v-else description="文章不存在" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { User, Calendar, View, Star, Pointer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import Navbar from '@/components/Navbar.vue'
import { articleApi, favoriteApi, likeApi } from '@/api/frontend'
import type { ArticleItem } from '@/api/transformers'
import { useUserStore } from '@/stores/admin'

const route = useRoute()
const userStore = useUserStore()
const article = ref<ArticleItem | null>(null)
const loading = ref(true)
const isFavorited = ref(false)
const isLiked = ref(false)
const articleId = computed(() => (Array.isArray(route.params.id) ? route.params.id[0] : route.params.id))

const renderedContent = computed(() => {
  return article.value ? marked(article.value.content || '') : ''
})

const emitFavoriteUpdated = () => {
  window.dispatchEvent(new Event('favorites-updated'))
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const response = await articleApi.getDetail(articleId.value)
    article.value = response.data
    isFavorited.value = Boolean(response.data?.isFavorited)
    isLiked.value = Boolean(response.data?.liked)
  } catch (error) {
    article.value = null
    const message = error instanceof Error ? error.message : '获取文章详情失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (isFavorited.value) {
      const response = await favoriteApi.remove(Number(articleId.value))
      isFavorited.value = false
      if (article.value) {
        article.value.favoriteCount = response.data.favoriteCount ?? Math.max(0, article.value.favoriteCount - 1)
      }
      ElMessage.success('已取消收藏')
    } else {
      const response = await favoriteApi.add(Number(articleId.value))
      isFavorited.value = true
      if (article.value) {
        article.value.favoriteCount = response.data.favoriteCount ?? article.value.favoriteCount + 1
      }
      ElMessage.success('收藏成功')
    }

    emitFavoriteUpdated()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (isLiked.value) {
      const response = await likeApi.remove(Number(articleId.value))
      isLiked.value = false
      if (article.value) {
        article.value.likeCount = response.data.likeCount ?? Math.max(0, article.value.likeCount - 1)
      }
      ElMessage.success('已取消点赞')
    } else {
      const response = await likeApi.add(Number(articleId.value))
      isLiked.value = true
      if (article.value) {
        article.value.likeCount = response.data.likeCount ?? article.value.likeCount + 1
      }
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

onMounted(fetchArticle)

watch(
  () => route.params.id,
  () => {
    fetchArticle()
  },
)
</script>

<style scoped>
.detail-container {
  max-width: 900px;
  margin: 0 auto;
  border-radius: 24px;
  padding: 24px;
}

.meta {
  display: flex;
  gap: 24px;
  color: #6b7280;
  margin: 16px 0;
}

.meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.content {
  line-height: 1.8;
  padding: 16px 0;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}
</style>
