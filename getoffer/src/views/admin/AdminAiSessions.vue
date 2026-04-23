<template>
  <section class="admin-page">
    <el-table :data="sessions" v-loading="loading" border>
      <el-table-column prop="id" label="会话 ID" width="100" />
      <el-table-column prop="username" label="所属用户" min-width="140" />
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
    </el-table>
    <div class="admin-page__pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @current-change="loadSessions"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/frontend'
import type { AdminAiSessionItem } from '@/api/transformers'

const loading = ref(false)
const sessions = ref<AdminAiSessionItem[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0,
})

const loadSessions = async (page = pagination.value.page) => {
  loading.value = true
  try {
    const response = await adminApi.getAiSessions({ page, pageSize: pagination.value.pageSize })
    sessions.value = response.data
    pagination.value = response.pagination
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载 AI 会话列表失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.admin-page__pagination {
  display: flex;
  justify-content: flex-end;
}
</style>
