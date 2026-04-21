<template>
  <div>
    <Navbar />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="6" :md="5" :lg="5">
        <CategorySidebar />
      </el-col>
      <el-col :xs="24" :sm="12" :md="14" :lg="14">
        <div class="feed-header">
          <el-button-group>
            <el-button
              :type="activeTab === 'tech' ? 'primary' : 'default'"
              @click="switchTab('tech')"
              round
            >
              八股文
            </el-button>
            <el-button
              :type="activeTab === 'interview' ? 'primary' : 'default'"
              @click="switchTab('interview')"
              round
            >
              面试经历
            </el-button>
          </el-button-group>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索八股、面经或关键词..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <ArticleCard
          v-for="article in hotArticles"
          :key="article.id"
          :article="article"
        />

        <el-card class="ai-search-card" shadow="hover">
          <div class="ai-search-content">
            <el-tag type="primary" effect="dark">AI 助手</el-tag>
            <span>登录后就能体验 DeepSeek 自由聊天，会自动保存会话和消息记录。</span>
            <el-button type="primary" link @click="$router.push('/ai')">立即体验</el-button>
          </div>
        </el-card>

        <div class="upload-entry">
          <el-button type="primary" round @click="$router.push('/upload')">
            + 上传八股 / 分享面经
          </el-button>
        </div>
      </el-col>
      <el-col :xs="24" :sm="6" :md="5" :lg="5">
        <HotPanel />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import CategorySidebar from '@/components/CategorySidebar.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import HotPanel from '@/components/HotPanel.vue'
import { articleApi } from '@/api/frontend'

const router = useRouter()
const activeTab = ref('tech')
const searchKeyword = ref('')
const hotArticles = ref([])

const switchTab = (type) => {
  activeTab.value = type
  router.push(`/articles/${type}`)
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    return
  }

  router.push({
    name: 'articleList',
    params: { type: activeTab.value },
    query: { keyword: searchKeyword.value },
  })
}

onMounted(async () => {
  try {
    const response = await articleApi.getHot({ limit: 5 })
    hotArticles.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '获取热门文章失败，请稍后重试')
  }
})
</script>

<style scoped>
.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.ai-search-card {
  margin: 20px 0;
  border-radius: 16px;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
}

.ai-search-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.upload-entry {
  margin-top: 24px;
  text-align: right;
}

@media (max-width: 768px) {
  .feed-header {
    flex-direction: column;
    gap: 12px;
  }

  .search-input {
    width: 100%;
  }
}
</style>
