package dev.orca.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Debug sender. state = fleet | empty | stale | none; unreachable = true | false.
 * adb shell am broadcast -a dev.orca.wear.DEBUG_SNAPSHOT -n dev.orca.app/dev.orca.wear.DebugSnapshotReceiver --es state stale
 */
class DebugSnapshotReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        intent.getStringExtra("unreachable")?.let { PhoneLink.set(it != "true") }
        val state = intent.getStringExtra("state") ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val store = SnapshotStore(app)
            when (state) {
                "fleet" -> store.save(FakeSnapshot.fleet())
                "empty" -> store.save(FakeSnapshot.empty())
                "stale" -> store.save(FakeSnapshot.fleet(System.currentTimeMillis() - 45 * 60_000L))
                "none" -> store.clear()
            }
            pending.finish()
        }
    }
}
