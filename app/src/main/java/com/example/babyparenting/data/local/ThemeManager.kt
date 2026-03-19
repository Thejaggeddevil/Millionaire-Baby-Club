package com.example.babyparenting.data.local

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK, false)

    fun setDarkMode(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK, dark).apply()
    }

    companion object {
        private const val KEY_DARK = "is_dark_mode"
    }
}
