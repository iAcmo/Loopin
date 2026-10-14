package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderStrong
import com.example.ui.theme.GlassFill
import com.example.ui.theme.GlassFillElevated
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinBg
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) LoopinBg else Color(0xFFF1F3F9)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Ambient soft liquid light refractions behind the frosted glass
        Canvas(modifier = Modifier.fillMaxSize()) {
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

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isElevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val fill = if (isDark) {
        if (isElevated) GlassFillElevated else GlassFill
    } else {
        if (isElevated) Color(0xF2FFFFFF) else Color(0xD8FFFFFF)
    }
    val borderBrush = if (isDark) {
        GlassStrokeBrush
    } else {
        Brush.linearGradient(listOf(Color(0x55000000), Color(0x1A000000), Color(0x33000000)))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            ),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    leadingIcon: ImageVector? = null,
    isPrimary: Boolean = true,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val isDark = LocalIsDarkTheme.current
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xEE7A6FFA),
                Color(0xEE9086FA)
            )
        )
    } else {
        if (isDark) {
            Brush.horizontalGradient(listOf(Color(0x28FFFFFF), Color(0x16FFFFFF)))
        } else {
            Brush.horizontalGradient(listOf(Color(0x1E000000), Color(0x0E000000)))
        }
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgBrush)
            .border(
                1.dp,
                if (isPrimary) GlassBorderStrong else if (isDark) GlassBorder else Color(0x22000000),
                shape
            )
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isPrimary) Color.White else if (isDark) Color.White else Color(0xFF141728),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPrimary) Color.White else if (isDark) Color.White else Color(0xFF141728)
                )
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val textColor = if (isDark) TextPrimary else Color(0xFF141728)
    val containerColor = if (isDark) Color(0x1AFFFFFF) else Color(0xEBFFFFFF)
    val unfocusedContainerColor = if (isDark) Color(0x0EFFFFFF) else Color(0xD0FFFFFF)
    val borderColor = if (isDark) GlassBorder else Color(0x26000000)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
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
