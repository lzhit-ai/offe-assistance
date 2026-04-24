<template>
  <div class="mobile-category-entry">
    <el-button class="mobile-category-trigger" round @click="drawerVisible = true">
      分类
    </el-button>

    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="280px"
      class="mobile-category-drawer"
      title="技术分类"
    >
      <div class="mobile-category-content">
        <section class="mobile-category-section">
          <h3 class="section-title">技术方向</h3>
          <div class="mobile-category-list">
            <el-button
              v-for="category in visibleCategories"
              :key="category.name"
              class="mobile-category-item"
              :type="activeCategory === category.name ? 'primary' : 'default'"
              round
              @click="selectCategory(category.name)"
            >
              {{ category.name }}
            </el-button>
          </div>
        </section>

        <section class="mobile-category-section">
          <h3 class="section-title">常用标签</h3>
          <div class="mobile-category-list">
            <el-tag
              v-for="tag in visibleTags"
              :key="tag"
              class="mobile-tag-item"
              :type="activeTag === tag ? 'primary' : 'info'"
              effect="plain"
              @click="selectTag(tag)"
            >
              {{ tag }}
            </el-tag>
          </div>
        </section>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { LocationQueryRaw } from 'vue-router'
import { metadataApi } from '@/api/frontend'
import type { MetadataItem } from '@/api/transformers'
import { resetPageInQuery } from '@/utils/pagination'

interface Props {
  defaultType?: 'tech' | 'interview'
}

const props = withDefaults(defineProps<Props>(), {
  defaultType: 'tech',
})

const fallbackCategories: MetadataItem[] = [
  { name: '前端框架' },
  { name: 'JavaScript/ES6/TypeScript' },
  { name: 'HTML&CSS' },
  { name: '计算机网络' },
  { name: '数据结构与算法' },
  { name: '前端纵向领域' },
]

const fallbackTags = ['JavaScript', 'Vue', 'Promise', 'HTTP', '链表', '性能优化']

const route = useRoute()
const router = useRouter()

const drawerVisible = ref(false)
const categories = ref<MetadataItem[]>([...fallbackCategories])
const hotTags = ref<string[]>([...fallbackTags])

const activeCategory = computed(() =>
  typeof route.query.category === 'string' ? route.query.category : '',
)

const activeTag = computed(() =>
  typeof route.query.tag === 'string' ? route.query.tag : '',
)

const visibleCategories = computed(() => categories.value.slice(0, 6))
const visibleTags = computed(() => hotTags.value.slice(0, 8))

const resolveArticleType = (): 'tech' | 'interview' => {
  if (route.params.type === 'interview') {
    return 'interview'
  }

  return props.defaultType
}

const loadMetadata = async () => {
  try {
    const [categoryResponse, tagResponse] = await Promise.all([
      metadataApi.getCategories(),
      metadataApi.getHotTags(12),
    ])

    if (categoryResponse.data.length > 0) {
      categories.value = categoryResponse.data
    }

    if (tagResponse.data.length > 0) {
      hotTags.value = tagResponse.data.map((item) => item.name)
    }
  } catch (error) {
    console.error('load mobile category metadata failed', error)
  }
}

const selectCategory = async (category: string) => {
  drawerVisible.value = false
  await router.push({
    name: 'articleList',
    params: { type: resolveArticleType() },
    query: resetPageInQuery({
      ...route.query,
      category,
    }) as LocationQueryRaw,
  })
}

const selectTag = async (tag: string) => {
  drawerVisible.value = false
  await router.push({
    name: 'articleList',
    params: { type: resolveArticleType() },
    query: resetPageInQuery({
      ...route.query,
      tag,
    }) as LocationQueryRaw,
  })
}

onMounted(loadMetadata)
</script>

<style scoped>
.mobile-category-entry {
  display: none;
  margin-bottom: 16px;
}

.mobile-category-trigger {
  border-color: #dbe3f1;
  background: #fff;
  color: #1f2937;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
}

.mobile-category-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mobile-category-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.mobile-category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.mobile-category-item {
  margin: 0;
}

.mobile-tag-item {
  cursor: pointer;
}

@media (max-width: 767px) {
  .mobile-category-entry {
    display: block;
  }
}
</style>
