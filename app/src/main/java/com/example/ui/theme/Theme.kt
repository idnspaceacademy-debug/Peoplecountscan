package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandAccent,
    onPrimary = TextInverse,
    primaryContainer = AppSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = TechCyan,
    onSecondary = TextInverse,
    secondaryContainer = AppCardElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = StatusWarning,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AppCardBorder,
    error = StatusDanger,
    onError = TextPrimary,
    errorContainer = StatusDangerBg,
    onErrorContainer = StatusDanger
)

@Composable
fun PeopleCountTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AppBackground.toArgb()
                window.navigationBarColor = AppBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for test compatibility
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    PeopleCountTheme(content = content)
}
