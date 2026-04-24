<template>
  <div class="category-sidebar">
    <el-menu
      :default-active="activeCategory"
      class="category-menu"
      @select="handleSelect"
    >
      <div class="category-title">技术方向</div>
      <el-menu-item
        v-for="cat in categories"
        :key="cat.name"
        :index="cat.name"
      >
        <span class="category-name">{{ cat.name }}</span>
        <el-badge :value="cat.count" class="count-badge" type="info" />
      </el-menu-item>
    </el-menu>

    <div class="tags-section">
      <div class="category-title">常用标签</div>
      <div class="tag-cloud">
        <el-tag
          v-for="tag in hotTags"
          :key="tag"
          class="tag-item"
          @click="selectTag(tag)"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { metadataApi } from '@/api/frontend'
import { resetPageInQuery } from '@/utils/pagination'

const fallbackCategories = [
  { name: '前端框架', count: 0 },
  { name: 'JavaScript/ES6/TypeScript', count: 0 },
  { name: 'HTML&CSS', count: 0 },
  { name: '计算机网络', count: 0 },
  { name: '数据结构与算法', count: 0 },
  { name: '前端纵向领域', count: 0 },
]

const fallbackTags = ['JavaScript', 'Vue', 'Promise', 'HTTP', '链表', '性能优化']

const router = useRouter()
const route = useRoute()
const activeCategory = ref(route.query.category || fallbackCategories[0].name)
const categories = ref([...fallbackCategories])
const hotTags = ref([...fallbackTags])

const resolveRouteType = () => route.params.type || 'tech'

const loadMetadata = async () => {
  try {
    const [categoryResponse, tagResponse] = await Promise.all([
      metadataApi.getCategories(),
      metadataApi.getHotTags(10),
    ])

    if (categoryResponse.data.length > 0) {
      categories.value = categoryResponse.data
    }

    if (tagResponse.data.length > 0) {
      hotTags.value = tagResponse.data.map((item) => item.name)
    }
  } catch (error) {
    console.error('load sidebar metadata failed', error)
  }
}

const handleSelect = (name) => {
  activeCategory.value = name
  router.push({
    name: 'articleList',
    params: { type: resolveRouteType() },
    query: resetPageInQuery({
      ...route.query,
      category: name,
    }),
  })
}

const selectTag = (tag) => {
  router.push({
    name: 'articleList',
    params: { type: resolveRouteType() },
    query: resetPageInQuery({
      ...route.query,
      tag,
    }),
  })
}

onMounted(loadMetadata)

watch(
  () => route.query.category,
  (value) => {
    activeCategory.value = value || fallbackCategories[0].name
  },
)
</script>

<style scoped>
.category-sidebar {
  background: white;
  border-radius: 24px;
  padding: 20px 0;
  border: 1px solid #eef0f4;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.02);
  height: fit-content;
}

.category-menu {
  border-right: none;
}

.category-title {
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6b7280;
  margin: 0 0 12px 16px;
}

.category-menu .el-menu-item {
  min-height: 48px;
  height: auto;
  line-height: 1.4;
  margin: 4px 12px;
  border-radius: 14px;
  font-weight: 500;
  color: #3c3f4a;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding-top: 12px;
  padding-bottom: 12px;
  white-space: normal;
}

.category-menu .el-menu-item.is-active {
  background: #eef2ff;
  color: #2563eb;
}

.category-name {
  flex: 1;
  min-width: 0;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.count-badge {
  margin-left: auto;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: none;
  margin-top: 2px;
}

.tags-section {
  padding: 20px 16px;
  border-top: 1px solid #f0f2f5;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
  border-radius: 20px;
}
</style>
