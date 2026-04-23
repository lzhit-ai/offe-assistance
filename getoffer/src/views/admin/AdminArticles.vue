<template>
  <section class="admin-page">
    <div class="admin-page__toolbar">
      <el-input v-model="keyword" placeholder="按标题搜索" clearable @keyup.enter="loadArticles(1)" />
      <el-select v-model="status" class="admin-page__select" @change="loadArticles(1)">
        <el-option label="全部状态" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="loadArticles(1)">查询</el-button>
    </div>
    <el-table :data="articles" v-loading="loading" border>
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="authorName" label="作者" min-width="120" />
      <el-table-column prop="category" label="分类" min-width="120" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" :disabled="row.status === 'APPROVED'" @click="approveArticle(row.id)">
            通过
          </el-button>
          <el-button size="small" type="warning" :disabled="row.status === 'REJECTED'" @click="rejectArticle(row.id)">
            驳回
          </el-button>
          <el-button size="small" type="danger" @click="removeArticle(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="admin-page__pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @current-change="loadArticles"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/frontend'
import type { AdminArticleItem } from '@/api/transformers'

const loading = ref(false)
const keyword = ref('')
const status = ref('PENDING')
const articles = ref<AdminArticleItem[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0,
})

const getStatusLabel = (value: string) => {
  if (value === 'APPROVED') {
    return '已通过'
  }
  if (value === 'REJECTED') {
    return '已拒绝'
  }
  return '待审核'
}

const getStatusTagType = (value: string) => {
  if (value === 'APPROVED') {
    return 'success'
  }
  if (value === 'REJECTED') {
    return 'danger'
  }
  return 'warning'
}

const loadArticles = async (page = pagination.value.page) => {
  loading.value = true
  try {
    const response = await adminApi.getArticles({
      page,
      pageSize: pagination.value.pageSize,
      keyword: keyword.value.trim(),
      status: status.value,
    })
    articles.value = response.data
    pagination.value = response.pagination
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载文章审核列表失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const approveArticle = async (articleId?: number) => {
  if (!articleId) {
    return
  }

  try {
    await adminApi.approveArticle(articleId)
    ElMessage.success('文章已审核通过')
    await loadArticles()
  } catch (error) {
    const message = error instanceof Error ? error.message : '审核通过失败'
    ElMessage.error(message)
  }
}

const rejectArticle = async (articleId?: number) => {
  if (!articleId) {
    return
  }

  try {
    await adminApi.rejectArticle(articleId)
    ElMessage.success('文章已驳回')
    await loadArticles()
  } catch (error) {
    const message = error instanceof Error ? error.message : '驳回失败'
    ElMessage.error(message)
  }
}

const removeArticle = async (articleId?: number) => {
  if (!articleId) {
    return
  }

  try {
    await ElMessageBox.confirm('删除后无法恢复，确认继续吗？', '删除文章', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await adminApi.deleteArticle(articleId)
    ElMessage.success('文章已删除')
    await loadArticles()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    const message = error instanceof Error ? error.message : '删除失败'
    ElMessage.error(message)
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.admin-page__toolbar {
  display: flex;
  gap: 12px;
}

.admin-page__select {
  width: 160px;
}

.admin-page__pagination {
  display: flex;
  justify-content: flex-end;
}
</style>
