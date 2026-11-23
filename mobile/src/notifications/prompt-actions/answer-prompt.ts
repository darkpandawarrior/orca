// Built from CJNA's watch action spike round trip (#42627): open the paired host, read the prompt, answer it once.
import type {
  AgentSessionHistoryResult,
  AgentSessionPromptResult
} from '../../../../src/shared/agent-session/agent-session-wire'
import type { AgentJournalRenderItem } from '../../../../src/shared/agent-session/journal/agent-session-journal-types'
import { openHostLogicalClient } from '../../transport/connection/host-logical-client'
import { loadHostCatalog } from '../../host-catalog/host-store'
import {
  callAgentSession,
  requestStructuredAgentSessionMutation
} from '../../session/native-chat/agent-session/mobile-structured-agent-session-rpc'

export type PromptAction = 'allow' | 'deny' | 'reply'

const ALREADY_ANSWERED_CODES = new Set([
  'agent_session_item_revision_stale',
  'agent_session_already_resolved'
])
const HISTORY_TAIL_LIMIT = 100

export async function answerPromptFromNotification(input: {
  hostId?: string
  sessionId: string
  promptId: string
  action: PromptAction
  text?: string
}): Promise<'accepted' | 'already_answered' | 'failed'> {
  const { sessionId, promptId, action } = input
  try {
    const catalog = await loadHostCatalog()
    const entry = input.hostId
      ? catalog.find((candidate) => candidate.id === input.hostId)
      : catalog.length === 1
        ? catalog[0]
        : undefined
    if (!entry?.profile) {
      return 'failed'
    }
    const client = openHostLogicalClient(entry.profile, () => {})
    try {
      const history = await callAgentSession<AgentSessionHistoryResult>(
        client,
        'agentSession.history',
        { sessionId, direction: 'tail', limit: HISTORY_TAIL_LIMIT }
      )
      const fence = history.page.fence ?? (history.ok ? undefined : history.fence)
      const prompt = history.page.items.find((item) => item.itemId === promptId)
      if (!prompt || fence === undefined) {
        return 'failed'
      }
      if (prompt.body.kind !== 'approval' && prompt.body.kind !== 'question') {
        return 'failed'
      }
      if (prompt.body.resolution.state !== 'pending') {
        return 'already_answered'
      }
      const request = buildResponse(prompt, action, input.text)
      if (!request) {
        return 'failed'
      }
      const result = await requestStructuredAgentSessionMutation<AgentSessionPromptResult>({
        client,
        sessionId,
        expectedRuntimeFence: fence,
        ...request
      })
      if (result.status === 'accepted') {
        return 'accepted'
      }
      if (result.status === 'refused' && ALREADY_ANSWERED_CODES.has(result.code)) {
        return 'already_answered'
      }
      return 'failed'
    } finally {
      client.close()
    }
  } catch {
    return 'failed'
  }
}

function buildResponse(
  prompt: AgentJournalRenderItem,
  action: PromptAction,
  text: string | undefined
): { method: string; fingerprintMethod: string; fields: Record<string, unknown> } | null {
  const target = { itemId: prompt.itemId, expectedRevision: prompt.revision }
  const { body } = prompt
  if (body.kind === 'approval' && action !== 'reply') {
    const wanted = action === 'allow' ? 'allow_once' : 'reject_once'
    const option = body.options.find((candidate) => candidate.intent === wanted)
    return option
      ? {
          method: 'agentSession.respondToApproval',
          fingerprintMethod: 'agentSession.respondTo:approval',
          fields: { ...target, optionId: option.id }
        }
      : null
  }
  const answer = text?.trim()
  if (body.kind === 'question' && action === 'reply' && answer && body.freeTextQuestionId) {
    return {
      method: 'agentSession.respondToQuestion',
      fingerprintMethod: 'agentSession.respondTo:question',
      fields: {
        ...target,
        answers: [{ questionId: body.freeTextQuestionId, optionIds: [], other: answer }]
      }
    }
  }
  return null
}
