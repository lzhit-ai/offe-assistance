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

        <section ref="commentSectionRef" class="comment-section">
          <div class="comment-header">
            <h2>评论区</h2>
            <span>{{ article.commentCount || 0 }} 条评论</span>
          </div>

          <div v-if="userStore.isLoggedIn" class="comment-editor">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="写下你的评论吧"
            />
            <div class="comment-editor-actions">
              <el-button
                type="primary"
                :loading="commentSubmitting"
                @click="submitComment"
              >
                发表评论
              </el-button>
            </div>
          </div>
          <el-alert
            v-else
            type="info"
            :closable="false"
            show-icon
            title="登录后可评论"
          />

          <div v-loading="commentsLoading" class="comment-list">
            <el-empty
              v-if="!commentsLoading && comments.length === 0"
              description="还没有评论，来抢沙发吧"
            />
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :src="resolveAvatarUrl(comment.author.avatar)">
                {{ getAvatarFallback(comment.author) }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-top">
                  <div class="comment-user">
                    <strong>{{ getDisplayName(comment.author) }}</strong>
                    <span>{{ comment.createdAt }}</span>
                  </div>
                  <el-button
                    v-if="comment.canDelete"
                    text
                    type="danger"
                    @click="deleteComment(comment.id)"
                  >
                    删除
                  </el-button>
                </div>
                <p>{{ comment.content }}</p>
              </div>
            </div>
          </div>
        </section>
      </template>
      <el-empty v-else description="文章不存在" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { User, Calendar, View, Star, Pointer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import Navbar from '@/components/Navbar.vue'
import { articleApi, commentApi, favoriteApi, likeApi } from '@/api/frontend'
import type { ArticleCommentItem, ArticleItem } from '@/api/transformers'
import { useUserStore } from '@/stores/admin'
import { getAvatarFallback, getDisplayName, resolveAvatarUrl } from '@/utils/user-profile'

const route = useRoute()
const userStore = useUserStore()
const article = ref<ArticleItem | null>(null)
const comments = ref<ArticleCommentItem[]>([])
const loading = ref(true)
const commentsLoading = ref(false)
const commentSubmitting = ref(false)
const isFavorited = ref(false)
const isLiked = ref(false)
const commentContent = ref('')
const commentSectionRef = ref<HTMLElement | null>(null)
const articleId = computed(() => (Array.isArray(route.params.id) ? route.params.id[0] : route.params.id))
const routeFocus = computed(() => (Array.isArray(route.query.focus) ? route.query.focus[0] : route.query.focus))

const renderedContent = computed(() => {
  return article.value ? marked(article.value.content || '') : ''
})

const emitFavoriteUpdated = () => {
  window.dispatchEvent(new Event('favorites-updated'))
}

const focusCommentsSection = async () => {
  if (routeFocus.value !== 'comments') {
    return
  }

  await nextTick()
  commentSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const loadComments = async () => {
  commentsLoading.value = true
  try {
    const response = await commentApi.getList(Number(articleId.value))
    comments.value = response.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取评论失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    commentsLoading.value = false
  }
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const response = await articleApi.getDetail(articleId.value)
    article.value = response.data
    isFavorited.value = Boolean(response.data?.isFavorited)
    isLiked.value = Boolean(response.data?.liked)
    await loadComments()
    await focusCommentsSection()
  } catch (error) {
    article.value = null
    comments.value = []
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

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  const content = commentContent.value.trim()
  if (!content) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  if (content.length > 500) {
    ElMessage.warning('评论内容不能超过 500 个字符')
    return
  }

  commentSubmitting.value = true
  try {
    const response = await commentApi.add(Number(articleId.value), { content })
    comments.value = [response.data, ...comments.value]
    commentContent.value = ''
    if (article.value) {
      article.value.commentCount += 1
    }
    ElMessage.success('评论成功')
    await focusCommentsSection()
  } catch (error) {
    const message = error instanceof Error ? error.message : '发表评论失败'
    ElMessage.error(message)
  } finally {
    commentSubmitting.value = false
  }
}

const deleteComment = async (commentId?: number) => {
  if (!commentId) {
    return
  }

  try {
    await ElMessageBox.confirm('确认删除这条评论吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    const response = await commentApi.remove(Number(articleId.value), commentId)
    comments.value = comments.value.filter(comment => comment.id !== commentId)
    if (article.value) {
      article.value.commentCount = response.data.commentCount ?? Math.max(0, article.value.commentCount - 1)
    }
    ElMessage.success('评论已删除')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }

    const message = error instanceof Error ? error.message : '删除评论失败'
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

watch(
  () => route.query.focus,
  () => {
    focusCommentsSection()
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

.comment-section {
  margin-top: 32px;
  scroll-margin-top: 96px;
}

.comment-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.comment-header h2 {
  margin: 0;
}

.comment-header span {
  color: #6b7280;
  font-size: 14px;
}

.comment-editor {
  margin-bottom: 20px;
}

.comment-editor-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #edf0f4;
  border-radius: 16px;
  background: #fff;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.comment-user span {
  color: #6b7280;
  font-size: 13px;
}

.comment-body p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #1f2937;
}
</style>
