package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class ChatThemeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val previewColors: List<Color>,
    val bubbleBrush: Brush,
    val accentColor: Color,
    val glowColor: Color
)

object ChatThemeRegistry {
    val IrisGlow = ChatThemeDefinition(
        id = "iris_glow",
        name = "Iris Glow",
        description = "Signature Loopin cosmic violet",
        previewColors = listOf(Color(0xFF7E74F1), Color(0xFFA69FFF)),
        bubbleBrush = Brush.linearGradient(listOf(Color(0xFF6B61ED), Color(0xFF9086FA))),
        accentColor = Color(0xFF7E74F1),
        glowColor = Color(0xFFA69FFF)
    )

    val CosmicMidnight = ChatThemeDefinition(
        id = "cosmic_midnight",
        name = "Cosmic Midnight",
        description = "Deep indigo & electric cyan",
        previewColors = listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)),
        bubbleBrush = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF0891B2))),
        accentColor = Color(0xFF4F46E5),
        glowColor = Color(0xFF22D3EE)
    )

    val SunsetEmber = ChatThemeDefinition(
        id = "sunset_ember",
        name = "Sunset Ember",
        description = "Warm coral & golden rose",
        previewColors = listOf(Color(0xFFFF5E62), Color(0xFFFF9966)),
        bubbleBrush = Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFF97316))),
        accentColor = Color(0xFFFF5E62),
        glowColor = Color(0xFFFDBA74)
    )

    val EmeraldSea = ChatThemeDefinition(
        id = "emerald_sea",
        name = "Emerald Sea",
        description = "Luminous mint & deep jade",
        previewColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
        bubbleBrush = Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
        accentColor = Color(0xFF10B981),
        glowColor = Color(0xFF6EE7B7)
    )

    val NeonOcean = ChatThemeDefinition(
        id = "neon_ocean",
        name = "Neon Ocean",
        description = "Electric cobalt & royal purple",
        previewColors = listOf(Color(0xFF2563EB), Color(0xFF9333EA)),
        bubbleBrush = Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF7E22CE))),
        accentColor = Color(0xFF3B82F6),
        glowColor = Color(0xFFA855F7)
    )

    val PlatinumFrost = ChatThemeDefinition(
        id = "platinum_frost",
        name = "Platinum Frost",
        description = "Clean minimalist liquid crystal",
        previewColors = listOf(Color(0xFF64748B), Color(0xFF94A3B8)),
        bubbleBrush = Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF64748B))),
        accentColor = Color(0xFF94A3B8),
        glowColor = Color(0xFFE2E8F0)
    )

    val allThemes = listOf(
        IrisGlow,
        CosmicMidnight,
        SunsetEmber,
        EmeraldSea,
        NeonOcean,
        PlatinumFrost
    )

    fun getThemeById(id: String?): ChatThemeDefinition {
        return allThemes.firstOrNull { it.id == id } ?: IrisGlow
    }
}
