<template>
  <section class="admin-page">
    <div class="admin-page__toolbar">
      <el-input v-model="keyword" placeholder="按用户名、昵称或邮箱筛选当前页" clearable />
    </div>
    <el-table :data="filteredUsers" v-loading="loading" border>
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="220" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="registerTime" label="注册时间" width="140" />
    </el-table>
    <div class="admin-page__pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @current-change="loadUsers"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/frontend'
import type { AdminUserItem } from '@/api/transformers'

const loading = ref(false)
const keyword = ref('')
const users = ref<AdminUserItem[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0,
})

const filteredUsers = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  if (!search) {
    return users.value
  }

  return users.value.filter((user) =>
    [user.username, user.nickname, user.email].some((field) => field.toLowerCase().includes(search)),
  )
})

const loadUsers = async (page = pagination.value.page) => {
  loading.value = true
  try {
    const response = await adminApi.getUsers({ page, pageSize: pagination.value.pageSize })
    users.value = response.data
    pagination.value = response.pagination
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载用户列表失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.admin-page__toolbar {
  display: flex;
  justify-content: flex-end;
}

.admin-page__pagination {
  display: flex;
  justify-content: flex-end;
}
</style>
