import { describe, expect, it } from 'vitest'
import { PushSendRequestSchema } from '@orca-cloud/push-contract/send-messages'
import { buildPushDelivery } from '../../delivery/push-delivery-message.js'
import { fcmMessageBody } from './fcm-client.js'

describe('agent prompt push', () => {
  it('carries the prompt fields from the send request into data-only FCM beside title and body', () => {
    const { notification } = PushSendRequestSchema.parse({
      v: 1,
      registrationIds: ['reg-1'],
      notification: {
        notificationId: 'n-1',
        notificationSeq: 1,
        notificationEpoch: 'epoch-1',
        source: 'agent-task-complete',
        agentState: 'needs-input',
        title: 'Claude needs approval',
        body: 'Run npm test?',
        categoryId: 'orca-agent-prompt',
        promptId: 'prompt-1',
        sessionId: 'session-1'
      }
    })
    const delivery = buildPushDelivery({
      expiresAt: 2_000_000,
      registrationId: 'reg-1',
      hostFingerprint: 'host-fp',
      notification
    })

    const { message } = JSON.parse(
      fcmMessageBody({ delivery, token: 'tok', channelId: 'orca', now: 1_000_000 })
    )

    expect(message.data).toMatchObject({
      categoryId: 'orca-agent-prompt',
      promptId: 'prompt-1',
      sessionId: 'session-1',
      title: 'Claude needs approval',
      message: 'Run npm test?'
    })
  })
})
