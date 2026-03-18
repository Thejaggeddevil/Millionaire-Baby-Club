package com.example.babyparenting

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.babyparenting.navigation.AppNavigation
import com.example.babyparenting.navigation.Routes
import com.example.babyparenting.ui.theme.BabyParentingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs       = getSharedPreferences("journey_progress", Context.MODE_PRIVATE)
        val isReturning = prefs.getString("child_name", "")?.isNotBlank() == true
        val start       = if (isReturning) Routes.JOURNEY else Routes.ONBOARDING

        setContent {
            BabyParentingTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(startDestination = start)
                }
            }
        }
    }
}