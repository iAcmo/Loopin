package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Subtle liquid glassmorphic background & surface colors
val LoopinBg = Color(0xFF08090E)
val LoopinBgSecondary = Color(0xFF0E1018)
val LoopinSurface = Color(0xFF141724)
val LoopinSurfaceElevated = Color(0xFF1A1E2E)

// Liquid Frosted Glass Colors
val GlassFill = Color(0x14FFFFFF)
val GlassFillElevated = Color(0x22FFFFFF)
val GlassFillSubtle = Color(0x0AFFFFFF)
val GlassBorder = Color(0x28FFFFFF)
val GlassBorderStrong = Color(0x55FFFFFF)

// Typography
val TextPrimary = Color(0xFFF8F9FD)
val TextSecondary = Color(0xFFA2A7BC)
val TextMuted = Color(0xFF696E84)

// Subtle Accents
val LoopinIris = Color(0xFF7E74F1)
val LoopinIrisGlow = Color(0xFFA69FFF)
val LoopinSilver = Color(0xFFE2E5EE)
val LoopinLikeRed = Color(0xFFFF4869)
val LoopinOnlineGreen = Color(0xFF22C55E)

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
val InstaTextPrimary = TextPrimary
val InstaTextSecondary = TextSecondary
val InstaBorder = GlassBorder
val InstaPink = LoopinIris
val InstaYellow = LoopinIrisGlow
val InstaLikeRed = LoopinLikeRed
val InstaStoryRingGradient = FrostedStoryRingGradient
val InstaGradientBrush = GlassStrokeBrush

