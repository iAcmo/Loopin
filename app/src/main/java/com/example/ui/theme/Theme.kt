package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = compositionLocalOf { true }

private val DarkGlassColorScheme = darkColorScheme(
    primary = LoopinIris,
    onPrimary = Color.White,
    primaryContainer = LoopinSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = LoopinSilver,
    onSecondary = Color.Black,
    tertiary = LoopinIrisGlow,
    background = LoopinBg,
    onBackground = TextPrimary,
    surface = LoopinSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassFill,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

private val LightGlassColorScheme = lightColorScheme(
    primary = LoopinIris,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EAF6),
    onPrimaryContainer = Color(0xFF141728),
    secondary = Color(0xFF4C5268),
    onSecondary = Color.White,
    tertiary = LoopinIris,
    background = Color(0xFFF1F3F9),
    onBackground = Color(0xFF141728),
    surface = Color(0xFFFCFCFD),
    onSurface = Color(0xFF141728),
    surfaceVariant = Color(0xE6FFFFFF),
    onSurfaceVariant = Color(0xFF5B6178),
    outline = Color(0x28000000)
)

@Composable
fun LoopinTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
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
