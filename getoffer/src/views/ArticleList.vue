<template>
  <div>
    <Navbar />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="6" :md="5">
        <CategorySidebar />
      </el-col>
      <el-col :xs="24" :sm="12" :md="14">
        <div class="list-header">
          <el-button-group>
            <el-button
              :type="activeTab === 'tech' ? 'primary' : 'default'"
              @click="switchTab('tech')"
              round
            >
              八股知识
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
            placeholder="搜索文章或关键词..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
        />

        <el-empty v-if="!loading && articles.length === 0" description="暂无文章" />
        <div v-if="loading" class="loading-placeholder">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
          <el-pagination
            background
            layout="prev, pager, next"
            :current-page="pagination.page"
            :page-size="pagination.pageSize"
            :total="pagination.total"
            @current-change="handlePageChange"
          />
        </div>
      </el-col>
      <el-col :xs="24" :sm="6" :md="5">
        <HotPanel />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import CategorySidebar from '@/components/CategorySidebar.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import HotPanel from '@/components/HotPanel.vue'
import { articleApi } from '@/api/frontend'
import {
  ARTICLE_LIST_PAGE_SIZE,
  parsePage,
  resetPageInQuery,
  withPageInQuery,
} from '@/utils/pagination'

const route = useRoute()
const router = useRouter()
const articles = ref([])
const loading = ref(false)
const searchKeyword = ref(route.query.keyword || '')
const pagination = ref({
  page: 1,
  pageSize: ARTICLE_LIST_PAGE_SIZE,
  total: 0,
})

const activeTab = computed(() => (route.params.type === 'tech' ? 'tech' : 'interview'))

const switchTab = (type) => {
  router.push({
    name: 'articleList',
    params: { type },
    query: {},
  })
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const response = await articleApi.getList({
      type: route.params.type,
      keyword: route.query.keyword || '',
      category: route.query.category || '',
      tag: route.query.tag || '',
      page: parsePage(route.query.page),
      pageSize: ARTICLE_LIST_PAGE_SIZE,
    })

    articles.value = response.data
    pagination.value = response.pagination
  } catch (error) {
    articles.value = []
    pagination.value = {
      page: 1,
      pageSize: ARTICLE_LIST_PAGE_SIZE,
      total: 0,
    }
    ElMessage.error(error.message || '获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  const nextQuery = resetPageInQuery({ ...route.query })

  if (searchKeyword.value.trim()) {
    nextQuery.keyword = searchKeyword.value.trim()
  } else {
    delete nextQuery.keyword
  }

  router.push({
    name: 'articleList',
    params: { type: activeTab.value },
    query: nextQuery,
  })
}

const handlePageChange = (page) => {
  router.push({
    name: 'articleList',
    params: { type: activeTab.value },
    query: withPageInQuery(route.query, page),
  })
}

onMounted(fetchArticles)

watch(
  () => route.fullPath,
  () => {
    searchKeyword.value = route.query.keyword || ''
    fetchArticles()
  },
)
</script>

<style scoped>
.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-input {
  width: 260px;
}

.loading-placeholder {
  margin-top: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin: 24px 0 8px;
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    gap: 12px;
  }

  .search-input {
    width: 100%;
  }
}
</style>
