package dev.orca.wear

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/** SHORT_TEXT complication: how many agents need you. Cache-only read. */
class NeedsYouComplicationService : SuspendingComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        render(type, "2")

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? =
        render(request.complicationType, FleetPresentation.complicationText(SnapshotStore(this).current()))

    private fun render(type: ComplicationType, text: String): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) return null
        val count = text.toIntOrNull() ?: 0
        val description = if (count == 1) "1 agent needs you" else "$count agents need you"
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(description).build(),
        )
            .setTitle(PlainComplicationText.Builder("needs you").build())
            .setTapAction(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, WearActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            ).build()
    }
}
