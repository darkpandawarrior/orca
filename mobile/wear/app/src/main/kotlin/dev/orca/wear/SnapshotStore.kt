package dev.orca.wear

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "orca_fleet")
private val SNAPSHOT_KEY = stringPreferencesKey("snapshot_json")

/** DataStore cache of the last snapshot received. Tile and complication read it cold. */
class SnapshotStore(private val context: Context) {
    val snapshot: Flow<WatchSnapshot?> =
        context.applicationContext.dataStore.data.map { SnapshotCodec.decode(it[SNAPSHOT_KEY]) }

    suspend fun current(): WatchSnapshot? = snapshot.first()

    suspend fun save(s: WatchSnapshot) {
        context.applicationContext.dataStore.edit { it[SNAPSHOT_KEY] = SnapshotCodec.encode(s) }
        SurfaceRefresh.requestAll(context)
    }

    suspend fun clear() {
        context.applicationContext.dataStore.edit { it.remove(SNAPSHOT_KEY) }
        SurfaceRefresh.requestAll(context)
    }
}

/** Whether the phone is reachable; the Data Layer poller and the debug sender write it. */
object PhoneLink {
    private val _reachable = MutableStateFlow(true)
    val reachable: StateFlow<Boolean> = _reachable
    fun set(value: Boolean) { _reachable.value = value }
}
