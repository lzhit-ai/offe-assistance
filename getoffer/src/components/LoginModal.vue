<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isLogin ? '登录' : '注册'"
    width="400px"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>

      <el-form-item v-if="!isLogin" label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
        />
      </el-form-item>

      <el-form-item v-if="!isLogin" label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          show-password
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isLogin ? '登录' : '注册' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/frontend'
import { useUserStore } from '@/stores/admin'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  isLogin: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:visible', 'login-success'])

const dialogVisible = ref(props.visible)
const loading = ref(false)
const formRef = ref()
const userStore = useUserStore()

const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const resetForm = () => {
  form.username = ''
  form.phone = ''
  form.password = ''
  form.confirmPassword = ''
  formRef.value?.resetFields()
}

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 之间', trigger: 'blur' },
  ],
  phone: props.isLogin
    ? []
    : [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
      ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: props.isLogin
    ? []
    : [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
          validator: (_rule, value, callback) => {
            if (value !== form.password) {
              callback(new Error('两次输入的密码不一致'))
              return
            }
            callback()
          },
          trigger: 'blur',
        },
      ],
}))

watch(
  () => props.visible,
  (value) => {
    dialogVisible.value = value
  },
)

watch(dialogVisible, (value) => {
  emit('update:visible', value)
  if (!value) {
    resetForm()
  }
})

watch(
  () => props.isLogin,
  () => {
    resetForm()
  },
)

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    const response = props.isLogin
      ? await authApi.login({
          username: form.username,
          password: form.password,
        })
      : await authApi.register({
          username: form.username,
          phone: form.phone,
          password: form.password,
        })

    userStore.login(response.data.accessToken, response.data.user)
    ElMessage.success(props.isLogin ? '登录成功' : '注册成功')
    dialogVisible.value = false
    emit('login-success', response.data.user)
  } catch (error) {
    ElMessage.error(error.message || (props.isLogin ? '登录失败' : '注册失败'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
