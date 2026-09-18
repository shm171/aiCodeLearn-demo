<!-- AI 学习助手：全局右下角悬浮按钮 + 聊天窗（深色玻璃风，与 global.css 统一） -->
<template>
  <div class="ai-widget">
    <!-- 聊天窗 -->
    <transition name="chat-pop">
      <div v-if="visible" class="chat-panel" :style="panelStyle">
        <!-- 左上角拖拽手柄：向左拖变宽、向上拖变高（默认尺寸保持不变） -->
        <div class="resize-handle" title="拖拽调整窗口大小" @mousedown.stop.prevent="startResize"></div>
        <div class="chat-header">
          <div class="chat-title">
            <el-icon :size="16"><MagicStick /></el-icon>
            <span>AI 学习助手</span>
          </div>
          <div class="chat-actions">
            <el-tooltip content="新对话" placement="bottom">
              <button class="icon-btn" @click="clearChat"><el-icon :size="14"><Refresh /></el-icon></button>
            </el-tooltip>
            <button class="icon-btn" @click="visible = false"><el-icon :size="14"><Close /></el-icon></button>
          </div>
        </div>

        <!-- 消息列表：用户靠右，AI 靠左 -->
        <div class="chat-body" ref="bodyRef">
          <div v-if="messages.length === 0" class="chat-empty">
            <el-icon :size="36" color="#c4b5fd"><MagicStick /></el-icon>
            <p>你好！我是你的编程学习助手<br />可以问我错题讲解、代码问题、学习方法</p>
          </div>

          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="chat-message"
            :class="msg.role"
          >
            <div v-if="msg.role === 'ai'" class="msg-avatar">AI</div>
            <div class="msg-bubble">{{ msg.content }}</div>
            <div v-if="msg.role === 'user'" class="msg-avatar user">我</div>
          </div>

          <!-- 打字中指示：AI 已开始回复但还没返回第一个字 -->
          <div v-if="isTyping" class="chat-message ai">
            <div class="msg-avatar">AI</div>
            <div class="msg-bubble typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- 输入区：回车发送，发送中禁用防重复提交 -->
        <div class="chat-input-area">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="输入你的问题，回车发送"
            :disabled="sending"
            @keydown.enter.exact.prevent="send()"
          />
          <button class="send-btn" :disabled="sending || !input.trim()" @click="send()">
            <el-icon v-if="!sending" :size="16"><Promotion /></el-icon>
            <el-icon v-else :size="16" class="spin"><Loading /></el-icon>
          </button>
        </div>
      </div>
    </transition>

    <!-- 右下角悬浮按钮 -->
    <button class="fab" :class="{ active: visible }" @click="visible = !visible">
      <el-icon :size="22"><ChatDotRound v-if="!visible" /><Close v-else /></el-icon>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { chatWithAssistantApi } from '../api/assistant'

const visible = ref(false)
const input = ref('')
const messages = ref([]) // { role: 'user' | 'ai', content }
const sending = ref(false)
const bodyRef = ref(null)

// 会话ID：存 localStorage，刷新页面后继续同一会话（多轮记忆）
const conversationId = ref(localStorage.getItem('aiConversationId') || '')

// 聊天窗尺寸：默认 380x520，可拖拽左上角调整，并记住用户上次的大小
const DEFAULT_W = 380
const DEFAULT_H = 520
const panelWidth = ref(Number(localStorage.getItem('aiChatWidth')) || DEFAULT_W)
const panelHeight = ref(Number(localStorage.getItem('aiChatHeight')) || DEFAULT_H)
const panelStyle = computed(() => ({
  width: panelWidth.value + 'px',
  height: panelHeight.value + 'px',
}))

