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

enum class VisualThemeMode(val id: String, val title: String, val description: String) {
    LIQUID_GLASS(
        id = "liquid_glass",
        title = "Liquid Glass",
        description = "Cosmic translucent frosted glass with pointer highlights & dynamic glows"
    ),
    STATIC_COLORS(
        id = "static_colors",
        title = "Static Colors",
        description = "Clean solid Material 3 surfaces with crisp high-contrast borders & opaque cards"
    )
}

val LocalIsDarkTheme = compositionLocalOf { true }
val LocalVisualThemeMode = compositionLocalOf { VisualThemeMode.LIQUID_GLASS }
val LocalAppAccentColor = compositionLocalOf { LoopinIris }

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

private val DarkStaticColorScheme = darkColorScheme(
    primary = LoopinIris,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF27272A),
    onPrimaryContainer = Color(0xFFF4F4F5),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color.Black,
    tertiary = LoopinIrisGlow,
    background = Color(0xFF18181B),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF27272A),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF3F3F46),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF52525B)
)

private val LightStaticColorScheme = lightColorScheme(
    primary = LoopinIris,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF18181B),
    secondary = Color(0xFF71717A),
    onSecondary = Color.White,
    tertiary = LoopinIris,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF18181B),
    surface = Color(0xFFF4F4F5),
    onSurface = Color(0xFF18181B),
    surfaceVariant = Color(0xFFE4E4E7),
    onSurfaceVariant = Color(0xFF71717A),
    outline = Color(0xFFD4D4D8)
)

@Composable
fun LoopinTheme(
    darkTheme: Boolean = true,
    visualThemeMode: VisualThemeMode = VisualThemeMode.LIQUID_GLASS,
    accentColor: Color = LoopinIris,
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

    val baseColorScheme = when {
        visualThemeMode == VisualThemeMode.STATIC_COLORS && darkTheme -> DarkStaticColorScheme
        visualThemeMode == VisualThemeMode.STATIC_COLORS && !darkTheme -> LightStaticColorScheme
        darkTheme -> DarkGlassColorScheme
        else -> LightGlassColorScheme
    }

    val dynamicColorScheme = baseColorScheme.copy(
        primary = accentColor,
        tertiary = accentColor.copy(alpha = 0.85f)
    )

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalVisualThemeMode provides visualThemeMode,
        LocalAppAccentColor provides accentColor
    ) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun InstaSquadTheme(
    darkTheme: Boolean = true,
    visualThemeMode: VisualThemeMode = VisualThemeMode.LIQUID_GLASS,
    accentColor: Color = LoopinIris,
    content: @Composable () -> Unit
) = LoopinTheme(darkTheme = darkTheme, visualThemeMode = visualThemeMode, accentColor = accentColor, content = content)
