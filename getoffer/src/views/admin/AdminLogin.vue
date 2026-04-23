<template>
  <section class="admin-login">
    <div class="admin-login__card">
      <p class="admin-login__eyebrow">Admin Portal</p>
      <h1>后台登录</h1>
      <p class="admin-login__hint">仅管理员可以从这里进入审核与管理工作台。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="admin-login__submit" :loading="submitting" @click="handleLogin">
          登录后台
        </el-button>
      </el-form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api/frontend'
import { useUserStore } from '@/stores/admin'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true
    const response = await authApi.login(form)

    if (response.data.user.role !== 'ADMIN') {
      userStore.logout()
      ElMessage.error('当前账号没有后台权限')
      return
    }

    userStore.login(response.data.accessToken, response.data.user)
    ElMessage.success('欢迎进入后台')
    router.push('/admin/dashboard')
  } catch (error) {
    const message = error instanceof Error ? error.message : '后台登录失败'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at top, rgba(14, 165, 233, 0.18), transparent 32%),
    linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
}

.admin-login__card {
  width: min(420px, 100%);
  padding: 32px 28px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
}

.admin-login__eyebrow {
  margin: 0 0 8px;
  color: #0369a1;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.admin-login__hint {
  margin: 12px 0 20px;
  color: #475569;
  line-height: 1.6;
}

.admin-login__submit {
  width: 100%;
  margin-top: 12px;
}
</style>
