package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class NoteColorTheme(
    val id: String,
    val name: String,
    val backgroundLight: Color,
    val backgroundDark: Color,
    val textLight: Color,
    val textDark: Color,
    val swatchColor: Color
)

object NoteColorPalette {
    val Neutral = NoteColorTheme(
        id = "neutral",
        name = "Classic Frost",
        backgroundLight = Color(0xF2FFFFFF),
        backgroundDark = Color(0xD9222533),
        textLight = Color(0xFF141728),
        textDark = Color(0xFFF8F9FD),
        swatchColor = Color(0xFF94A3B8)
    )

    val Pink = NoteColorTheme(
        id = "pink",
        name = "Bubblegum Pink",
        backgroundLight = Color(0xFFFF3377),
        backgroundDark = Color(0xFFFF2A70),
        textLight = Color.White,
        textDark = Color.White,
        swatchColor = Color(0xFFFF3377)
    )

    val Blue = NoteColorTheme(
        id = "blue",
        name = "Electric Blue",
        backgroundLight = Color(0xFF0099FF),
        backgroundDark = Color(0xFF0088EE),
        textLight = Color.White,
        textDark = Color.White,
        swatchColor = Color(0xFF0099FF)
    )

    val Lime = NoteColorTheme(
        id = "lime",
        name = "Neon Lime",
        backgroundLight = Color(0xFF65A30D),
        backgroundDark = Color(0xFF4D7C0F),
        textLight = Color.White,
        textDark = Color.White,
        swatchColor = Color(0xFF84CC16)
    )

    val Yellow = NoteColorTheme(
        id = "yellow",
        name = "Sunshine Yellow",
        backgroundLight = Color(0xFFFACC15),
        backgroundDark = Color(0xFFEAB308),
        textLight = Color(0xFF1E1E1E),
        textDark = Color(0xFF18181B),
        swatchColor = Color(0xFFFACC15)
    )

    val Purple = NoteColorTheme(
        id = "purple",
        name = "Orchid Purple",
        backgroundLight = Color(0xFF9333EA),
        backgroundDark = Color(0xFF7E22CE),
        textLight = Color.White,
        textDark = Color.White,
        swatchColor = Color(0xFFA855F7)
    )

    val Orange = NoteColorTheme(
        id = "orange",
        name = "Sunset Orange",
        backgroundLight = Color(0xFFEA580C),
        backgroundDark = Color(0xFFC2410C),
        textLight = Color.White,
        textDark = Color.White,
        swatchColor = Color(0xFFFB923C)
    )

    val allColors = listOf(
        Neutral,
        Pink,
        Blue,
        Lime,
        Yellow,
        Purple,
        Orange
    )

    fun getColorById(id: String): NoteColorTheme {
        return allColors.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: Neutral
    }
}
