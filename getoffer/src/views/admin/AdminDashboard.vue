<template>
  <section class="admin-dashboard">
    <div class="admin-dashboard__cards">
      <el-card shadow="hover">
        <p class="card-label">用户总数</p>
        <strong>{{ dashboard.userCount }}</strong>
      </el-card>
      <el-card shadow="hover">
        <p class="card-label">文章总数</p>
        <strong>{{ dashboard.articleCount }}</strong>
      </el-card>
      <el-card shadow="hover">
        <p class="card-label">AI 会话数</p>
        <strong>{{ dashboard.aiSessionCount }}</strong>
      </el-card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/frontend'

const dashboard = reactive({
  userCount: 0,
  articleCount: 0,
  aiSessionCount: 0,
})

const loadDashboard = async () => {
  try {
    const response = await adminApi.getDashboard()
    Object.assign(dashboard, response.data)
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载后台概览失败'
    ElMessage.error(message)
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.admin-dashboard__cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.card-label {
  margin: 0 0 14px;
  color: #64748b;
}

.admin-dashboard strong {
  font-size: 36px;
  color: #0f172a;
}

@media (max-width: 960px) {
  .admin-dashboard__cards {
    grid-template-columns: 1fr;
  }
}
</style>
