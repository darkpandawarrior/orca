import { describe, expect, it } from 'vitest'
import { parsePromptActionResponse } from './prompt-action-payload'

const withData = (data: unknown, actionIdentifier: string, userText?: string) => ({
  actionIdentifier,
  userText,
  notification: { request: { content: { data } } }
})

describe('parsePromptActionResponse', () => {
  it('maps Allow once from flat FCM data', () => {
    expect(
      parsePromptActionResponse(
        withData({ hostId: 'h', sessionId: 's', promptId: 'p' }, 'orca.allow-once')
      )
    ).toEqual({ hostId: 'h', sessionId: 's', promptId: 'p', action: 'allow' })
  })

  it('refuses an empty reply, Open, and a payload without a prompt', () => {
    const data = { sessionId: 's', promptId: 'p' }
    expect(parsePromptActionResponse(withData(data, 'orca.reply', ''))).toBeNull()
    expect(parsePromptActionResponse(withData(data, 'orca.open'))).toBeNull()
    expect(parsePromptActionResponse(withData({ sessionId: 's' }, 'orca.deny'))).toBeNull()
  })
})
