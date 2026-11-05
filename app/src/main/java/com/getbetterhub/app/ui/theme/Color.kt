package com.getbetterhub.app.ui.theme

import androidx.compose.ui.graphics.Color

// Colors sampled directly from the "Get Better Hub" reference build (screenshots supplied by Jacob)
// so the compiled app matches pixel-for-pixel.

val AppBackground = Color(0xFF0D0713)
val SurfaceCard = Color(0xFF181220)
val SurfaceCardBorder = Color(0xFF32293A)

val TextPrimary = Color(0xFFF3F2ED)
val TextMuted = Color(0xFFA6A2B0)

val AccentOrange = Color(0xFFF7A831)      // primary actions, active nav, logo
val AccentOrangeOnFill = Color(0xFF1A1200)

val AccentCoral = Color(0xFFF17166)       // "Spin the roulette", "Stop", high-energy actions
val AccentCoralOnFill = Color(0xFF2A0A06)

val AccentMutedGold = Color(0xFF815822)   // "Get my plan" default/idle button state
val AccentMutedGoldText = Color(0xFFE8C89A)

val AccentGreen = Color(0xFF22C55E)       // "Synced" state
val SyncedToastBg = Color(0xFFECFDF3)
val SyncedToastText = Color(0xFF15803D)

val PendingTagBg = Color(0xFF3D2B1D)
val PendingTagText = Color(0xFFE8A94B)

val ScrubberTrack = Color(0xFF3A3A3A)
val PillBorder = Color(0xFF3A3548)
val HomeIndicator = Color(0xFF4A4956)

// Legacy aliases kept so earlier screens/components compiled against the old names still resolve.
val AccentRed = AccentCoral
val AccentRedDim = Color(0xFF3A1F1C)
val AccentGold = AccentMutedGold
