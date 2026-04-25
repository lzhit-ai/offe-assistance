<template>
  <div class="ai-page">
    <Navbar />

    <div class="ai-shell" :style="layoutVars">
      <AiSidebar
        class="desktop-ai-sidebar"
        :sessions="sessions"
        :active-session-id="currentSessionId"
        :loading="loadingSessions"
        :creating="creatingSession"
        @create="handleCreateSession"
        @select="handleSelectSession"
        @delete="handleDeleteSession"
      />

      <section class="chat-panel">
        <header class="chat-header">
          <div class="chat-header-main">
            <el-button class="history-trigger" round @click="historyDrawerVisible = true">
              会话历史
            </el-button>
            <h1>{{ currentSession?.title || '新对话' }}</h1>
          </div>
        </header>

        <div class="chat-body">
          <el-scrollbar ref="chatScrollRef" class="message-scroll">
            <div v-if="loadingMessages" class="message-loading">
              <el-skeleton v-for="index in 4" :key="index" animated>
                <template #template>
                  <el-skeleton-item variant="p" style="width: 48%; height: 20px" />
                  <el-skeleton-item variant="text" style="width: 100%; margin-top: 12px" />
                  <el-skeleton-item variant="text" style="width: 82%; margin-top: 8px" />
                </template>
              </el-skeleton>
            </div>

            <div v-else-if="!messages.length" class="message-empty">
              <div class="empty-copy">
                <h2>开始一个新的对话</h2>
                <p>输入一个问题，AI 会为你持续生成回答。</p>
              </div>
            </div>

            <div v-else class="message-list">
              <article
                v-for="message in messages"
                :key="message.id"
                class="message-item"
                :class="`message-${message.role}`"
              >
                <div class="message-bubble">
                  <div
                    v-if="message.role === 'assistant'"
                    class="message-content markdown-body"
                    v-html="renderAssistantContent(message.content)"
                  ></div>
                  <div v-else class="message-content message-plain">
                    {{ message.content }}
                  </div>
                </div>
              </article>
            </div>
          </el-scrollbar>
        </div>

        <footer class="composer">
          <div class="composer-shell">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="给 AI 发送消息"
              :disabled="sending"
              @keydown.enter.exact.prevent="handleSend"
            />

            <div class="composer-actions">
              <el-button
                type="primary"
                circle
                :loading="sending"
                :disabled="!canSend"
                @click="handleSend"
              >
                ↑
              </el-button>
            </div>
          </div>
        </footer>
      </section>
    </div>

    <el-drawer
      v-model="historyDrawerVisible"
      direction="ltr"
      size="320px"
      class="mobile-ai-history-drawer"
      title="会话历史"
    >
      <AiSidebar
        class="mobile-ai-sidebar"
        :sessions="sessions"
        :active-session-id="currentSessionId"
        :loading="loadingSessions"
        :creating="creatingSession"
        @create="handleDrawerCreateSession"
        @select="handleDrawerSelectSession"
        @delete="handleDrawerDeleteSession"
      />
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import Navbar from '@/components/Navbar.vue'
import AiSidebar from '@/components/AiSidebar.vue'
import { aiApi } from '@/api/frontend'
import { streamAiMessage } from '@/api/ai-stream'
import { buildAiLayoutVars } from './ai-layout'

const layoutVars = buildAiLayoutVars()

const sessions = ref([])
const messages = ref([])
const currentSessionId = ref(null)
const draft = ref('')
const loadingSessions = ref(false)
const loadingMessages = ref(false)
const creatingSession = ref(false)
const sending = ref(false)
const chatScrollRef = ref(null)
const streamController = ref(null)
const historyDrawerVisible = ref(false)

const currentSession = computed(() =>
  sessions.value.find((session) => session.id === currentSessionId.value) || null,
)

const canSend = computed(() => Boolean(draft.value.trim()) && !sending.value)

const getScrollWrap = () =>
  chatScrollRef.value?.wrapRef ||
  chatScrollRef.value?.$el?.querySelector?.('.el-scrollbar__wrap') ||
  null

const scrollToBottom = async () => {
  await nextTick()
  const wrap = getScrollWrap()

  if (wrap) {
    wrap.scrollTop = wrap.scrollHeight
  }
}

const renderAssistantContent = (content) => marked.parse(content || '')

const replaceSessions = (items) => {
  sessions.value = [...items].sort((left, right) =>
    (right.updatedAt || '').localeCompare(left.updatedAt || ''),
  )
}

const upsertSession = (session) => {
  replaceSessions([session, ...sessions.value.filter((item) => item.id !== session.id)])
}

