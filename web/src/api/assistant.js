// AI 学习助手：使用 fetch 读取后端 SSE 流，并将增量文本交给页面展示。
import request from '../utils/request'

export function generatePracticeApi(errorType, category, language = 'CPP') {
  return request.post('/core/assistant/practice', { errorType, category, language })
}

export async function chatWithAssistantApi({ message, conversationId, onChunk, signal }) {
  const token = localStorage.getItem('token')
  const response = await fetch('/api/core/assistant/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ message, conversationId: conversationId || null }),
    signal,
  })

  if (!response.ok) {
    if (response.status === 401) {
      for (const key of ['token', 'userId', 'email', 'username', 'role']) {
        localStorage.removeItem(key)
      }
      if (window.location.pathname !== '/login') window.location.href = '/login'
      throw new Error('登录状态已失效，请重新登录')
    }
    const detail = await response.text()
    throw new Error(detail || `AI 助手请求失败（${response.status}）`)
  }

  const nextConversationId = response.headers.get('X-Conversation-Id') || conversationId || ''
  if (!response.body) {
    throw new Error('浏览器不支持流式响应')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  const emitEvent = (event) => {
    const text = event
      .split(/\r?\n/)
      .filter((line) => line.startsWith('data:'))
      .map((line) => line.slice(5).replace(/^ /, ''))
      .join('\n')
    if (text && text !== '[DONE]') onChunk?.(text)
  }

  while (true) {
    const { value, done } = await reader.read()
    buffer += decoder.decode(value || new Uint8Array(), { stream: !done })
    const events = buffer.split(/\r?\n\r?\n/)
    buffer = events.pop() || ''
    events.forEach(emitEvent)
    if (done) break
  }

  if (buffer.trim()) {
    if (buffer.includes('data:')) emitEvent(buffer)
    else onChunk?.(buffer)
  }

  return { conversationId: nextConversationId }
}
