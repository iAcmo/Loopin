package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalIsDarkTheme

/**
 * LiquidGlassHeader
 * Premium liquid glassmorphic top header bar featuring:
 * - Backdrop frosted blur & refractive glass base
 * - Specular Fresnel glow along the top boundary
 * - Dynamic brightness & luminance adjustments
 * - Z-radius depth with ambient & directional shadows
 * - Chromatic aberration (prismatic dispersion with subtle cyan & magenta fringe reflections)
 */
@Composable
fun LiquidGlassHeader(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
    zRadius: Dp = 10.dp,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accentColor = LocalAppAccentColor.current

    // Fresnel specular colors
    val fresnelTopGlint = if (isDark) Color(0x66FFFFFF) else Color(0x99FFFFFF)
    val fresnelAccentGlint = accentColor.copy(alpha = if (isDark) 0.35f else 0.20f)
    val glassFill = if (isDark) Color(0x22121720) else Color(0xD8FFFFFF)
    val borderGradient = Brush.verticalGradient(
        colors = listOf(
            fresnelTopGlint,
            accentColor.copy(alpha = 0.20f),
            if (isDark) Color(0x18FFFFFF) else Color(0x20000000)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = zRadius,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.22f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(glassFill)
            .border(width = 1.dp, brush = borderGradient, shape = shape)
            .drawBehind {
                // 1. Fresnel specular top arc
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            fresnelTopGlint,
                            fresnelAccentGlint,
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.45f
                    )
                )

                // 2. Chromatic aberration fringe (Cyan & Magenta dispersion along the bottom glass boundary)
                val bottomY = size.height
                // Cyan dispersion line offset slightly up-left
                drawLine(
                    color = Color(0x3500E5FF),
                    start = Offset(0f, bottomY - 1.5f),
                    end = Offset(size.width, bottomY - 1.5f),
                    strokeWidth = 1.5f
                )
                // Magenta dispersion line offset slightly down-right
                drawLine(
                    color = Color(0x35FF007F),
                    start = Offset(0f, bottomY - 0.5f),
                    end = Offset(size.width, bottomY - 0.5f),
                    strokeWidth = 1.5f
                )
            }
    ) {
        // Internal content row spaced with status bar padding for immersive edge-to-edge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
