package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object AppThemeState {
    private const val PREFS_NAME = "loopin_app_prefs"
    private const val KEY_ACCENT = "key_app_accent_color"
    val DEFAULT_ACCENT = Color(0xFF7E74F1)

    private val _currentAccent = mutableStateOf(DEFAULT_ACCENT)
    val currentAccent: State<Color> = _currentAccent

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val argb = prefs.getInt(KEY_ACCENT, DEFAULT_ACCENT.toArgb())
        _currentAccent.value = Color(argb)
    }

    fun setAccent(color: Color, context: Context) {
        _currentAccent.value = color
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ACCENT, color.toArgb()).apply()
    }
}
