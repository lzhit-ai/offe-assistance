const DEFAULT_BASE_URL =
  (typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_BASE_URL) || 'http://localhost:8080'

const readStoredToken = () =>
  (typeof localStorage !== 'undefined' && localStorage.getItem('token')) || ''

const safeParseJson = (value: string) => {
  const raw = value.trim()
  if (!raw) {
    return ''
  }

  try {
    return JSON.parse(raw) as unknown
  } catch {
    return raw
  }
}

const resolveBaseUrl = (baseURL = DEFAULT_BASE_URL) => baseURL.replace(/\/$/, '')

const readErrorMessage = async (response: Response) => {
  const contentType = response.headers.get('content-type') || ''

  if (contentType.includes('application/json')) {
    const payload = (await response.json()) as { message?: string; error?: string }
    return payload?.message || payload?.error || 'AI 请求失败'
  }

  const text = await response.text()
  return text || 'AI 请求失败'
}

interface ParsedSseEvent {
  event: string
  data: unknown
}

const emitSseEvent = (rawEvent: string, onEvent: (event: ParsedSseEvent) => void) => {
  const normalized = rawEvent.replace(/\r/g, '')
  const lines = normalized.split('\n')
  let event = 'message'
  const dataLines: string[] = []

  for (const line of lines) {
    if (!line || line.startsWith(':')) {
      continue
    }

    if (line.startsWith('event:')) {
      event = line.slice(6).trim() || 'message'
      continue
    }

    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart())
    }
  }

  if (!dataLines.length) {
    return
  }

  onEvent({
    event,
    data: safeParseJson(dataLines.join('\n')),
  })
}

export const createSseParser = (onEvent: (event: ParsedSseEvent) => void) => {
  let buffer = ''

  const drain = () => {
    let boundaryIndex = buffer.indexOf('\n\n')

    while (boundaryIndex !== -1) {
      const rawEvent = buffer.slice(0, boundaryIndex)
      buffer = buffer.slice(boundaryIndex + 2)
      emitSseEvent(rawEvent, onEvent)
      boundaryIndex = buffer.indexOf('\n\n')
    }
  }

  return {
    push(chunk: string) {
      buffer += chunk.replace(/\r\n/g, '\n')
      drain()
    },
    flush() {
      const trailing = buffer.trim()
      if (trailing) {
        emitSseEvent(buffer, onEvent)
      }
      buffer = ''
    },
  }
}

interface StreamAiMessageOptions {
  sessionId: number
  content: string
  token?: string
  signal?: AbortSignal
  baseURL?: string
  fetchImpl?: typeof fetch
  onChunk?: (chunk: string) => void
  onDone?: (payload: unknown) => void
  onError?: (error: Error) => void
}

export const streamAiMessage = async ({
  sessionId,
  content,
  token = readStoredToken(),
  signal,
  baseURL = DEFAULT_BASE_URL,
  fetchImpl = fetch,
  onChunk,
  onDone,
  onError,
}: StreamAiMessageOptions) => {
  const response = await fetchImpl(
    `${resolveBaseUrl(baseURL)}/api/v1/ai/sessions/${sessionId}/messages/stream`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ content }),
      signal,
    },
  )

  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }

  if (!response.body) {
    throw new Error('AI 流式响应不可用')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let streamError: Error | null = null
  let donePayload: unknown = null
  let streamCompleted = false

  const parser = createSseParser(({ event, data }) => {
    if (event === 'chunk') {
      onChunk?.(String(data ?? ''))
      return
    }

    if (event === 'done') {
      donePayload = data
      streamCompleted = true
      onDone?.(data)
      return
    }

    if (event === 'error') {
      streamError = new Error(typeof data === 'string' ? data : 'AI 回复失败')
      onError?.(streamError)
    }
  })

  while (true) {
    const { done, value } = await reader.read()

    if (done) {
      break
    }

    parser.push(decoder.decode(value, { stream: true }))

    if (streamError) {
      await reader.cancel().catch(() => undefined)
      throw streamError
    }

    if (streamCompleted) {
      await reader.cancel().catch(() => undefined)
      break
    }
  }

  parser.flush()

  if (streamError) {
    throw streamError
  }

  return donePayload
}
