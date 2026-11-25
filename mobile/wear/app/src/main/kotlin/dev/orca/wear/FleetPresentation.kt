package dev.orca.wear

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Pure mapping from snapshot to what the surfaces show, so it is unit-testable. */
sealed interface FleetUi {
    /** No snapshot has ever arrived. */
    data class NoData(val phoneUnreachable: Boolean) : FleetUi

    /** Snapshot received, nothing running. */
    data class Empty(val asOf: String, val notice: Notice?) : FleetUi

    data class List(
        val counts: Counts,
        val rows: kotlin.collections.List<AgentRow>,
        val asOf: String,
        val notice: Notice?,
    ) : FleetUi
}

enum class Notice { PHONE_UNREACHABLE, STALE }

object FleetPresentation {
    const val STALE_AFTER_MS = 10 * 60_000L

    // Mobile theme: agentQuestion, statusAmber, statusGreen, statusRed.
    val needsYouColor = Color(0xFFF97316)
    val workingColor = Color(0xFFEAB308)
    val doneColor = Color(0xFF86EFAC)
    val failedColor = Color(0xFFFF6568)

    fun word(s: AgentStatus): String = when (s) {
        AgentStatus.NEEDS_YOU -> "needs you"
        AgentStatus.WORKING -> "working"
        AgentStatus.DONE -> "done"
        AgentStatus.FAILED -> "failed"
    }

    fun color(s: AgentStatus): Color = when (s) {
        AgentStatus.NEEDS_YOU -> needsYouColor
        AgentStatus.WORKING -> workingColor
        AgentStatus.DONE -> doneColor
        AgentStatus.FAILED -> failedColor
    }

    /** Desktop attention rank: needs you, failed, working, done; newest first inside a status. */
    private fun rank(s: AgentStatus) = when (s) {
        AgentStatus.NEEDS_YOU -> 0
        AgentStatus.FAILED -> 1
        AgentStatus.WORKING -> 2
        AgentStatus.DONE -> 3
    }

    fun sorted(rows: kotlin.collections.List<AgentRow>) =
        rows.sortedWith(compareBy<AgentRow> { rank(it.status) }.thenByDescending { it.updatedAt })

    fun asOf(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        "as of " + DateTimeFormatter.ofPattern("HH:mm").format(Instant.ofEpochMilli(millis).atZone(zone))

    fun isStale(s: WatchSnapshot, now: Long) = now - s.sentAt > STALE_AFTER_MS

    fun toUi(
        snapshot: WatchSnapshot?,
        phoneReachable: Boolean,
        now: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): FleetUi {
        if (snapshot == null) return FleetUi.NoData(phoneUnreachable = !phoneReachable)
        val notice = when {
            !phoneReachable -> Notice.PHONE_UNREACHABLE
            isStale(snapshot, now) -> Notice.STALE
            else -> null
        }
        val asOf = asOf(snapshot.sentAt, zone)
        return if (snapshot.rows.isEmpty() && snapshot.counts == Counts()) {
            FleetUi.Empty(asOf, notice)
        } else {
            FleetUi.List(snapshot.counts, sorted(snapshot.rows), asOf, notice)
        }
    }

    /** SHORT_TEXT complication text: the needs-you count, "0" when nothing waits. */
    fun complicationText(s: WatchSnapshot?): String = (s?.counts?.needsYou ?: 0).toString()
}
