package com.example.binm.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun setTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.edit {
            putBoolean("is_dark_theme", isDark)
        }
    }
}
