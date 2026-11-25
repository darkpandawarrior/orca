package dev.orca.wear

import android.content.ComponentName
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.runBlocking

/** Tile: needs you / working / done counts and "as of HH:MM", read from the DataStore cache. */
class FleetTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        // A tile process is short-lived and cold; one small DataStore read is acceptable here.
        val snapshot = runBlocking { SnapshotStore(this@FleetTileService).current() }
        return Futures.immediateFuture(buildTile(snapshot))
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> =
        Futures.immediateFuture(ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build())

    private fun buildTile(s: WatchSnapshot?): TileBuilders.Tile =
        TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(60_000L)
            .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(layout(s)))
            .build()

    private fun text(value: String, sp: Float, color: Int, bold: Boolean = false) =
        LayoutElementBuilders.Text.Builder()
            .setText(value)
            .setFontStyle(
                LayoutElementBuilders.FontStyle.Builder()
                    .setSize(DimensionBuilders.sp(sp))
                    .setColor(argb(color))
                    .apply { if (bold) setWeight(LayoutElementBuilders.FONT_WEIGHT_BOLD) }
                    .build(),
            ).build()

    private fun layout(s: WatchSnapshot?): LayoutElementBuilders.LayoutElement {
        val col = LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        ModifiersBuilders.Clickable.Builder()
                            .setId("open")
                            .setOnClick(ActionBuilders.launchAction(ComponentName(this, WearActivity::class.java)))
                            .build(),
                    ).build(),
            )
            .addContent(text("ORCA", 12f, 0xFFACACAC.toInt(), bold = true))
        if (s == null) {
            col.addContent(text("Open Orca", 16f, 0xFFFAFAFA.toInt()))
            col.addContent(text("on your phone", 12f, 0xFFACACAC.toInt()))
            return col.build()
        }
        col.addContent(text("${s.counts.needsYou} need you", 18f, FleetPresentation.needsYouColor.toArgbInt(), true))
        col.addContent(text("${s.counts.working} working", 16f, FleetPresentation.workingColor.toArgbInt()))
        col.addContent(text("${s.counts.done} done", 16f, FleetPresentation.doneColor.toArgbInt()))
        col.addContent(text(FleetPresentation.asOf(s.sentAt), 12f, 0xFFACACAC.toInt()))
        return col.build()
    }

    private companion object {
        const val RESOURCES_VERSION = "1"
    }
}

internal fun androidx.compose.ui.graphics.Color.toArgbInt(): Int = this.toArgb()
