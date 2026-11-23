package com.electrosathi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// ElectroSathi "midnight study" palette — inspired by PrepFusion's dark,
// confident, blue-accented aesthetic. Since Batch 4 the midnight look is the
// app's fixed identity: the dark scheme is always used, whatever the phone's
// system setting says. (The light scheme is kept below for future use.)
// ---------------------------------------------------------------------------
private val NavyBlack = Color(0xFF0A0E1C)      // app background (dark)
private val NavyCard = Color(0xFF151C33)        // card surface (dark)
private val NavyBorder = Color(0xFF232B4A)      // hairline borders (dark)
private val MutedBlue = Color(0xFF9AA3B8)       // secondary text (dark)
private val AccentBlue = Color(0xFF3B82F6)      // primary accent
private val AccentBlueLight = Color(0xFF60A5FA) // links, highlights

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color.White,
    secondary = AccentBlueLight,
    onSecondary = NavyBlack,
    background = NavyBlack,
    onBackground = Color.White,
    surface = NavyCard,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A2340),
    onSurfaceVariant = MutedBlue,
    outline = NavyBorder,
    outlineVariant = NavyBorder
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = AccentBlue,
    background = Color(0xFFF1F4F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE8EDF5),
    onSurfaceVariant = Color(0xFF5B6B85),
    outline = Color(0xFFD6DEEA),
    outlineVariant = Color(0xFFD6DEEA)
)

// ---------------------------------------------------------------------------
// Brand tokens — Batch 4 (from Ashish's reference designs).
// The bright cyan is the new signature: logo, rings, search pill, Volt pill.
// ---------------------------------------------------------------------------
val BrandBlue = Color(0xFF3B82F6)
val BrandCyan = Color(0xFF22D3EE)
val BrandPurple = Color(0xFFA855F7)
val BrandOrange = Color(0xFFF97316)
val NavBarNavy = Color(0xFF0C1430)   // bottom navigation bar container
val MutedText = Color(0xFF9AA3B8)    // unselected nav items / secondary text

/** Blue → cyan: logo ring, play button, progress accents. */
val BrandGradient: Brush = Brush.linearGradient(listOf(BrandBlue, BrandCyan))

/** Blue → purple → orange: the GATE promo banner. */
val BannerGradient: Brush =
    Brush.horizontalGradient(listOf(Color(0xFF2563EB), BrandPurple, BrandOrange))

/** Blue → purple → cyan: the glowing border on Home's shortcut tiles. */
val TileBorderGradient: Brush =
    Brush.linearGradient(listOf(BrandBlue, BrandPurple, BrandCyan))

/** Deep navy → burnt orange glow: first exam-countdown card. */
val CountdownGradientWarm: Brush = Brush.horizontalGradient(
    listOf(Color(0xFF16224A), Color(0xFF7C2D12), BrandOrange)
)

/** Royal blue → sky → orange tip: second exam-countdown card. */
val CountdownGradientCool: Brush = Brush.horizontalGradient(
    listOf(Color(0xFF1D4ED8), Color(0xFF0EA5E9), BrandOrange)
)

/**
 * The app's theme: Material3, fixed midnight identity.
 * Every screen is wrapped in this — no XML styling needed.
 * The dark scheme is the default and is what MainActivity uses; the
 * parameter stays so any existing caller keeps compiling unchanged.
 */
@Composable
fun ElectroSathiTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
