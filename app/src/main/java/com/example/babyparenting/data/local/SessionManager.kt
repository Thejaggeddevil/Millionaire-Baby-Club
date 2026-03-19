package com.example.babyparenting.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Saves login/session state locally so the user doesn't have to
 * log in every time the app is opened from history (process death).
 *
 * Firebase auth will replace this later.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .apply()
    }

    companion object {
        private const val KEY_LOGGED_IN = "is_logged_in"
    }
}
