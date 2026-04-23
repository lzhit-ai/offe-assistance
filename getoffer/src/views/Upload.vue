<template>
  <div>
    <Navbar />
    <el-card class="upload-container" shadow="never">
      <template #header>
        <div class="upload-header">
          <h2>{{ isEditMode ? '编辑文章' : '上传文章' }}</h2>
          <p v-if="!isEditMode">文章提交后会进入后台审核，审核通过后才会在前台展示。</p>
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="left"
        v-loading="loading"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类">
                <el-option
                  v-for="category in categories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio :value="1">八股文</el-radio>
                <el-radio :value="2">面试经历</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签"
          >
            <el-option
              v-for="tag in presetTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="支持 Markdown 格式"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ isEditMode ? '更新' : '提交审核' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import { articleApi, metadataApi } from '@/api/frontend'
import { useUserStore } from '@/stores/admin'

const fallbackCategories = ['前端', '后端', 'Agent开发', '客户端', '游戏开发', 'C++', '数据库', '网络/OS']
const fallbackTags = ['Vue3', 'React', 'SpringBoot', 'Java', 'LeetCode', '系统设计', 'Redis', 'MySQL']

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const categories = ref([...fallbackCategories])
const presetTags = ref([...fallbackTags])

const form = reactive<{
  title: string
  category: string
  type: number
  tags: string[]
  content: string
  id: number | null
}>({
  title: '',
  category: fallbackCategories[0],
  type: 1,
  tags: [],
  content: '',
  id: null,
})

const rules: FormRules<typeof form> = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const getRouteArticleId = () => {
  const rawId = route.query.id
  if (Array.isArray(rawId)) {
    return rawId[0]
  }
  return rawId || null
}

const isEditMode = computed(() => Boolean(getRouteArticleId()))

const loadMetadata = async () => {
  try {
    const [categoryResponse, tagResponse] = await Promise.all([
      metadataApi.getCategories(),
      metadataApi.getHotTags(20),
    ])

    const categoryNames = categoryResponse.data.map((item) => item.name).filter(Boolean)
    if (categoryNames.length > 0) {
      categories.value = categoryNames
      if (!categoryNames.includes(form.category)) {
        form.category = categoryNames[0]
      }
    }

    const tags = tagResponse.data.map((item) => item.name).filter(Boolean)
    if (tags.length > 0) {
      presetTags.value = [...new Set([...tags, ...fallbackTags])]
    }
  } catch (error) {
    console.error('load metadata failed', error)
  }
}

const loadArticleData = async () => {
  if (!isEditMode.value) {
    return
  }

  loading.value = true
  try {
    const response = await articleApi.getDetail(getRouteArticleId())
    const article = response.data

    if (!article.canEdit) {
      ElMessage.error('你无权编辑这篇文章')
      router.back()
      return
    }

    form.id = article.id || null
    form.title = article.title
    form.category = article.category || categories.value[0]
    form.type = article.type || 1
    form.tags = article.tags || []
    form.content = article.content || ''
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载文章失败，请稍后重试'
    ElMessage.error(message)
    router.back()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/')
    return
  }

  await loadMetadata()
  await loadArticleData()
})

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    const articleData = {
      title: form.title,
      category: form.category,
      type: form.type,
      tags: form.tags,
      content: form.content,
    }

    if (isEditMode.value) {
      await articleApi.update(form.id || undefined, articleData)
      ElMessage.success('更新成功')
    } else {
      await articleApi.create(articleData)
      ElMessage.success('文章已提交审核')
    }

    router.push('/')
  } catch (error) {
    const fallbackMessage = isEditMode.value ? '更新失败' : '提交审核失败'
    const message = error instanceof Error ? error.message : fallbackMessage
    ElMessage.error(message || fallbackMessage)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.upload-container {
  max-width: 900px;
  margin: 0 auto;
  border-radius: 24px;
}

.upload-header p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}
</style>
