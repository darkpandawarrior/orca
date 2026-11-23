import { PROMPT_ACTION_ID } from './prompt-action-ids'
import type { PromptAction } from './answer-prompt'

export type PromptActionRequest = {
  hostId?: string
  sessionId: string
  promptId: string
  action: PromptAction
  text?: string
}

const ACTIONS: Record<string, PromptAction> = {
  [PROMPT_ACTION_ID.allow]: 'allow',
  [PROMPT_ACTION_ID.deny]: 'deny',
  [PROMPT_ACTION_ID.reply]: 'reply'
}

function readString(value: unknown): string | undefined {
  return typeof value === 'string' && value.length > 0 ? value : undefined
}

// Returns null for Open, the default tap, unknown actions, or a payload without a prompt to answer.
export function parsePromptActionResponse(response: {
  actionIdentifier?: unknown
  userText?: unknown
  notification?: { request?: { content?: { data?: unknown } } }
}): PromptActionRequest | null {
  const action = ACTIONS[String(response.actionIdentifier)]
  const data = response.notification?.request?.content?.data
  if (!action || !data || typeof data !== 'object') {
    return null
  }
  const nested = (data as { orca?: unknown }).orca
  const record = (nested && typeof nested === 'object' ? nested : data) as Record<string, unknown>
  const sessionId = readString(record.sessionId)
  const promptId = readString(record.promptId)
  const text = readString(response.userText)
  if (!sessionId || !promptId || (action === 'reply' && !text)) {
    return null
  }
  return {
    ...(readString(record.hostId) ? { hostId: readString(record.hostId) } : {}),
    sessionId,
    promptId,
    action,
    ...(action === 'reply' ? { text } : {})
  }
}
