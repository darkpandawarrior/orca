import { describe, expect, it, vi } from 'vitest'

const sendRequest = vi.fn()
vi.mock('../../host-catalog/host-store', () => ({
  loadHostCatalog: async () => [{ id: 'host-1', profile: { id: 'host-1' } }]
}))
vi.mock('../../transport/connection/host-logical-client', () => ({
  openHostLogicalClient: () => ({ sendRequest, close: vi.fn() })
}))

import { answerPromptFromNotification } from './answer-prompt'

const approval = {
  itemId: 'prompt-1',
  revision: 3,
  body: {
    kind: 'approval',
    options: [{ id: 'yes', label: 'Allow', intent: 'allow_once' }],
    resolution: { state: 'pending', selectedOptionId: null }
  }
}

describe('answerPromptFromNotification', () => {
  it('reports already_answered when the host refuses a stale revision', async () => {
    sendRequest.mockImplementation(async (method: string) => ({
      ok: true,
      result:
        method === 'agentSession.history'
          ? { ok: true, page: { fence: 7, items: [approval] } }
          : { ok: false, refusal: { code: 'agent_session_item_revision_stale' } }
    }))

    const outcome = await answerPromptFromNotification({
      hostId: 'host-1',
      sessionId: 'session-1',
      promptId: 'prompt-1',
      action: 'allow'
    })

    expect(outcome).toBe('already_answered')
    expect(sendRequest).toHaveBeenCalledWith(
      'agentSession.respondToApproval',
      expect.objectContaining({ itemId: 'prompt-1', expectedRevision: 3, optionId: 'yes' }),
      expect.anything()
    )
  })
})
