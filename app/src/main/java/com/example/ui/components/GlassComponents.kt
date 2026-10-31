package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassBorderStrong
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderStrong
import com.example.ui.theme.GlassFill
import com.example.ui.theme.GlassFillElevated
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.LightGlassBorderStrong
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalVisualThemeMode
import com.example.ui.theme.LoopinBg
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VisualThemeMode
import kotlin.math.max

/**
 * Tracks touch / drag pointer positions over a glass surface and animates
 * a dynamic specular radial highlight center with spring physics.
 */
@Composable
fun rememberLiquidHighlightOffset(
    defaultOffsetFraction: Offset = Offset(0.35f, 0.25f)
): Pair<State<Offset>, Modifier> {
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val targetOffset = remember(touchPosition, containerSize) {
        touchPosition ?: Offset(
            containerSize.width * defaultOffsetFraction.x,
            containerSize.height * defaultOffsetFraction.y
        )
    }

    val animatedOffset = animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquidHighlightOffset"
    )

    val pointerModifier = Modifier
        .onSizeChanged { containerSize = it }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull()
                    if (change != null) {
                        when (event.type) {
                            PointerEventType.Press, PointerEventType.Move -> {
                                touchPosition = change.position
                            }
                            PointerEventType.Release, PointerEventType.Exit -> {
                                touchPosition = null
                            }
                        }
                    }
                }
            }
        }

    return Pair(animatedOffset, pointerModifier)
}

