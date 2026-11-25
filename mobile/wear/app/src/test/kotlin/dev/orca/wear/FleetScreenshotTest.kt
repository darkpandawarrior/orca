package dev.orca.wear

import android.app.Application
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import java.time.ZoneOffset
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Round-screen goldens, no emulator. Output: docs/screens/roborazzi_*.png.
 * Record with ROBORAZZI_RECORD=true; the default verifies.
 */
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w227dp-h227dp-round-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FleetScreenshotTest {
    companion object {
        private val dir: File by lazy {
            val base = File(System.getProperty("user.dir") ?: ".")
            File(base.parentFile, "docs/screens").also { it.mkdirs() }
        }

        @BeforeClass
        @JvmStatic
        fun setup() {
            if (System.getenv("ROBORAZZI_RECORD") == "true") System.setProperty("roborazzi.test.record", "true")
        }
    }

    private val now = 1_700_000_000_000L
    private val fleet = FakeSnapshotForTest.fleet(now)

    private fun shot(name: String, ui: FleetUi) {
        captureRoboImage(File(dir, "roborazzi_$name.png").absolutePath) {
            OrcaWearTheme { FleetScreen(ui, showTime = false) }
        }
    }

    @Test fun list() = shot("list", FleetPresentation.toUi(fleet, true, now + 1_000, ZoneOffset.UTC)!!)

    @Test fun stale() =
        shot("stale", FleetPresentation.toUi(fleet, true, now + 45 * 60_000L, ZoneOffset.UTC))

    @Test fun phoneUnreachable() = shot("unreachable", FleetUi.NoData(phoneUnreachable = true))

    @Test fun empty() =
        shot("empty", FleetPresentation.toUi(WatchSnapshot(sentAt = now), true, now, ZoneOffset.UTC))
}

/** Mirrors the debug FakeSnapshot; the debug source set is not on the unit-test classpath for release. */
object FakeSnapshotForTest {
    fun fleet(now: Long) = WatchSnapshot(
        counts = Counts(2, 4, 7),
        sentAt = now,
        rows = listOf(
            AgentRow("Claude", "orca-wear", AgentStatus.NEEDS_YOU, "Run: pnpm test", now - 60_000),
            AgentRow("Codex", "api-fix", AgentStatus.NEEDS_YOU, "Reply: add retry to the client?", now - 180_000),
            AgentRow("Claude", "docs-sweep", AgentStatus.WORKING, "Editing README.md", now - 20_000),
            AgentRow("Claude", "login-bug", AgentStatus.FAILED, "Typecheck failed in auth.ts", now - 400_000),
            AgentRow("Claude", "release", AgentStatus.DONE, "Opened PR 142", now - 900_000),
        ),
    )
}
