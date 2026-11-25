package dev.orca.wear

import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FleetPresentationTest {
    private val now = 1_700_000_000_000L
    private val utc = ZoneOffset.UTC

    private fun row(s: AgentStatus, at: Long, name: String = "a") = AgentRow(name, "wt", s, "", at)

    @Test
    fun needsYouSortsFirstThenFailedWorkingDone() {
        val sorted = FleetPresentation.sorted(
            listOf(
                row(AgentStatus.DONE, 5), row(AgentStatus.WORKING, 4), row(AgentStatus.FAILED, 3),
                row(AgentStatus.NEEDS_YOU, 1, "old"), row(AgentStatus.NEEDS_YOU, 2, "new"),
            ),
        )
        assertEquals(listOf("new", "old"), sorted.take(2).map { it.agent })
        assertEquals(
            listOf(AgentStatus.FAILED, AgentStatus.WORKING, AgentStatus.DONE),
            sorted.drop(2).map { it.status },
        )
    }

    @Test
    fun wordsMatchOrca() {
        assertEquals(
            listOf("needs you", "working", "done", "failed"),
            AgentStatus.entries.map(FleetPresentation::word),
        )
    }

    @Test
    fun noSnapshotIsNoDataAndCarriesReachability() {
        assertEquals(FleetUi.NoData(true), FleetPresentation.toUi(null, false, now))
        assertEquals(FleetUi.NoData(false), FleetPresentation.toUi(null, true, now))
    }

    @Test
    fun emptySnapshotIsEmpty() {
        val ui = FleetPresentation.toUi(WatchSnapshot(sentAt = now), true, now, utc)
        assertTrue(ui is FleetUi.Empty)
    }

    @Test
    fun staleAndUnreachableNotices() {
        val s = WatchSnapshot(counts = Counts(1, 0, 0), rows = listOf(row(AgentStatus.NEEDS_YOU, 1)), sentAt = now)
        val fresh = FleetPresentation.toUi(s, true, now + 1_000, utc) as FleetUi.List
        assertEquals(null, fresh.notice)
        val stale = FleetPresentation.toUi(s, true, now + FleetPresentation.STALE_AFTER_MS + 1, utc) as FleetUi.List
        assertEquals(Notice.STALE, stale.notice)
        val away = FleetPresentation.toUi(s, false, now, utc) as FleetUi.List
        assertEquals(Notice.PHONE_UNREACHABLE, away.notice)
    }

    @Test
    fun asOfFormatsHoursAndMinutes() {
        // 2023-11-14 22:13:20 UTC
        assertEquals("as of 22:13", FleetPresentation.asOf(now, utc))
    }

    @Test
    fun codecRoundTripAndTolerance() {
        val s = WatchSnapshot(counts = Counts(2, 4, 7), rows = listOf(row(AgentStatus.NEEDS_YOU, 9)), sentAt = now)
        assertEquals(s, SnapshotCodec.decode(SnapshotCodec.encode(s)))
        assertEquals(null, SnapshotCodec.decode("not json"))
        assertEquals(1, SnapshotCodec.decode("""{"version":1,"future":true}""")?.version)
    }

    @Test
    fun complicationShowsNeedsYouCount() {
        assertEquals("0", FleetPresentation.complicationText(null))
        assertEquals("3", FleetPresentation.complicationText(WatchSnapshot(counts = Counts(needsYou = 3))))
    }
}
