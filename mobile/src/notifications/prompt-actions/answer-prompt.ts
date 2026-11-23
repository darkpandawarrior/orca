export type PromptAction = 'allow' | 'deny' | 'reply'

export async function answerPromptFromNotification(_input: {
  hostId?: string
  sessionId: string
  promptId: string
  action: PromptAction
  text?: string
}): Promise<'accepted' | 'already_answered' | 'failed'> {
  throw new Error('not implemented')
}