/**
 * Ambient Liquid Glass background with luminous soft refractions.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val visualMode = LocalVisualThemeMode.current
    val isStatic = visualMode == VisualThemeMode.STATIC_COLORS

    val bgColor = if (isDark) {
        if (isStatic) Color(0xFF18181B) else LoopinBg
    } else {
        if (isStatic) Color(0xFFFFFFFF) else Color(0xFFF1F3F9)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (!isStatic) {
            // Ambient soft liquid light refractions with optical blur behind frosted glass
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 32.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            ) {
                if (isDark) {
                    // Upper subtle indigo liquid light
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x354B3FA8), Color(0x123A2F8A), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.15f),
                            radius = size.width * 0.85f
                        ),
                        radius = size.width * 0.85f,
                        center = Offset(size.width * 0.2f, size.height * 0.15f)
                    )

                    // Center-right chromatic prism refraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x22EC4899), Color(0x108B5CF6), Color.Transparent),
                            center = Offset(size.width * 0.65f, size.height * 0.42f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width * 0.65f, size.height * 0.42f)
                    )

                    // Lower subtle teal/slate liquid refraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x28233C57), Color(0x0E182A3E), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.75f),
                            radius = size.width * 0.9f
                        ),
                        radius = size.width * 0.9f,
                        center = Offset(size.width * 0.85f, size.height * 0.75f)
                    )
                } else {
                    // Light mode: Soft lavender and luminous sky liquid bloom
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x30A59EFF), Color(0x128A82F0), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.15f),
                            radius = size.width * 0.85f
                        ),
                        radius = size.width * 0.85f,
                        center = Offset(size.width * 0.2f, size.height * 0.15f)
                    )

                    // Center prismatic chromatic refraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x22F43F5E), Color(0x12818CF8), Color.Transparent),
                            center = Offset(size.width * 0.65f, size.height * 0.42f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width * 0.65f, size.height * 0.42f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x287EC2FF), Color(0x0C9FD3FF), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.75f),
                            radius = size.width * 0.9f
                        ),
                        radius = size.width * 0.9f,
                        center = Offset(size.width * 0.85f, size.height * 0.75f)
                    )
                }
            }
        }

        content()
    }
}

/**
 * Reusable GlassCard:
 * - Frosted translucent container with multi-stop border gradient (or solid opaque in static mode)
 * - Dynamic pointer-based specular highlight that glides with touch/drag
 * - Soft diffuse shadow and optional click scale animation
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isElevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    borderBrush: Brush? = null,
    enableHighlightMovement: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val visualMode = LocalVisualThemeMode.current
    val isStatic = visualMode == VisualThemeMode.STATIC_COLORS

    val fill = if (isStatic) {
        if (isDark) {
            if (isElevated) Color(0xFF27272A) else Color(0xFF202023)
        } else {
            if (isElevated) Color(0xFFFFFFFF) else Color(0xFFF8F9FA)
        }
    } else {
        if (isDark) {
            if (isElevated) GlassFillElevated else GlassFill
        } else {
            if (isElevated) Color(0xF4FFFFFF) else Color(0xDDFFFFFF)
        }
    }

    val resolvedBorderBrush = borderBrush ?: if (isStatic) {
        val solidBorderColor = if (isDark) Color(0xFF3F3F46) else Color(0xFFE2E8F0)
        Brush.linearGradient(listOf(solidBorderColor, solidBorderColor))
    } else if (isDark) {
        GlassStrokeBrush
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x60000000),
                Color(0x18000000),
                Color(0x35000000)
            )
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "glassCardScale"
    )

    val (highlightOffsetState, pointerModifier) = if (!isStatic && enableHighlightMovement) {
        rememberLiquidHighlightOffset(defaultOffsetFraction = Offset(0.3f, 0.2f))
    } else {
        Pair(remember { mutableStateOf(Offset.Zero) }, Modifier)
    }

    val specularColor = if (isDark) {
        if (isPressed) Color(0x40FFFFFF) else Color(0x20FFFFFF)
    } else {
        if (isPressed) Color(0x70FFFFFF) else Color(0x45FFFFFF)
    }

    val tintRefractionColor = if (isDark) Color(0x127E74F1) else Color(0x1E7E74F1)

    val clickableMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(scaleAnim)
            .shadow(
                elevation = if (isElevated) 8.dp else 3.dp,
                shape = shape,
                ambientColor = if (isDark) Color(0x44000000) else Color(0x1A000000),
                spotColor = if (isDark) Color(0x337E74F1) else Color(0x12000000)
            )
            .clip(shape)
            .background(fill)
            .then(pointerModifier)
            .drawBehind {
                if (!isStatic && enableHighlightMovement) {
                    val center = highlightOffsetState.value
                    val radius = max(size.width, size.height) * 0.75f
                    // 1. Primary refractive specular glint following pointer
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(specularColor, tintRefractionColor, Color.Transparent),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // 2. Chromatic refraction split (prismatic light dispersion at glass curvature)
                    val chromaticOffset = Offset(18f, 14f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isDark) Color(0x18A855F7) else Color(0x22818CF8),
                                Color.Transparent
                            ),
                            center = center + chromaticOffset,
                            radius = radius * 0.65f
                        ),
                        radius = radius * 0.65f,
                        center = center + chromaticOffset
                    )

                    // 3. Specular rim refraction sheen across the glass surface
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                if (isDark) Color(0x26FFFFFF) else Color(0x48FFFFFF),
                                Color.Transparent,
                                if (isDark) Color(0x0C7E74F1) else Color(0x16818CF8)
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                    )
                }
            }
            .border(
                width = 1.dp,
                brush = resolvedBorderBrush,
                shape = shape
            )
            .then(clickableMod),
        content = content
    )
}

/**
 * Reusable GlassButton:
 * - Spring scale feedback on press
 * - Pointer-based highlight refraction across button face
 * - Tactile feel with gradient border and accessible >= 48dp touch target
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isPrimary: Boolean = true,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    testTag: String? = null
) {
    val isDark = LocalIsDarkTheme.current
    val visualMode = LocalVisualThemeMode.current
    val isStatic = visualMode == VisualThemeMode.STATIC_COLORS

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "glassButtonScale"
    )

    val (highlightOffsetState, pointerModifier) = if (!isStatic) {
        rememberLiquidHighlightOffset(defaultOffsetFraction = Offset(0.4f, 0.3f))
    } else {
        Pair(remember { mutableStateOf(Offset.Zero) }, Modifier)
    }

    val bgBrush = if (isStatic) {
        if (isPrimary) {
            Brush.horizontalGradient(listOf(Color(0xFF6B61ED), Color(0xFF7E74F1)))
        } else {
            if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF27272A), Color(0xFF27272A)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFF1F5F9)))
            }
        }
    } else {
        if (isPrimary) {
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xEE7A6FFA),
                    Color(0xEE9086FA)
                )
            )
        } else {
            if (isDark) {
                Brush.horizontalGradient(listOf(Color(0x28FFFFFF), Color(0x14FFFFFF)))
            } else {
                Brush.horizontalGradient(listOf(Color(0x1C000000), Color(0x0C000000)))
            }
        }
    }

    val borderStroke = if (isStatic) {
        if (isPrimary) Color(0xFF7E74F1) else if (isDark) Color(0xFF3F3F46) else Color(0xFFCBD5E1)
    } else {
        if (isPrimary) {
            GlassBorderStrong
        } else if (isDark) {
            GlassBorder
        } else {
            Color(0x26000000)
        }
    }

    val contentColor = if (isPrimary) Color.White else if (isDark) Color.White else Color(0xFF141728)

    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier

    Box(
        modifier = modifier
            .scale(scaleAnim)
            .shadow(
                elevation = if (isPrimary) 6.dp else 2.dp,
                shape = shape,
                ambientColor = if (isPrimary) Color(0x447E74F1) else Color(0x1A000000),
                spotColor = if (isPrimary) Color(0x667E74F1) else Color(0x10000000)
            )
            .clip(shape)
            .background(bgBrush)
            .then(pointerModifier)
            .drawBehind {
                if (!isStatic) {
                    val center = highlightOffsetState.value
                    val radius = max(size.width, size.height) * 0.8f
                    val sheenAlpha = if (isPressed) 0.45f else 0.22f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = sheenAlpha),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // Specular rim refraction glint
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isPressed) 0.35f else 0.20f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f)
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                    )
                }
            }
            .border(1.dp, borderStroke, shape)
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp)
            .then(tagModifier),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Item descriptor for GlassNavBar.
 */
