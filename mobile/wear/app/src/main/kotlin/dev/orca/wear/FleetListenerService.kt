package dev.orca.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Receives the phone's fleet snapshot on [FLEET_PATH] and caches it. */
class FleetListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(events: DataEventBuffer) {
        val snapshots = events
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == FLEET_PATH }
            .mapNotNull { SnapshotCodec.decode(DataMapItem.fromDataItem(it.dataItem).dataMap.getString(PAYLOAD_KEY)) }
        val latest = snapshots.maxByOrNull { it.sentAt } ?: return
        PhoneLink.set(true)
        scope.launch { SnapshotStore(applicationContext).save(latest) }
    }
}