const loadSessions = async ({ preserveSelection = true } = {}) => {
  loadingSessions.value = true

  try {
    const response = await aiApi.getSessions({ page: 1, pageSize: 50 })
    replaceSessions(response.data)

    if (!sessions.value.length) {
      currentSessionId.value = null
      messages.value = []
      return
    }

    const hasCurrent =
      preserveSelection &&
      currentSessionId.value &&
      sessions.value.some((session) => session.id === currentSessionId.value)

    const nextSessionId = hasCurrent ? currentSessionId.value : sessions.value[0].id

    if (nextSessionId && nextSessionId !== currentSessionId.value) {
      await loadMessages(nextSessionId)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取 AI 会话失败，请稍后重试')
  } finally {
    loadingSessions.value = false
  }
}

const loadMessages = async (sessionId) => {
  if (!sessionId) {
    currentSessionId.value = null
    messages.value = []
    return
  }

  loadingMessages.value = true

  try {
    currentSessionId.value = sessionId
    const response = await aiApi.getMessages(sessionId, { page: 1, pageSize: 200 })
    messages.value = response.data.map((message) => ({
      ...message,
      status: message.status || 'success',
    }))
    await scrollToBottom()
  } catch (error) {
    ElMessage.error(error.message || '获取消息历史失败，请稍后重试')
  } finally {
    loadingMessages.value = false
  }
}

const createSession = async (title = '') => {
  creatingSession.value = true

  try {
    const response = await aiApi.createSession({ title })
    const session = response.data
    currentSessionId.value = session.id
    messages.value = []
    upsertSession(session)
    return session
  } catch (error) {
    ElMessage.error(error.message || '创建会话失败，请稍后重试')
    return null
  } finally {
    creatingSession.value = false
  }
}

const ensureActiveSession = async (content) => {
  if (currentSession.value) {
    return currentSession.value
  }

  return createSession(content.slice(0, 30))
}

const refreshSessionList = async () => {
  try {
    const response = await aiApi.getSessions({ page: 1, pageSize: 50 })
    replaceSessions(response.data)
  } catch (error) {
    console.error('refresh ai sessions failed', error)
  }
}

const handleCreateSession = async () => {
  if (sending.value) {
    ElMessage.warning('当前回复还在生成，请稍后再新建会话')
    return
  }

  await createSession('')
}

const handleSelectSession = async (sessionId) => {
  if (sending.value) {
    ElMessage.warning('当前回复还在生成，请稍后切换会话')
    return
  }

  if (sessionId === currentSessionId.value) {
    return
  }

  await loadMessages(sessionId)
}

const handleDeleteSession = async (sessionId) => {
  if (sending.value) {
    ElMessage.warning('当前回复还在生成，请稍后再删除历史记录')
    return
  }

  const targetIndex = sessions.value.findIndex((session) => session.id === sessionId)
  const fallbackSession =
    sessions.value[targetIndex + 1] ||
    sessions.value[targetIndex - 1] ||
    null

  try {
    await ElMessageBox.confirm('删除后将无法恢复这条历史记录，是否继续？', '删除历史记录', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    await aiApi.deleteSession(sessionId)
    sessions.value = sessions.value.filter((session) => session.id !== sessionId)

    if (currentSessionId.value === sessionId) {
      if (fallbackSession) {
        await loadMessages(fallbackSession.id)
      } else {
        currentSessionId.value = null
        messages.value = []
      }
    }

    ElMessage.success('历史记录已删除')
  } catch (error) {
    ElMessage.error(error.message || '删除历史记录失败，请稍后重试')
  }
}

const handleDrawerCreateSession = async () => {
  await handleCreateSession()
  historyDrawerVisible.value = false
}

const handleDrawerSelectSession = async (sessionId) => {
  await handleSelectSession(sessionId)
  historyDrawerVisible.value = false
}

const handleDrawerDeleteSession = async (sessionId) => {
  await handleDeleteSession(sessionId)

  if (!sessions.value.length) {
    historyDrawerVisible.value = false
  }
}

const handleSend = async () => {
  const content = draft.value.trim()

  if (!content || sending.value) {
    return
  }

  const session = await ensureActiveSession(content)
  if (!session) {
    return
  }

  const timestamp = new Date().toISOString().slice(0, 16).replace('T', ' ')
  const userMessage = {
    id: `user-${Date.now()}`,
    role: 'user',
    content,
    createdAt: timestamp,
    status: 'success',
  }
  const assistantMessage = {
    id: `assistant-${Date.now()}`,
    role: 'assistant',
    content: '',
    createdAt: timestamp,
    status: 'sending',
  }

  messages.value = [...messages.value, userMessage, assistantMessage]
  draft.value = ''
  upsertSession({
    ...session,
    lastMessagePreview: content,
    updatedAt: timestamp,
  })
  await scrollToBottom()

  const controller = new AbortController()
  streamController.value = controller
  sending.value = true

  try {
    const donePayload = await streamAiMessage({
      sessionId: session.id,
      content,
      signal: controller.signal,
      onChunk: async (chunk) => {
        assistantMessage.content += chunk
        messages.value = [...messages.value]
        await scrollToBottom()
      },
      onDone: (payload) => {
        assistantMessage.id = payload?.assistantMessageId || assistantMessage.id
        assistantMessage.content = payload?.content || assistantMessage.content
        assistantMessage.status = 'success'
        messages.value = [...messages.value]
      },
      onError: () => {
        assistantMessage.status = 'failed'
        messages.value = [...messages.value]
      },
    })

    if (donePayload && !assistantMessage.content) {
      assistantMessage.content = donePayload.content || ''
    }

    assistantMessage.status = assistantMessage.status === 'failed' ? 'failed' : 'success'
    messages.value = [...messages.value]
    await refreshSessionList()
    await scrollToBottom()
  } catch (error) {
    assistantMessage.status = 'failed'
    if (!assistantMessage.content) {
      assistantMessage.content = '这次回复没有成功完成，你可以直接重新发送问题。'
    }
    messages.value = [...messages.value]

    if (error.name !== 'AbortError') {
      ElMessage.error(error.message || 'AI 回复失败，请稍后重试')
    }
  } finally {
    sending.value = false
    streamController.value = null
  }
}

onMounted(async () => {
  document.getElementById('app')?.classList.add('app-ai-mode')
  await loadSessions({ preserveSelection: false })
})

onUnmounted(() => {
  document.getElementById('app')?.classList.remove('app-ai-mode')
  streamController.value?.abort?.()
})
</script>

<style scoped>
.ai-page {
  min-height: 100vh;
  background: #ffffff;
}

.ai-shell {
  width: 100%;
  box-sizing: border-box;
  margin: 0;
  padding: 0 0 20px;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 0;
  height: var(--ai-shell-height);
  min-height: var(--ai-panel-min-height);
  align-items: stretch;
}

.chat-panel {
  height: 100%;
  min-height: var(--ai-panel-min-height);
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  background: #ffffff;
  overflow: hidden;
}

.chat-header {
  padding: 22px 34px 10px;
}

.chat-header-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.history-trigger {
  display: none;
  border-color: #dbe3f1;
  background: #fff;
  color: #1f2937;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
}

.chat-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #111827;
}

.chat-body {
  min-height: 0;
  overflow: hidden;
}

.message-scroll {
  height: 100%;
  padding: 8px 34px 20px;
}

.message-loading {
  display: grid;
  gap: 18px;
}

.message-empty {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-copy {
  text-align: center;
  color: #94a3b8;
}

.empty-copy h2 {
  margin: 0 0 10px;
  font-size: 30px;
  font-weight: 500;
  color: #0f172a;
}

.empty-copy p {
  margin: 0;
  font-size: 15px;
}

.message-list {
  display: grid;
  gap: 18px;
  max-width: 960px;
  margin: 0 auto;
}

.message-item {
  display: flex;
}

.message-user {
  justify-content: flex-end;
}

.message-assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: min(100%, 780px);
  padding: 14px 18px;
  border-radius: 22px;
  background: #f8fafc;
  color: #111827;
}

.message-user .message-bubble {
  background: #eaf1ff;
}

.message-content {
  line-height: 1.8;
  color: #0f172a;
}

.message-plain {
  white-space: pre-wrap;
}

.composer {
  padding: 0 24px 16px;
}

.composer-shell {
  max-width: 780px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 28px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
}

.composer-actions {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.message-scroll .el-scrollbar__wrap),
:deep(.session-scroll .el-scrollbar__wrap) {
  overscroll-behavior: contain;
}

:global(#app.app-ai-mode) {
  max-width: none;
  width: 100%;
  margin: 0;
  padding: 0;
}

@media (max-width: 1024px) {
  .ai-shell {
    grid-template-columns: 1fr;
    height: auto;
  }

  .chat-panel {
    min-height: 640px;
  }
}

@media (max-width: 1024px) {
  .ai-shell {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .desktop-ai-sidebar {
    display: none;
  }

  .history-trigger {
    display: inline-flex;
  }

  .chat-header {
    padding: 18px 20px 8px;
  }

  .chat-header-main {
    flex-wrap: wrap;
  }

  .chat-panel {
    min-height: calc(100vh - 120px);
  }
}

@media (max-width: 768px) {
  .chat-header {
    padding: 18px 18px 8px;
  }

  .message-scroll {
    padding: 8px 18px 16px;
  }

  .empty-copy h2 {
    font-size: 24px;
  }

  .composer {
    padding: 0 14px 14px;
  }

  .composer-shell {
    grid-template-columns: 1fr auto;
    padding: 12px;
    border-radius: 22px;
  }
}

:deep(.mobile-ai-history-drawer .el-drawer__body) {
  padding: 0;
}

.mobile-ai-sidebar {
  height: 100%;
  min-height: 100%;
  border-right: none;
}
</style>
