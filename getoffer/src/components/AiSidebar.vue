<template>
  <aside class="ai-sidebar">
    <div class="sidebar-brand">
      <div class="brand-mark">AI</div>
      <div class="brand-copy">
        <strong>面经 AI</strong>
        <span>DeepSeek 对话</span>
      </div>
    </div>

    <div class="sidebar-actions">
      <el-button class="new-chat-btn" round @click="emit('create')" :loading="creating">
        开启新对话
      </el-button>
    </div>

    <el-scrollbar class="session-scroll">
      <div v-if="loading" class="session-skeletons">
        <el-skeleton v-for="index in 5" :key="index" animated>
          <template #template>
            <el-skeleton-item variant="text" style="width: 75%; height: 16px" />
            <el-skeleton-item variant="text" style="width: 100%; margin-top: 10px" />
          </template>
        </el-skeleton>
      </div>

      <div v-else-if="!sessions.length" class="empty-state">
        <el-empty description="还没有历史对话" />
      </div>

      <div v-else class="session-list">
        <article
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === activeSessionId }"
          @click="emit('select', session.id)"
        >
          <div class="session-head">
            <h4 class="session-title">{{ session.title }}</h4>
            <el-popconfirm
              title="确定删除这条历史记录吗？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="emit('delete', session.id)"
            >
              <template #reference>
                <button
                  type="button"
                  class="session-delete"
                  title="删除历史记录"
                  @click.stop
                >
                  ×
                </button>
              </template>
            </el-popconfirm>
          </div>
          <p class="session-preview">
            {{ session.lastMessagePreview || '暂无消息' }}
          </p>
          <span class="session-time">{{ session.updatedAt || session.createdAt }}</span>
        </article>
      </div>
    </el-scrollbar>
  </aside>
</template>

<script setup>
defineProps({
  sessions: {
    type: Array,
    default: () => [],
  },
  activeSessionId: {
    type: [Number, String, null],
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  creating: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['create', 'select', 'delete'])
</script>

<style scoped>
.ai-sidebar {
  height: 100%;
  min-height: var(--ai-panel-min-height);
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  background: #f5f7fb;
  border-right: 1px solid #e5e7eb;
  overflow: hidden;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 20px 14px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-copy strong {
  font-size: 17px;
  color: #1f2937;
}

.brand-copy span {
  font-size: 12px;
  color: #94a3b8;
}

.sidebar-actions {
  padding: 8px 16px 14px;
}

.new-chat-btn {
  width: 100%;
  height: 42px;
  border-color: #e5e7eb;
  background: #fff;
  color: #1f2937;
}

.session-scroll {
  min-height: 0;
  padding: 0 12px 16px;
}

.session-skeletons {
  display: grid;
  gap: 10px;
}

.session-list {
  display: grid;
  gap: 8px;
}

.session-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 12px 10px;
  border-radius: 16px;
  border: 1px solid transparent;
  background: transparent;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.78);
}

.session-item.active {
  background: #eaf1ff;
  border-color: #c7d7fe;
}

.session-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.session-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  color: #1f2937;
}

.session-delete {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #94a3b8;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}

.session-delete:hover {
  background: rgba(148, 163, 184, 0.14);
  color: #475569;
}

.session-preview {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.session-time {
  font-size: 12px;
  color: #94a3b8;
}

.empty-state {
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.session-scroll .el-scrollbar__wrap) {
  overscroll-behavior: contain;
}

@media (max-width: 1180px) {
  .ai-sidebar {
    min-height: 320px;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
}
</style>
