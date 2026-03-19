package com.example.babyparenting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.babyparenting.data.local.ThemeManager
import com.example.babyparenting.navigation.AppNavigation
import com.example.babyparenting.ui.theme.BabyParentingTheme
import com.example.babyparenting.ui.theme.LocalAppColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeManager = ThemeManager(this)

        setContent {
            var isDark by remember { mutableStateOf(themeManager.isDarkMode()) }

            BabyParentingTheme(isDark = isDark) {
                val colors = LocalAppColors.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = colors.bgMain
                ) {
                    AppNavigation(
                        onToggleTheme = {
                            isDark = !isDark
                            themeManager.setDarkMode(isDark)
                        }
                    )
                }
            }
        }
    }
}