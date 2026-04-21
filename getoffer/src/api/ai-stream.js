const DEFAULT_BASE_URL =
  (typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_BASE_URL) ||
  'http://localhost:8080'

const readStoredToken = () =>
  (typeof localStorage !== 'undefined' && localStorage.getItem('token')) || ''

const safeParseJson = (value) => {
  if (typeof value !== 'string') {
    return value
  }

  const raw = value.trim()
  if (!raw) {
    return ''
  }

  try {
    return JSON.parse(raw)
  } catch {
    return raw
  }
}

const resolveBaseUrl = (baseURL = DEFAULT_BASE_URL) => baseURL.replace(/\/$/, '')

const readErrorMessage = async (response) => {
  const contentType = response.headers.get('content-type') || ''

  if (contentType.includes('application/json')) {
    const payload = await response.json()
    return payload?.message || payload?.error || 'AI 请求失败'
  }

  const text = await response.text()
  return text || 'AI 请求失败'
}

const emitSseEvent = (rawEvent, onEvent) => {
  const normalized = rawEvent.replace(/\r/g, '')
  const lines = normalized.split('\n')
  let event = 'message'
  const dataLines = []

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

export const createSseParser = (onEvent) => {
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
    push(chunk) {
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
}) => {
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
  let streamError = null
  let donePayload = null

  const parser = createSseParser(({ event, data }) => {
    if (event === 'chunk') {
      onChunk?.(String(data ?? ''))
      return
    }

    if (event === 'done') {
      donePayload = data
      onDone?.(data)
      return
    }

    if (event === 'error') {
      streamError = new Error(typeof data === 'string' ? data : data?.message || 'AI 回复失败')
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
      throw streamError
    }
  }

  parser.flush()

  if (streamError) {
    throw streamError
  }

  return donePayload
}
