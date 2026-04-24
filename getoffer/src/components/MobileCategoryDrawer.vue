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
        <p class="mobile-category-hint">只保留常用方向，方便手机端快速筛选。</p>
        <div class="mobile-category-list">
          <el-button
            v-for="category in importantCategories"
            :key="category"
            class="mobile-category-item"
            :type="activeCategory === category ? 'primary' : 'default'"
            round
            @click="selectCategory(category)"
          >
            {{ category }}
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { LocationQueryRaw } from 'vue-router'
import { resetPageInQuery } from '@/utils/pagination'

interface Props {
  defaultType?: 'tech' | 'interview'
}

const props = withDefaults(defineProps<Props>(), {
  defaultType: 'tech',
})

const route = useRoute()
const router = useRouter()

const drawerVisible = ref(false)
const importantCategories = ['前端', '后端', '算法', 'Java', 'Vue', 'Spring', 'MySQL']

const activeCategory = computed(() =>
  typeof route.query.category === 'string' ? route.query.category : '',
)

const resolveArticleType = (): 'tech' | 'interview' => {
  if (route.params.type === 'interview') {
    return 'interview'
  }

  return props.defaultType
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
  gap: 16px;
}

.mobile-category-hint {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

.mobile-category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.mobile-category-item {
  margin: 0;
}

@media (max-width: 767px) {
  .mobile-category-entry {
    display: block;
  }
}
</style>