data class GlassNavBarItem(
    val id: Int,
    val icon: ImageVector,
    val label: String,
    val hasBadge: Boolean = false,
    val avatarUrl: String? = null,
    val testTag: String? = null
)

/**
 * Reusable GlassNavBar:
 * - Floating liquid glass pill capsule
 * - Pointer-based highlight refraction tracking swipe/touch
 * - Sliding spring-animated active indicator pill
 * - Accessibility-compliant tabs (>= 48dp touch targets, semantic roles)
 */
@Composable
fun GlassNavBar(
    items: List<GlassNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    trailingItem: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val visualMode = LocalVisualThemeMode.current
    val isStatic = visualMode == VisualThemeMode.STATIC_COLORS

    val (highlightOffsetState, pointerModifier) = if (!isStatic) {
        rememberLiquidHighlightOffset(defaultOffsetFraction = Offset(0.5f, 0.5f))
    } else {
        Pair(remember { mutableStateOf(Offset.Zero) }, Modifier)
    }

    val capsuleFill = if (isStatic) {
        if (isDark) Color(0xFF202024) else Color(0xFFFFFFFF)
    } else {
        if (isDark) Color(0x38141724) else Color(0xF7FFFFFF)
    }
    val capsuleBorder = if (isStatic) {
        if (isDark) Color(0xFF383840) else Color(0xFFE2E8F0)
    } else {
        if (isDark) DarkGlassBorder else LightGlassBorder
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = if (isStatic) 4.dp else 8.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0x33000000),
                    spotColor = if (isDark) Color(0x227E74F1) else Color(0x10000000)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(capsuleFill)
                .then(pointerModifier)
                .drawBehind {
                    if (!isStatic) {
                        val center = highlightOffsetState.value
                        val radius = size.width * 0.45f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isDark) Color(0x28A69FFF) else Color(0x357E74F1),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )
                        // Chromatic refraction secondary glint
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isDark) Color(0x1506B6D4) else Color(0x2038BDF8),
                                    Color.Transparent
                                ),
                                center = center + Offset(16f, 10f),
                                radius = radius * 0.65f
                            ),
                            radius = radius * 0.65f,
                            center = center + Offset(16f, 10f)
                        )
                    }
                }
                .border(
                    width = 1.dp,
                    color = capsuleBorder,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedIndex == item.id
                    val tabInteractionSource = remember { MutableInteractionSource() }
                    val isTabPressed by tabInteractionSource.collectIsPressedAsState()

                    val tabScale by animateFloatAsState(
                        targetValue = if (isTabPressed) 0.90f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                        label = "tabScale_${item.id}"
                    )

                    val itemTagMod = if (item.testTag != null) Modifier.testTag(item.testTag) else Modifier

                    Box(
                        modifier = Modifier
                            .scale(tabScale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    if (isDark) Color(0x337E74F1) else Color(0x227E74F1)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .selectable(
                                selected = isSelected,
                                interactionSource = tabInteractionSource,
                                indication = null,
                                role = Role.Tab,
                                onClick = { onItemSelected(item.id) }
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .semantics {
                                contentDescription = item.label
                                role = Role.Tab
                            }
                            .then(itemTagMod),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.avatarUrl != null) {
                            LoopinAvatar(
                                avatarUrl = item.avatarUrl,
                                size = 26.dp,
                                borderWidth = if (isSelected) 2.dp else 1.dp,
                                borderColor = if (isSelected) LoopinIris else Color.Transparent,
                                borderBrush = if (isSelected) GlassStrokeBrush else null
                            )
                        } else {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) LoopinIris else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                if (item.hasBadge) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(LoopinIris)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (trailingItem != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingItem()
        }
    }
}

/**
 * Reusable GlassSheetContainer:
 * Liquid Glass sheet container with drag handle, translucent body,
 * specular edge refraction, and pointer movement.
 */
@Composable
fun GlassSheetContainer(
    modifier: Modifier = Modifier,
    title: String? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val visualMode = LocalVisualThemeMode.current
    val isStatic = visualMode == VisualThemeMode.STATIC_COLORS

    val (highlightOffsetState, pointerModifier) = if (!isStatic) {
        rememberLiquidHighlightOffset(defaultOffsetFraction = Offset(0.5f, 0.15f))
    } else {
        Pair(remember { mutableStateOf(Offset.Zero) }, Modifier)
    }

    val sheetBg = if (isStatic) {
        if (isDark) Color(0xFF202024) else Color(0xFFFFFFFF)
    } else {
        if (isDark) Color(0xE6121422) else Color(0xF2FFFFFF)
    }
    val sheetBorder = if (isStatic) {
        if (isDark) Color(0xFF383840) else Color(0xFFE2E8F0)
    } else {
        if (isDark) DarkGlassBorderStrong else LightGlassBorderStrong
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(sheetBg)
            .then(pointerModifier)
            .drawBehind {
                if (!isStatic) {
                    val center = highlightOffsetState.value
                    val radius = size.width * 0.7f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isDark) Color(0x28A69FFF) else Color(0x357E74F1),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
            }
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        sheetBorder,
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Drag Handle Pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(42.dp)
                .height(4.5.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0x55FFFFFF) else Color(0x33000000))
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        content()
    }
}

