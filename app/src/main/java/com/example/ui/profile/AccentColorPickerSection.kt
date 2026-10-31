package com.example.ui.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.AppThemeState
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.PRESET_ACCENT_COLORS
import kotlin.math.roundToInt

@Composable
fun PrivacyChoiceChipRow(
    isPrivate: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accent = LocalAppAccentColor.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Public Choice Chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (!isPrivate) accent.copy(alpha = 0.22f) else Color(0x10FFFFFF)
                    )
                    .border(
                        width = if (!isPrivate) 1.5.dp else 1.dp,
                        color = if (!isPrivate) accent else Color(0x20FFFFFF),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onPrivacyChange(false) }
                    .padding(vertical = 12.dp, horizontal = 10.dp)
                    .testTag("privacy_public_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = if (!isPrivate) accent else (if (isDark) DarkTextSecondary else Color(0xFF64748B)),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Public",
                        fontSize = 14.sp,
                        fontWeight = if (!isPrivate) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isPrivate) (if (isDark) Color.White else Color(0xFF0F172A)) else (if (isDark) DarkTextSecondary else Color(0xFF64748B))
                    )
                }
            }

            // Private Choice Chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isPrivate) accent.copy(alpha = 0.22f) else Color(0x10FFFFFF)
                    )
                    .border(
                        width = if (isPrivate) 1.5.dp else 1.dp,
                        color = if (isPrivate) accent else Color(0x20FFFFFF),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onPrivacyChange(true) }
                    .padding(vertical = 12.dp, horizontal = 10.dp)
                    .testTag("privacy_private_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isPrivate) accent else (if (isDark) DarkTextSecondary else Color(0xFF64748B)),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Private",
                        fontSize = 14.sp,
                        fontWeight = if (isPrivate) FontWeight.Bold else FontWeight.Medium,
                        color = if (isPrivate) (if (isDark) Color.White else Color(0xFF0F172A)) else (if (isDark) DarkTextSecondary else Color(0xFF64748B))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isPrivate)
                "🔒 Only approved followers can view your posts, stories, and instants."
            else
                "🌐 Anyone on Loopin can discover your profile, join your posts, and follow you.",
            fontSize = 12.sp,
            color = if (isDark) DarkTextSecondary else Color(0xFF5B6178),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun AccentColorPickerCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val currentAccent = LocalAppAccentColor.current

    var showMixer by remember { mutableStateOf(false) }
    var redVal by remember { mutableFloatStateOf(currentAccent.red * 255f) }
    var greenVal by remember { mutableFloatStateOf(currentAccent.green * 255f) }
    var blueVal by remember { mutableFloatStateOf(currentAccent.blue * 255f) }
    var hexInput by remember { mutableStateOf(colorToHex(currentAccent)) }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = currentAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "App Accent Color",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DarkTextPrimary else Color(0xFF141728)
                    )
                }

                // Current Color Orb preview
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(currentAccent)
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            }

            Text(
                text = "Select from vibrant color presets or mix your custom RGB accent color.",
                fontSize = 12.sp,
                color = if (isDark) DarkTextSecondary else Color(0xFF5B6178),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            // Preset Swatches Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PRESET_ACCENT_COLORS) { preset ->
                    val isSelected = currentAccent == preset.color
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(preset.color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0x33FFFFFF),
                                shape = CircleShape
                            )
                            .clickable {
                                AppThemeState.setAccent(preset.color, context)
                                redVal = preset.color.red * 255f
                                greenVal = preset.color.green * 255f
                                blueVal = preset.color.blue * 255f
                                hexInput = colorToHex(preset.color)
                            }
                            .testTag("accent_preset_${preset.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle Custom Color Mixer
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x18FFFFFF))
                    .clickable { showMixer = !showMixer }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (showMixer) "▲ Hide Custom Color Mixer" else "▼ Open Custom Color Mixer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentAccent
                )
            }

            if (showMixer) {
                Spacer(modifier = Modifier.height(12.dp))

                // Red Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "R: ${redVal.roundToInt()}", fontSize = 11.sp, color = Color(0xFFFF5252), modifier = Modifier.width(42.dp))
                    Slider(
                        value = redVal,
                        onValueChange = {
                            redVal = it
                            val newC = Color(redVal.roundToInt(), greenVal.roundToInt(), blueVal.roundToInt())
                            AppThemeState.setAccent(newC, context)
                            hexInput = colorToHex(newC)
                        },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = Color(0xFFFF5252)),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Green Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "G: ${greenVal.roundToInt()}", fontSize = 11.sp, color = Color(0xFF4CAF50), modifier = Modifier.width(42.dp))
                    Slider(
                        value = greenVal,
                        onValueChange = {
                            greenVal = it
                            val newC = Color(redVal.roundToInt(), greenVal.roundToInt(), blueVal.roundToInt())
                            AppThemeState.setAccent(newC, context)
                            hexInput = colorToHex(newC)
                        },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Blue Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "B: ${blueVal.roundToInt()}", fontSize = 11.sp, color = Color(0xFF2196F3), modifier = Modifier.width(42.dp))
                    Slider(
                        value = blueVal,
                        onValueChange = {
                            blueVal = it
                            val newC = Color(redVal.roundToInt(), greenVal.roundToInt(), blueVal.roundToInt())
                            AppThemeState.setAccent(newC, context)
                            hexInput = colorToHex(newC)
                        },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hex input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            hexInput = input
                            try {
                                val clean = input.removePrefix("#").trim()
                                if (clean.length == 6) {
                                    val r = clean.substring(0, 2).toInt(16)
                                    val g = clean.substring(2, 4).toInt(16)
                                    val b = clean.substring(4, 6).toInt(16)
                                    val newC = Color(r, g, b)
                                    AppThemeState.setAccent(newC, context)
                                    redVal = r.toFloat()
                                    greenVal = g.toFloat()
                                    blueVal = b.toFloat()
                                }
                            } catch (_: Exception) {}
                        },
                        placeholder = "#7E74F1",
                        modifier = Modifier.weight(1f).testTag("custom_color_hex_input")
                    )

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(redVal.roundToInt(), greenVal.roundToInt(), blueVal.roundToInt()))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    )
                }
            }
        }
    }
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).roundToInt()
    val g = (color.green * 255).roundToInt()
    val b = (color.blue * 255).roundToInt()
    return "#%02X%02X%02X".format(r, g, b)
}
