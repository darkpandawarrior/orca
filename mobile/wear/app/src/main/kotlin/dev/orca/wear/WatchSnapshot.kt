package dev.orca.wear

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Words match Orca's agent-status presentation: needs you, working, done, failed. */
@Serializable
enum class AgentStatus {
    @SerialName("needs_you") NEEDS_YOU,
    @SerialName("working") WORKING,
    @SerialName("done") DONE,
    @SerialName("failed") FAILED,
}

@Serializable
data class AgentRow(
    val agent: String,
    val worktree: String,
    val status: AgentStatus,
    val summary: String = "",
    val updatedAt: Long,
)

@Serializable
data class Counts(val needsYou: Int = 0, val working: Int = 0, val done: Int = 0)

/** Read-only fleet snapshot the phone sends on Data Layer path [FLEET_PATH]. */
@Serializable
data class WatchSnapshot(
    val version: Int = 1,
    val hosts: List<String> = emptyList(),
    val counts: Counts = Counts(),
    val rows: List<AgentRow> = emptyList(),
    /** When the phone built this snapshot (epoch millis); drives "as of HH:MM". */
    val sentAt: Long = 0L,
)

const val FLEET_PATH = "/orca/fleet"
const val PAYLOAD_KEY = "json"

object SnapshotCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun encode(s: WatchSnapshot): String = json.encodeToString(WatchSnapshot.serializer(), s)

    fun decode(text: String?): WatchSnapshot? =
        text?.let { runCatching { json.decodeFromString(WatchSnapshot.serializer(), it) }.getOrNull() }
}
