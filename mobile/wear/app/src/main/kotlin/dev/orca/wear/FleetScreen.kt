package dev.orca.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text

@Composable
fun FleetRoot(vm: FleetViewModel = viewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    OrcaWearTheme { FleetScreen(ui) }
}

/** Stateless, so Roborazzi can render it. [showTime] is off in goldens (the clock drifts). */
@Composable
fun FleetScreen(ui: FleetUi?, showTime: Boolean = true) {
    AppScaffold {
        val listState = rememberScalingLazyListState()
        ScreenScaffold(scrollState = listState, timeText = if (showTime) ({ androidx.wear.compose.material3.TimeText() }) else ({})) {
            when (ui) {
                null -> Message("Orca", "Loading")
                is FleetUi.NoData ->
                    if (ui.phoneUnreachable) Message("Phone unreachable", "Open Orca on your phone, then check Bluetooth.")
                    else Message("Waiting for phone", "Open Orca on your phone to sync.")
                is FleetUi.Empty ->
                    Message("No agents running", noticeText(ui.notice, ui.asOf), noticeColor(ui.notice))
                is FleetUi.List -> FleetList(ui, listState)
            }
        }
    }
}

private fun noticeText(n: Notice?, asOf: String) = when (n) {
    Notice.PHONE_UNREACHABLE -> "Phone unreachable, $asOf"
    else -> asOf
}

private fun noticeColor(n: Notice?) = if (n == null) OrcaColors.textMuted else FleetPresentation.workingColor

@Composable
private fun Message(title: String, body: String, bodyColor: Color = OrcaColors.textSecondary) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodySmall, color = bodyColor, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FleetList(ui: FleetUi.List, state: androidx.wear.compose.foundation.lazy.ScalingLazyListState) {
    ScalingLazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
        item { CountsHeader(ui.counts) }
        item {
            Text(
                noticeText(ui.notice, ui.asOf),
                style = MaterialTheme.typography.labelSmall,
                color = noticeColor(ui.notice),
            )
        }
        items(ui.rows, key = { it.agent + "/" + it.worktree }) { AgentRowCard(it) }
    }
}

@Composable
private fun CountsHeader(c: Counts) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Count(c.needsYou, "needs you", FleetPresentation.needsYouColor)
        Count(c.working, "working", FleetPresentation.workingColor)
        Count(c.done, "done", FleetPresentation.doneColor)
    }
}

@Composable
private fun Count(n: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(n.toString(), style = MaterialTheme.typography.numeralSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OrcaColors.textSecondary)
    }
}

@Composable
private fun AgentRowCard(row: AgentRow) {
    val color = FleetPresentation.color(row.status)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(OrcaColors.bgPanel)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.padding(top = 5.dp, end = 8.dp).size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Column(Modifier.weight(1f)) {
            Text(
                "${row.agent} · ${row.worktree}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Status as a word as well as a colour, so state is never colour-only.
            Text(FleetPresentation.word(row.status), style = MaterialTheme.typography.labelSmall, color = color)
            if (row.summary.isNotBlank()) {
                Text(
                    row.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = OrcaColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
