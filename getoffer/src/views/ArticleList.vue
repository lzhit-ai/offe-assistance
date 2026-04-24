<template>
  <div>
    <Navbar />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="6" :md="5" class="sidebar-col">
        <CategorySidebar />
      </el-col>
      <el-col :xs="24" :sm="12" :md="14" class="content-col">
        <MobileCategoryDrawer />

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
      <el-col :xs="24" :sm="6" :md="5" class="hot-panel-col">
        <HotPanel />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { LocationQueryRaw } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import CategorySidebar from '@/components/CategorySidebar.vue'
import MobileCategoryDrawer from '@/components/MobileCategoryDrawer.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import HotPanel from '@/components/HotPanel.vue'
import { articleApi } from '@/api/frontend'
import type { ArticleItem, PageResult } from '@/api/transformers'
import {
  ARTICLE_LIST_PAGE_SIZE,
  parsePage,
  resetPageInQuery,
  withPageInQuery,
} from '@/utils/pagination'

const route = useRoute()
const router = useRouter()
const articles = ref<ArticleItem[]>([])
const loading = ref(false)
const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const pagination = ref<PageResult<ArticleItem>>({
  list: [],
  page: 1,
  pageSize: ARTICLE_LIST_PAGE_SIZE,
  total: 0,
  hasMore: false,
})

const activeTab = computed(() => (route.params.type === 'tech' ? 'tech' : 'interview'))

const getErrorMessage = (error: unknown, fallback: string) =>
  error instanceof Error ? error.message : fallback

const switchTab = (type: string) => {
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
      type: typeof route.params.type === 'string' ? route.params.type : undefined,
      keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
      category: typeof route.query.category === 'string' ? route.query.category : '',
      tag: typeof route.query.tag === 'string' ? route.query.tag : '',
      page: parsePage(route.query.page),
      pageSize: ARTICLE_LIST_PAGE_SIZE,
    })

    articles.value = response.data
    pagination.value = response.pagination
  } catch (error) {
    articles.value = []
    pagination.value = {
      list: [],
      page: 1,
      pageSize: ARTICLE_LIST_PAGE_SIZE,
      total: 0,
      hasMore: false,
    }
    ElMessage.error(getErrorMessage(error, '获取文章列表失败'))
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
    query: nextQuery as LocationQueryRaw,
  })
}

const handlePageChange = (page: number) => {
  router.push({
    name: 'articleList',
    params: { type: activeTab.value },
    query: withPageInQuery(route.query, page) as LocationQueryRaw,
  })
}

onMounted(fetchArticles)

watch(
  () => route.fullPath,
  () => {
    searchKeyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
    fetchArticles()
  },
)
</script>

<style scoped>
.sidebar-col {
  display: block;
}

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

@media (max-width: 1024px) {
  .hot-panel-col {
    display: none;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .content-col {
    flex: 0 0 75%;
    max-width: 75%;
  }
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

@media (max-width: 767px) {
  .sidebar-col {
    display: none;
  }
}
</style>
