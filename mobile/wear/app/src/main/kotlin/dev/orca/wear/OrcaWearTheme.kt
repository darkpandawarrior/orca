package dev.orca.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/** Orca mobile palette (mobile/src/theme/mobile-theme.ts) mapped onto Wear Material 3 roles. */
object OrcaColors {
    val bgBase = Color(0xFF0A0A0A)
    val bgPanel = Color(0xFF171717)
    val bgRaised = Color(0xFF262626)
    val textPrimary = Color(0xFFFAFAFA)
    val textSecondary = Color(0xFFACACAC)
    val textMuted = Color(0xFF898989)
    val accent = Color(0xFF3276F2)
}

private val scheme = ColorScheme(
    primary = OrcaColors.accent,
    onPrimary = Color.White,
    surfaceContainerLow = OrcaColors.bgPanel,
    surfaceContainer = OrcaColors.bgPanel,
    surfaceContainerHigh = OrcaColors.bgRaised,
    onSurface = OrcaColors.textPrimary,
    onSurfaceVariant = OrcaColors.textSecondary,
    background = OrcaColors.bgBase,
    onBackground = OrcaColors.textPrimary,
    error = FleetPresentation.failedColor,
    onError = Color(0xFF450A0A),
)

@Composable
fun OrcaWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
