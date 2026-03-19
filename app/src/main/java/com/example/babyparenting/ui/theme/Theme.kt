package com.example.babyparenting.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary          = AppColors.Coral,
    onPrimary        = AppColors.TextOnAccent,
    secondary        = AppColors.Peach,
    onSecondary      = AppColors.TextOnAccent,
    tertiary         = AppColors.Lavender,
    background       = AppColors.BgMain,
    surface          = AppColors.BgSurface,
    surfaceVariant   = AppColors.BgElevated,
    onBackground     = AppColors.TextPrimary,
    onSurface        = AppColors.TextPrimary,
    onSurfaceVariant = AppColors.TextSecondary,
    outline          = AppColors.Border,
    error            = AppColors.Red
)

@Composable
fun BabyParentingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        content     = content
    )
}