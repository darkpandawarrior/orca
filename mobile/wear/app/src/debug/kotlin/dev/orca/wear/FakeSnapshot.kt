package dev.orca.wear

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Debug-only fake snapshots so the app renders without a phone. */
object FakeSnapshot {
    fun fleet(now: Long = System.currentTimeMillis()) = WatchSnapshot(
        hosts = listOf("studio-mac"),
        counts = Counts(needsYou = 2, working = 4, done = 7),
        sentAt = now,
        rows = listOf(
            AgentRow("Claude", "orca-wear", AgentStatus.NEEDS_YOU, "Run: pnpm test", now - 60_000),
            AgentRow("Codex", "api-fix", AgentStatus.NEEDS_YOU, "Reply: add retry to the client?", now - 180_000),
            AgentRow("Claude", "docs-sweep", AgentStatus.WORKING, "Editing README.md", now - 20_000),
            AgentRow("Codex", "perf", AgentStatus.WORKING, "Running benchmarks", now - 90_000),
            AgentRow("Claude", "login-bug", AgentStatus.FAILED, "Typecheck failed in auth.ts", now - 400_000),
            AgentRow("Claude", "release", AgentStatus.DONE, "Opened PR 142", now - 900_000),
        ),
    )

    fun empty(now: Long = System.currentTimeMillis()) = WatchSnapshot(hosts = listOf("studio-mac"), sentAt = now)
}

object DebugSeeder {
    fun seedIfEmpty(context: Context) {
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val store = SnapshotStore(app)
            if (store.current() == null) store.save(FakeSnapshot.fleet())
        }
    }
}
