package com.example.babyparenting.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized dark theme color palette.
 * Deep indigo-navy — dark but warm, not pure black.
 *
 * Use ONLY these constants in all screens.
 * Never hardcode colors directly in screens.
 */
object AppColors {

    // ── Backgrounds ───────────────────────────────────────────────────────────
    val BgMain        = Color(0xFF1A1928)   // main screen background
    val BgSurface     = Color(0xFF242338)   // cards, form fields
    val BgElevated    = Color(0xFF2E2C45)   // dialogs, bottom sheets, dropdowns
    val BgTopBar      = Color(0xFF1E1D2E)   // top navigation bars
    val BgHeader      = Color(0xFF201F32)   // journey header, section headers
    val BgChip        = Color(0xFF2E2C45)   // unselected chip
    val BgChipSelected = Color(0xFFFF8B94)  // selected chip — coral

    // ── Borders ───────────────────────────────────────────────────────────────
    val Border        = Color(0xFF3A3854)   // card borders, dividers
    val BorderLight   = Color(0xFF2E2C45)   // subtle borders

    // ── Text ──────────────────────────────────────────────────────────────────
    val TextPrimary   = Color(0xFFF2EDE8)   // main headings, titles
    val TextSecondary = Color(0xFFAA9AAC)   // subtitles, captions
    val TextMuted     = Color(0xFF6B6480)   // hints, placeholders
    val TextOnAccent  = Color(0xFFFFFFFF)   // text on colored buttons

    // ── Accents (keep playful for baby app) ───────────────────────────────────
    val Coral         = Color(0xFFFF8B94)   // primary — coral pink
    val Peach         = Color(0xFFFFB06A)   // secondary — warm orange
    val Lavender      = Color(0xFF9B8FD4)   // tertiary — soft purple
    val Mint          = Color(0xFF4CAF82)   // success / complete
    val Sky           = Color(0xFF5E9BE0)   // info / advice
    val Gold          = Color(0xFFE6891A)   // warning / why it matters
    val Red           = Color(0xFFE53935)   // error / danger
    val Yellow        = Color(0xFFFFD600)   // tip border

    // ── Special ───────────────────────────────────────────────────────────────
    val PathBlue      = Color(0xFF7B8FFF)   // journey path completed
    val PathFaint     = Color(0xFF3A3854)   // journey path future
    val Locked        = Color(0xFF4A4862)   // locked milestone

    // ── Gradient helpers ──────────────────────────────────────────────────────
    val HeaderGradientStart = Color(0xFF252238)
    val HeaderGradientEnd   = Color(0xFF1A1928)
    val LoginGradientStart  = Color(0xFF1E1B30)
    val LoginGradientMid    = Color(0xFF221F34)
    val LoginGradientEnd    = Color(0xFF1A1928)
}
