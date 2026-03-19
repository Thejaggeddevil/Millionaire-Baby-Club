package com.example.babyparenting.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColorScheme(
    // ── Backgrounds ───────────────────────────────────────────────────────────
    val bgMain: Color,         // full screen background
    val bgSurface: Color,      // top bars, header
    val bgCard: Color,         // milestone cards, content cards
    val bgCardLocked: Color,   // locked milestone card background
    val bgElevated: Color,     // dialogs, dropdowns
    val bgChip: Color,         // unselected chip

    // ── Card text (inverted vs bg for contrast) ───────────────────────────────
    val cardTextPrimary: Color,
    val cardTextSecondary: Color,
    val cardBorder: Color,
    val cardBorderActive: Color,   // completed card border

    // ── General text ──────────────────────────────────────────────────────────
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnAccent: Color,

    // ── Global borders ────────────────────────────────────────────────────────
    val border: Color,

    // ── Accents (same in both themes) ─────────────────────────────────────────
    val coral: Color,
    val peach: Color,
    val lavender: Color,
    val mint: Color,
    val sky: Color,
    val gold: Color,
    val red: Color,
    val yellow: Color,

    // ── Journey path ──────────────────────────────────────────────────────────
    val pathCompleted: Color,
    val pathFuture: Color,

    // ── Hamburger menu ────────────────────────────────────────────────────────
    val menuBg: Color,
    val menuItemBg: Color,
    val menuDivider: Color,

    // ── Flag ─────────────────────────────────────────────────────────────────
    val isDark: Boolean
)

// ── Light scheme ──────────────────────────────────────────────────────────────
// Warm peach background, DARK cards for contrast
val LightColors = AppColorScheme(
    isDark            = false,

    bgMain            = Color(0xFFF5F0EB),   // warm off-white
    bgSurface         = Color(0xFFFFEEE0),   // peach top bars
    bgCard            = Color(0xFF2D2240),   // dark purple-navy card bg
    bgCardLocked      = Color(0xFF3C3750),   // slightly lighter locked
    bgElevated        = Color(0xFFFFFFFF),   // white dialogs
    bgChip            = Color(0xFFEDE8E0),   // light chip bg

    cardTextPrimary   = Color(0xFFF2EDE8),   // warm white on dark card
    cardTextSecondary = Color(0xFFB8AECF),   // muted lavender on dark card
    cardBorder        = Color(0xFF4A3F6A),   // card border
    cardBorderActive  = Color(0xFFFF8B94),   // completed card coral border

    textPrimary       = Color(0xFF2D1B0E),   // dark brown
    textSecondary     = Color(0xFF7A5C4A),   // medium brown
    textMuted         = Color(0xFFAA8877),   // light brown
    textOnAccent      = Color(0xFFFFFFFF),

    border            = Color(0xFFE0D5CA),

    coral             = Color(0xFFFF8B94),
    peach             = Color(0xFFFFB06A),
    lavender          = Color(0xFF9B8FD4),
    mint              = Color(0xFF4CAF82),
    sky               = Color(0xFF5E9BE0),
    gold              = Color(0xFFE6891A),
    red               = Color(0xFFE53935),
    yellow            = Color(0xFFFFD600),

    pathCompleted     = Color(0xFF1565C0),
    pathFuture        = Color(0xFFBBDEFB),

    menuBg            = Color(0xFFFFFFFF),
    menuItemBg        = Color(0xFFFFF5EE),
    menuDivider       = Color(0xFFEDE8E0),
)

// ── Dark scheme ───────────────────────────────────────────────────────────────
// Deep indigo background, LIGHT cards for contrast
val DarkColors = AppColorScheme(
    isDark            = true,

    bgMain            = Color(0xFF1A1928),   // deep indigo-navy
    bgSurface         = Color(0xFF1E1D2E),   // slightly lighter for top bars
    bgCard            = Color(0xFFF0EAE2),   // warm cream card bg (light on dark)
    bgCardLocked      = Color(0xFFD8D0C8),   // slightly dimmer locked card
    bgElevated        = Color(0xFF2E2C45),   // dialogs
    bgChip            = Color(0xFF2E2C45),   // chip bg

    cardTextPrimary   = Color(0xFF1A1430),   // dark text on light card
    cardTextSecondary = Color(0xFF5A4E7A),   // medium purple on light card
    cardBorder        = Color(0xFFD0C8BC),   // card border on cream
    cardBorderActive  = Color(0xFFFF8B94),   // completed card coral border

    textPrimary       = Color(0xFFF2EDE8),   // warm white
    textSecondary     = Color(0xFFAA9AAC),   // muted lavender
    textMuted         = Color(0xFF6B6480),   // dark muted
    textOnAccent      = Color(0xFFFFFFFF),

    border            = Color(0xFF3A3854),

    coral             = Color(0xFFFF8B94),
    peach             = Color(0xFFFFB06A),
    lavender          = Color(0xFF9B8FD4),
    mint              = Color(0xFF4CAF82),
    sky               = Color(0xFF5E9BE0),
    gold              = Color(0xFFE6891A),
    red               = Color(0xFFE53935),
    yellow            = Color(0xFFFFD600),

    pathCompleted     = Color(0xFF7B8FFF),
    pathFuture        = Color(0xFF3A3854),

    menuBg            = Color(0xFF242338),
    menuItemBg        = Color(0xFF2E2C45),
    menuDivider       = Color(0xFF3A3854),
)

// ── CompositionLocal — use this everywhere instead of hardcoded colors ─────────
val LocalAppColors = staticCompositionLocalOf { LightColors }