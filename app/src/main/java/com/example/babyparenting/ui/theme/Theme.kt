package com.example.babyparenting.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightM3 = lightColorScheme(
    primary          = LightColors.coral,
    secondary        = LightColors.peach,
    tertiary         = LightColors.lavender,
    background       = LightColors.bgMain,
    surface          = LightColors.bgSurface,
    onBackground     = LightColors.textPrimary,
    onSurface        = LightColors.textPrimary,
    outline          = LightColors.border,
    error            = LightColors.red
)

private val DarkM3 = darkColorScheme(
    primary          = DarkColors.coral,
    secondary        = DarkColors.peach,
    tertiary         = DarkColors.lavender,
    background       = DarkColors.bgMain,
    surface          = DarkColors.bgSurface,
    onBackground     = DarkColors.textPrimary,
    onSurface        = DarkColors.textPrimary,
    outline          = DarkColors.border,
    error            = DarkColors.red
)

@Composable
fun BabyParentingTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (isDark) DarkColors else LightColors

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = if (isDark) DarkM3 else LightM3,
            content     = content
        )
    }
}