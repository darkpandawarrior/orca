package dev.orca.wear

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/** Polls the Data Layer for connected nodes and publishes the result to [PhoneLink]. */
suspend fun pollPhoneReachability(context: Context, everyMs: Long = 30_000L) {
    val client = Wearable.getNodeClient(context.applicationContext)
    while (true) {
        runCatching { client.connectedNodes.await().isNotEmpty() }
            .onSuccess { PhoneLink.set(it) }
            .onFailure { PhoneLink.set(false) }
        delay(everyMs)
    }
}
