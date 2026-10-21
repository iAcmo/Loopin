package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = compositionLocalOf { true }

private val DarkGlassColorScheme = darkColorScheme(
    primary = LoopinIris,
    onPrimary = Color.White,
    primaryContainer = LoopinSurfaceElevated,
    onPrimaryContainer = DarkTextPrimary,
    secondary = LoopinSilver,
    onSecondary = Color.Black,
    tertiary = LoopinIrisGlow,
    background = LoopinBg,
    onBackground = DarkTextPrimary,
    surface = LoopinSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkGlassFill,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder
)

private val LightGlassColorScheme = lightColorScheme(
    primary = LoopinIris,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = LightTextPrimary,
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    tertiary = LoopinIris,
    background = Color(0xFFF6F8FC),
    onBackground = LightTextPrimary,
    surface = Color(0xFFFFFFFF),
    onSurface = LightTextPrimary,
    surfaceVariant = LightGlassFill,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder
)

@Composable
fun LoopinTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkGlassColorScheme else LightGlassColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun InstaSquadTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = LoopinTheme(darkTheme = darkTheme, content = content)