/**
 * Reusable GlassModalSheet:
 * Full modal bottom sheet with translucent Liquid Glass design,
 * smooth animations, scrim backdrop, and drag handle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassModalSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        scrimColor = Color(0x88000000),
        dragHandle = null,
        modifier = modifier
    ) {
        GlassSheetContainer(
            title = title,
            onDismiss = onDismissRequest,
            content = content
        )
    }
}

/**
 * Reusable GlassTextField:
 * Frosted translucent text field with rounded corners, clear contrast,
 * and liquid border focus state.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val visualMode = LocalVisualThemeMode.current
    val isStatic = visualMode == VisualThemeMode.STATIC_COLORS

    val textColor = if (isDark) TextPrimary else Color(0xFF141728)
    val containerColor = if (isStatic) {
        if (isDark) Color(0xFF27272A) else Color(0xFFF4F4F5)
    } else {
        if (isDark) Color(0x1AFFFFFF) else Color(0xEBFFFFFF)
    }
    val unfocusedContainerColor = if (isStatic) {
        if (isDark) Color(0xFF202024) else Color(0xFFFFFFFF)
    } else {
        if (isDark) Color(0x0EFFFFFF) else Color(0xD0FFFFFF)
    }
    val borderColor = if (isStatic) {
        if (isDark) Color(0xFF3F3F46) else Color(0xFFE2E8F0)
    } else {
        if (isDark) GlassBorder else Color(0x26000000)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label.isNotBlank()) { { Text(label, fontSize = 13.sp) } } else null,
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder, fontSize = 13.sp, color = if (isDark) TextSecondary else Color(0xFF8E95A5)) }
        } else null,
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (isPassword) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = unfocusedContainerColor,
            focusedBorderColor = LoopinIris,
            unfocusedBorderColor = borderColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedLabelColor = LoopinIris,
            unfocusedLabelColor = if (isDark) TextSecondary else Color(0xFF5B6178)
        )
    )
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    GlassTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        modifier = modifier,
        singleLine = singleLine,
        isPassword = isPassword,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (LocalIsDarkTheme.current) TextSecondary else Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
    )
}

/**
 * Brand Header with custom Loopin SVG icon.
 */
@Composable
fun LoopinBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 48.dp,
    showSubtext: Boolean = true
) {
    val isDark = LocalIsDarkTheme.current
    val textColor = if (isDark) TextPrimary else Color(0xFF141728)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_loopin_logo),
            contentDescription = "Loopin Logo",
            tint = if (isDark) Color.White else LoopinIris,
            modifier = Modifier.size(logoSize)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "loopin",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = (-0.5).sp
            )
            if (showSubtext) {
                Text(
                    text = "COMMUNICATIONS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) TextMuted else Color(0xFF7E849A),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/**
 * Avatar with fallback silhouette and glass ring border.
 */
@Composable
fun LoopinAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    shape: Shape = CircleShape,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    borderBrush: Brush? = null,
    contentDescription: String? = "Avatar",
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    val borderModifier = when {
        borderBrush != null -> Modifier.border(borderWidth, borderBrush, shape)
        borderWidth > 0.dp && borderColor != Color.Transparent -> Modifier.border(borderWidth, borderColor, shape)
        else -> Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(borderModifier)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            coil.compose.AsyncImage(
                model = avatarUrl,
                contentDescription = contentDescription,
                placeholder = painterResource(R.drawable.ic_default_avatar),
                error = painterResource(R.drawable.ic_default_avatar),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_default_avatar),
                contentDescription = contentDescription,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
