import * as Notifications from 'expo-notifications'
import { translate } from '../../i18n/mobile-i18n'
import { answerPromptFromNotification } from './answer-prompt'
import { parsePromptActionResponse } from './prompt-action-payload'

const REPEAT_WINDOW_MS = 2000
const recentTaps = new Map<string, number>()

// Runs in the headless task, so it must not touch React state.
export async function runPromptActionResponse(response: Notifications.NotificationResponse) {
  const request = parsePromptActionResponse(response)
  if (!request) {
    return
  }
  // A double tap must not become two answers.
  const now = Date.now()
  const last = recentTaps.get(request.promptId)
  if (last !== undefined && now - last < REPEAT_WINDOW_MS) {
    return
  }
  recentTaps.set(request.promptId, now)

  const identifier = response.notification.request.identifier
  const outcome = await answerPromptFromNotification(request).catch(() => 'failed' as const)
  if (outcome === 'accepted') {
    await Notifications.dismissNotificationAsync(identifier)
    return
  }
  const body =
    outcome === 'already_answered'
      ? translate('notifications.promptActions.alreadyAnswered', 'Already answered on desktop')
      : translate('notifications.promptActions.failed', 'Could not send. Open Orca on your phone.')
  await Notifications.scheduleNotificationAsync({
    identifier,
    content: {
      title: response.notification.request.content.title ?? 'Orca',
      body,
      data: response.notification.request.content.data
    },
    trigger: null
  })
}
