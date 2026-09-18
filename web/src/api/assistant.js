// AI 学习助手接口：SSE 流式对话
// 注意：接口要带 JWT，不能用 EventSource（它不支持自定义请求头），
// 所以用 fetch POST + response.body.getReader() 手动读流

// 发送消息并逐块接收 AI 回复（打字机效果）
// 参数：message 消息内容；conversationId 会话ID（首次留空）；
//       onChunk 每收到一段文本回调一次；signal 用于中断请求
// 返回：服务端生成/回传的会话ID（下次请求原样传回，保持多轮记忆）
export async function chatWithAssistantApi({ message, conversationId = '', onChunk, signal }) {
  // 用 axios 同款的 /api 前缀，走 vite 代理到后端
  const token = localStorage.getItem('token')

  const res = await fetch('/api/core/assistant/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ message, conversationId }),
    signal,
  })

  // 非 2xx：解析后端错误信息后抛出，由调用方提示
  if (!res.ok) {
    let detail = ''
    try {
      detail = (await res.json())?.detail || ''
    } catch {
      /* 响应体不是 JSON 时忽略 */
    }
    throw new Error(detail || `请求失败（${res.status}）`)
  }

  // 首次对话时后端会在响应头返回会话ID，存下来后续传回
  const newConversationId = res.headers.get('X-Conversation-Id') || conversationId

  // 读流：按 SSE 事件（空行分隔）解析 data 字段，逐块回调
  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    buffer = consumeSseEvents(buffer, onChunk)
  }
  // 流结束时处理剩余缓冲（最后一条事件可能没有结尾空行）
  if (buffer.trim()) {
    consumeSseEvents(buffer + '\n\n', onChunk)
  }
  return newConversationId
}

// 从缓冲区里切出完整的 SSE 事件逐条解析，返回剩余未处理的部分
// 导出以便单元测试直接验证解析逻辑
export function consumeSseEvents(buffer, onChunk) {
  let rest = buffer
  let idx
  while ((idx = rest.indexOf('\n\n')) !== -1) {
    const event = rest.slice(0, idx)
    rest = rest.slice(idx + 2)
    const text = parseSseEvent(event)
    if (text) onChunk(text)
  }
  return rest
}

// 解析单个 SSE 事件：收集 data: 开头的行（多行用换行拼接），忽略心跳注释行
export function parseSseEvent(event) {
  const dataLines = []
  for (const line of event.split('\n')) {
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).replace(/^ /, ''))
    }
  }
  return dataLines.length ? dataLines.join('\n') : ''
}
