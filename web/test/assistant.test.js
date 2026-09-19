import assert from 'node:assert/strict'
import test from 'node:test'

import { chatWithAssistantApi } from '../src/api/assistant.js'

test('收到 DONE 后主动结束仍保持连接的 SSE 响应', async () => {
  const originalFetch = globalThis.fetch
  const originalLocalStorage = globalThis.localStorage
  let cancelled = false

  globalThis.localStorage = {
    getItem: () => 'test-token',
    removeItem: () => {},
  }
  globalThis.fetch = async () => {
    const encoder = new TextEncoder()
    const body = new ReadableStream({
      start(controller) {
        controller.enqueue(encoder.encode('data: 第一段\n\n'))
        controller.enqueue(encoder.encode('data: 第二段\n\ndata: [DONE]\n\n'))
        // 故意不 close：客户端必须依靠协议结束标记收尾。
      },
      cancel() {
        cancelled = true
      },
    })
    return new Response(body, {
      status: 200,
      headers: { 'X-Conversation-Id': 'conversation-1' },
    })
  }

  try {
    const chunks = []
    const result = await Promise.race([
      chatWithAssistantApi({ message: '继续讲解', onChunk: (chunk) => chunks.push(chunk) }),
      new Promise((_, reject) => setTimeout(() => reject(new Error('SSE 请求未按时结束')), 500)),
    ])

    assert.deepEqual(chunks, ['第一段', '第二段'])
    assert.equal(result.conversationId, 'conversation-1')
    assert.equal(cancelled, true)
  } finally {
    globalThis.fetch = originalFetch
    globalThis.localStorage = originalLocalStorage
  }
})