// 拖拽左上角手柄改大小：鼠标向左移变宽、向上移变高
function startResize(e) {
  const startX = e.clientX
  const startY = e.clientY
  const startW = panelWidth.value
  const startH = panelHeight.value
  const onMove = (ev) => {
    const w = startW - (ev.clientX - startX)
    const h = startH - (ev.clientY - startY)
    // 限制最小/最大，避免拖没或盖住整个屏幕
    panelWidth.value = Math.min(Math.max(w, 340), window.innerWidth - 40)
    panelHeight.value = Math.min(Math.max(h, 420), window.innerHeight - 120)
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    localStorage.setItem('aiChatWidth', String(panelWidth.value))
    localStorage.setItem('aiChatHeight', String(panelHeight.value))
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

// 打字中：正在请求且最后一条 AI 消息还没有内容
const isTyping = computed(() => {
  if (!sending.value || messages.value.length === 0) return false
  const last = messages.value[messages.value.length - 1]
  return last.role === 'ai' && !last.content
})

// 新消息自动滚到底部
watch(
  messages,
  () => {
    nextTick(() => {
      if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    })
  },
  { deep: true }
)

// 发送消息：先塞用户气泡，再塞空 AI 气泡逐块填充（打字机效果）
async function send(text) {
  const message = (text ?? input.value).trim()
  if (!message || sending.value) return
  input.value = ''
  sending.value = true

  messages.value.push({ role: 'user', content: message })
  const aiMsg = reactive({ role: 'ai', content: '' })
  messages.value.push(aiMsg)

  const controller = new AbortController()
  try {
    const newId = await chatWithAssistantApi({
      message,
      conversationId: conversationId.value,
      signal: controller.signal,
      onChunk: (chunk) => {
        aiMsg.content += chunk
      },
    })
    conversationId.value = newId
    localStorage.setItem('aiConversationId', newId || '')
    // 流正常结束但一个字都没有：提示重试
    if (!aiMsg.content.trim()) {
      aiMsg.content = '（AI 没有返回内容，请稍后重试）'
    }
  } catch (err) {
    // 失败时保留已收到的部分，没收到就显示错误原因
    aiMsg.content =
      aiMsg.content || `请求失败：${err?.message || '网络异常，请稍后重试'}`
  } finally {
    sending.value = false
  }
}

// 新对话：清空消息与会话ID，下次请求重新生成
function clearChat() {
  if (sending.value) return
  messages.value = []
  conversationId.value = ''
  localStorage.removeItem('aiConversationId')
}

// 供其他页面唤起：window.dispatchEvent(new CustomEvent('open-ai-assistant', { detail: { message } }))
function onExternalOpen(e) {
  const message = e.detail?.message
  if (!message) return
  visible.value = true
  send(message)
}

onMounted(() => window.addEventListener('open-ai-assistant', onExternalOpen))
onBeforeUnmount(() => window.removeEventListener('open-ai-assistant', onExternalOpen))
</script>

<style scoped>
.ai-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
}

/* ===== 悬浮按钮 ===== */
.fab {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 1px solid rgba(196, 181, 253, 0.4);
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  color: #f4f4f5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 28px rgba(124, 58, 237, 0.45);
  transition: transform 0.2s, box-shadow 0.2s;
}
.fab:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 12px 34px rgba(124, 58, 237, 0.6);
}
.fab.active {
  background: rgba(30, 30, 36, 0.9);
  color: #a1a1aa;
}

/* ===== 聊天窗 ===== */
.chat-panel {
  position: absolute;
  right: 0;
  bottom: 64px;
  width: 380px;
  height: 520px;
  min-width: 340px;
  min-height: 420px;
  max-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  background: rgba(18, 18, 24, 0.97);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(20px);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
}

/* 左上角拖拽手柄：斜条纹暗示可拉伸 */
.resize-handle {
  position: absolute;
  left: 0;
  top: 0;
  width: 18px;
  height: 18px;
  z-index: 10;
  cursor: nwse-resize;
  opacity: 0.3;
  transition: opacity 0.15s;
  border-top-left-radius: 16px;
  background:
    linear-gradient(135deg,
      transparent 0 38%,
      rgba(196, 181, 253, 0.8) 38% 46%,
      transparent 46% 58%,
      rgba(196, 181, 253, 0.8) 58% 66%,
      transparent 66% 78%,
      rgba(196, 181, 253, 0.8) 78% 86%,
      transparent 86%);
}
.resize-handle:hover { opacity: 1; }
.chat-pop-enter-active,
.chat-pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.chat-pop-enter-from,
.chat-pop-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 14px 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(196, 181, 253, 0.05);
}
.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #f4f4f5;
}
.chat-title .el-icon { color: #c4b5fd; }
.chat-actions { display: flex; gap: 6px; }
.icon-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #71717a;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.icon-btn:hover { background: rgba(255, 255, 255, 0.08); color: #d4d4d8; }

/* 消息区 */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chat-empty {
  text-align: center;
  color: #71717a;
  font-size: 12px;
  line-height: 1.8;
  margin: auto 0;
  padding: 20px 0;
}
.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 100%;
}
.chat-message.user { justify-content: flex-end; }
.msg-avatar {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  border-radius: 8px;
  background: rgba(196, 181, 253, 0.15);
  color: #c4b5fd;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.msg-avatar.user {
  background: rgba(96, 165, 250, 0.15);
  color: #60a5fa;
}
.msg-bubble {
  max-width: 78%;
  padding: 9px 13px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d8;
  background: rgba(255, 255, 255, 0.06);
  white-space: pre-wrap;
  word-break: break-word;
}
.chat-message.user .msg-bubble {
  background: rgba(196, 181, 253, 0.16);
  border: 1px solid rgba(196, 181, 253, 0.25);
  color: #e4e4e7;
}

/* 打字中三点动画 */
.msg-bubble.typing {
  display: flex;
  gap: 4px;
  padding: 12px 14px;
}
.msg-bubble.typing .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c4b5fd;
  animation: typing-bounce 1.2s infinite ease-in-out;
}
.msg-bubble.typing .dot:nth-child(2) { animation-delay: 0.15s; }
.msg-bubble.typing .dot:nth-child(3) { animation-delay: 0.3s; }
@keyframes typing-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-4px); opacity: 1; }
}

/* 输入区 */
.chat-input-area {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.send-btn {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 10px;
  border: none;
  background: #c4b5fd;
  color: #0a0a0c;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s;
}
.send-btn:hover:not(:disabled) { background: #d4c5fe; }
.send-btn:disabled {
  background: rgba(255, 255, 255, 0.08);
  color: #52525b;
  cursor: not-allowed;
}
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

/* 手机端：聊天窗铺开，不允许拖拽 */
@media (max-width: 768px) {
  .ai-widget { right: 14px; bottom: 14px; }
  .chat-panel {
    width: calc(100vw - 28px) !important;
    height: 70vh !important;
    right: 0;
  }
  .resize-handle { display: none; }
}
</style>
