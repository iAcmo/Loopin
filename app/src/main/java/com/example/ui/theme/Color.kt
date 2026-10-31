package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Subtle liquid glassmorphic background & surface colors (Dark theme underlying color #0C1014)
val LoopinBg = Color(0xFF0C1014)
val LoopinBgSecondary = Color(0xFF10151C)
val LoopinSurface = Color(0xFF151C24)
val LoopinSurfaceElevated = Color(0xFF1C2530)

// Liquid Frosted Glass Colors
val DarkGlassFill = Color(0x14FFFFFF)
val DarkGlassFillElevated = Color(0x22FFFFFF)
val DarkGlassFillSubtle = Color(0x0AFFFFFF)
val DarkGlassBorder = Color(0x28FFFFFF)
val DarkGlassBorderStrong = Color(0x55FFFFFF)

val LightGlassFill = Color(0xF2FFFFFF)
val LightGlassFillElevated = Color(0xFAFFFFFF)
val LightGlassFillSubtle = Color(0xE0FFFFFF)
val LightGlassBorder = Color(0x1F000000)
val LightGlassBorderStrong = Color(0x33000000)

val GlassFill: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkGlassFill else LightGlassFill

val GlassFillElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkGlassFillElevated else LightGlassFillElevated

val GlassFillSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkGlassFillSubtle else LightGlassFillSubtle

val GlassBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkGlassBorder else LightGlassBorder

val GlassBorderStrong: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkGlassBorderStrong else LightGlassBorderStrong

// Typography
val DarkTextPrimary = Color(0xFFF8F9FD)
val DarkTextSecondary = Color(0xFFA2A7BC)
val DarkTextMuted = Color(0xFF696E84)

val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF64748B)

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextPrimary else LightTextPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextSecondary else LightTextSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DarkTextMuted else LightTextMuted

// Subtle Accents
val LoopinIris = Color(0xFF7E74F1)
val LoopinIrisGlow = Color(0xFFA69FFF)
val LoopinSilver = Color(0xFFE2E5EE)
val LoopinLikeRed = Color(0xFFFF4869)
val LoopinOnlineGreen = Color(0xFF22C55E)

data class AccentPreset(
    val id: String,
    val name: String,
    val color: Color,
    val glow: Color
)

val PRESET_ACCENT_COLORS = listOf(
    AccentPreset("iris", "Signature Iris", Color(0xFF7E74F1), Color(0xFFA69FFF)),
    AccentPreset("cyan", "Cyber Cyan", Color(0xFF06B6D4), Color(0xFF67E8F9)),
    AccentPreset("emerald", "Neon Emerald", Color(0xFF10B981), Color(0xFF6EE7B7)),
    AccentPreset("ember", "Sunset Ember", Color(0xFFF97316), Color(0xFFFDBA74)),
    AccentPreset("rose", "Rose Quartz", Color(0xFFF43F5E), Color(0xFFFDA4AF)),
    AccentPreset("violet", "Electric Violet", Color(0xFFA855F7), Color(0xFFD8B4FE)),
    AccentPreset("amber", "Golden Amber", Color(0xFFF59E0B), Color(0xFFFCD34D)),
    AccentPreset("sky", "Sky Blue", Color(0xFF0284C7), Color(0xFF7DD3FC))
)

// Liquid Frosted Brushes
val GlassStrokeBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x66FFFFFF),
        Color(0x1AFFFFFF),
        Color(0x40FFFFFF)
    )
)

val LiquidMeshBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF101322),
        Color(0xFF08090E),
        Color(0xFF0C0E16)
    )
)

val FrostedStoryRingGradient = Brush.sweepGradient(
    colors = listOf(
        LoopinIrisGlow,
        Color(0xFFC7C5FF),
        LoopinSilver,
        LoopinIris,
        LoopinIrisGlow
    )
)

val FrostedPillBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x26FFFFFF),
        Color(0x12FFFFFF)
    )
)

val LiquidButtonBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6B61ED),
        Color(0xFF8980F5)
    )
)

// Legacy aliases gracefully redirected to subtle glassmorphic palette
val InstaDark = LoopinBg
val InstaDarkCard = LoopinSurface
val InstaDarkElevated = LoopinSurfaceElevated
val InstaPink = LoopinIris
val InstaYellow = LoopinIrisGlow
val InstaLikeRed = LoopinLikeRed
val InstaStoryRingGradient = FrostedStoryRingGradient
val InstaGradientBrush = GlassStrokeBrush
