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
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
            {{ row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="registerTime" label="注册时间" width="140" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.role === 'ADMIN' ? 'warning' : 'primary'"
            :disabled="isCurrentAdmin(row)"
            @click="confirmUpdateRole(row)"
          >
            {{ isCurrentAdmin(row) ? '不可修改自己' : getActionLabel(row) }}
          </el-button>
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
        @current-change="loadUsers"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/frontend'
import type { AdminUserItem } from '@/api/transformers'
import { useUserStore } from '@/stores/admin'

const loading = ref(false)
const keyword = ref('')
const users = ref<AdminUserItem[]>([])
const userStore = useUserStore()
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0,
})
const currentAdminId = computed(() => userStore.user?.id)

const filteredUsers = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  if (!search) {
    return users.value
  }

  return users.value.filter((user) =>
    [user.username, user.nickname, user.email].some((field) => field.toLowerCase().includes(search)),
  )
})

const isCurrentAdmin = (user: AdminUserItem) => user.id === currentAdminId.value

const getActionLabel = (user: AdminUserItem) => (user.role === 'ADMIN' ? '降为普通用户' : '设为管理员')

const getTargetRole = (user: AdminUserItem): 'USER' | 'ADMIN' => (user.role === 'ADMIN' ? 'USER' : 'ADMIN')

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

const confirmUpdateRole = async (user: AdminUserItem) => {
  if (!user.id || isCurrentAdmin(user)) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认将用户“${user.nickname || user.username}”${getActionLabel(user)}吗？`,
      '调整用户权限',
      {
        type: 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消',
      },
    )
    await adminApi.updateUserRole(user.id, getTargetRole(user))
    ElMessage.success('权限已更新')
    await loadUsers(pagination.value.page)
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    const message = error instanceof Error ? error.message : '权限调整失败，请稍后重试'
    ElMessage.error(message)
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
