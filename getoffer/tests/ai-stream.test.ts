import test from 'node:test'
import assert from 'node:assert/strict'

import { createSseParser, streamAiMessage } from '../src/api/ai-stream.ts'

test('createSseParser parses fragmented chunk and done events', () => {
  const events: Array<{ event: string; data: unknown }> = []
  const parser = createSseParser((event) => events.push(event))

  parser.push('event: chunk\ndata: 你好，')
  parser.push('世界\n\n')
  parser.push('event: done\ndata: {"sessionId":1,"assistantMessageId":2,"content":"你好，世界"}\n\n')

  assert.deepEqual(events, [
    {
      event: 'chunk',
      data: '你好，世界',
    },
    {
      event: 'done',
      data: {
        sessionId: 1,
        assistantMessageId: 2,
        content: '你好，世界',
      },
    },
  ])
})

test('streamAiMessage forwards stream chunks and returns done payload', async () => {
  const encoder = new TextEncoder()
  const stream = new ReadableStream({
    start(controller) {
      controller.enqueue(
        encoder.encode(
          'event: chunk\ndata: 第一段\n\nevent: chunk\ndata: 第二段\n\nevent: done\ndata: {"sessionId":8,"assistantMessageId":13,"content":"第一段第二段"}\n\n',
        ),
      )
      controller.close()
    },
  })

  const chunks: string[] = []
  let donePayload: unknown = null

  const result = await streamAiMessage({
    sessionId: 8,
    content: '你好',
    token: 'demo-token',
    fetchImpl: async () =>
      new Response(stream, {
        status: 200,
        headers: {
          'Content-Type': 'text/event-stream',
        },
      }),
    onChunk: (chunk) => chunks.push(chunk),
    onDone: (payload) => {
      donePayload = payload
    },
  })

  assert.deepEqual(chunks, ['第一段', '第二段'])
  assert.deepEqual(donePayload, {
    sessionId: 8,
    assistantMessageId: 13,
    content: '第一段第二段',
  })
  assert.deepEqual(result, donePayload)
})

test('streamAiMessage surfaces backend json errors', async () => {
  await assert.rejects(
    () =>
      streamAiMessage({
        sessionId: 3,
        content: 'test',
        fetchImpl: async () =>
          new Response(JSON.stringify({ message: '未登录' }), {
            status: 401,
            headers: {
              'Content-Type': 'application/json',
            },
          }),
      }),
    /未登录/,
  )
})
