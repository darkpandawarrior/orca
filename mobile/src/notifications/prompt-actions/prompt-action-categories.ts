import * as Notifications from 'expo-notifications'
import { Platform } from 'react-native'
import { translate } from '../../i18n/mobile-i18n'
import { PROMPT_ACTION_ID } from './prompt-action-ids'

export const PROMPT_CATEGORY = {
  approval: 'orca-needs-approval',
  approvalRisky: 'orca-needs-approval-risky',
  question: 'orca-question'
} as const

// One channel per event so vibration is tuned per event in Android settings: needs-you 2 pulses, finished 1, failed 3.
// Never bypasses Do Not Disturb. The cloud picks the channel through data.channelId.
export const EVENT_CHANNEL_ID = {
  needsYou: 'orca-needs-you',
  finished: 'orca-finished',
  failed: 'orca-failed'
} as const

const BACKGROUND = { opensAppToForeground: false } as const

// No "Always allow" on any category: a wrist tap must never widen permissions.
export async function registerPromptActionCategories(): Promise<void> {
  const open = {
    identifier: PROMPT_ACTION_ID.open,
    buttonTitle: translate('notifications.promptActions.open', 'Open on phone'),
    options: { opensAppToForeground: true }
  }
  await Promise.all([
    Notifications.setNotificationCategoryAsync(PROMPT_CATEGORY.approval, [
      {
        identifier: PROMPT_ACTION_ID.allow,
        buttonTitle: translate('notifications.promptActions.allow', 'Allow once'),
        options: BACKGROUND
      },
      {
        identifier: PROMPT_ACTION_ID.deny,
        buttonTitle: translate('notifications.promptActions.deny', 'Deny'),
        options: BACKGROUND
      },
      open
    ]),
    Notifications.setNotificationCategoryAsync(PROMPT_CATEGORY.approvalRisky, [open]),
    Notifications.setNotificationCategoryAsync(PROMPT_CATEGORY.question, [
      {
        identifier: PROMPT_ACTION_ID.reply,
        buttonTitle: translate('notifications.promptActions.reply', 'Reply'),
        textInput: {
          submitButtonTitle: translate('notifications.promptActions.send', 'Send'),
          placeholder: translate('notifications.promptActions.replyPlaceholder', 'Say a reply')
        },
        options: BACKGROUND
      },
      open
    ])
  ])
}

export async function ensureEventChannels(): Promise<void> {
  if (Platform.OS !== 'android') {
    return
  }
  const base = {
    importance: Notifications.AndroidImportance.HIGH,
    // Command text shows only on an unlocked device.
    lockscreenVisibility: Notifications.AndroidNotificationVisibility.PRIVATE,
    bypassDnd: false
  }
  await Promise.all([
    Notifications.setNotificationChannelAsync(EVENT_CHANNEL_ID.needsYou, {
      ...base,
      name: translate('notifications.eventChannels.needsYou', 'Needs you'),
      vibrationPattern: [0, 300, 150, 300]
    }),
    Notifications.setNotificationChannelAsync(EVENT_CHANNEL_ID.finished, {
      ...base,
      name: translate('notifications.eventChannels.finished', 'Finished'),
      vibrationPattern: [0, 150]
    }),
    Notifications.setNotificationChannelAsync(EVENT_CHANNEL_ID.failed, {
      ...base,
      name: translate('notifications.eventChannels.failed', 'Failed'),
      vibrationPattern: [0, 150, 100, 150, 100, 150]
    })
  ])
}
