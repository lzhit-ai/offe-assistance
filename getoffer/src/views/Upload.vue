<template>
  <div>
    <Navbar />
    <el-card class="upload-container" shadow="never">
      <template #header>
        <h2>{{ isEditMode ? '编辑文章' : '上传文章' }}</h2>
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
            {{ isEditMode ? '更新' : '发布' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
const formRef = ref()
const categories = ref([...fallbackCategories])
const presetTags = ref([...fallbackTags])

const form = reactive({
  title: '',
  category: fallbackCategories[0],
  type: 1,
  tags: [],
  content: '',
  id: null,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const isEditMode = computed(() => Boolean(route.query.id))

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
    const response = await articleApi.getDetail(route.query.id)
    const article = response.data

    if (!article.canEdit) {
      ElMessage.error('你无权编辑这篇文章')
      router.back()
      return
    }

    form.id = article.id
    form.title = article.title
    form.category = article.category || categories.value[0]
    form.type = article.type || 1
    form.tags = article.tags || []
    form.content = article.content || ''
  } catch (error) {
    ElMessage.error(error.message || '加载文章失败，请稍后重试')
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
    await formRef.value.validate()
    submitLoading.value = true

    const articleData = {
      title: form.title,
      category: form.category,
      type: form.type,
      tags: form.tags,
      content: form.content,
    }

    if (isEditMode.value) {
      await articleApi.update(form.id, articleData)
      ElMessage.success('更新成功')
    } else {
      await articleApi.create(articleData)
      ElMessage.success('发布成功')
    }

    router.push('/')
  } catch (error) {
    ElMessage.error(error.message || (isEditMode.value ? '更新失败' : '发布失败'))
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
</style>
