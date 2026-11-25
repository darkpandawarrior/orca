package dev.orca.wear

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

object SurfaceRefresh {
    fun requestAll(context: Context) {
        val app = context.applicationContext
        runCatching { TileService.getUpdater(app).requestUpdate(FleetTileService::class.java) }
        runCatching {
            ComplicationDataSourceUpdateRequester.create(
                app,
                ComponentName(app, NeedsYouComplicationService::class.java),
            ).requestUpdateAll()
        }
    }
}
