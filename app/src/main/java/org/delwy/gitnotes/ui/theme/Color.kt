package org.delwy.gitnotes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Фолбэк‑палитра, повторяющая референс (тёмный фон, сливовые action‑кнопки,
// слейт‑операторы, лавандовый акцент). На Android 12+ перекрывается Monet.
private val DarkPrimary = androidx.compose.ui.graphics.Color(0xFFBFC6FA)
private val DarkOnPrimary = androidx.compose.ui.graphics.Color(0xFF1B1C2E)
private val DarkPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF3A3D6B)
private val DarkOnPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFDEE1FF)

private val DarkSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF3C4257)
private val DarkOnSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFDEE1F0)

private val DarkTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF5A4458)
private val DarkOnTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFF6D2EC)

private val DarkError = androidx.compose.ui.graphics.Color(0xFFF2B8B5)
private val DarkOnError = androidx.compose.ui.graphics.Color(0xFF601410)
private val DarkErrorContainer = androidx.compose.ui.graphics.Color(0xFF8C1D18)
private val DarkOnErrorContainer = androidx.compose.ui.graphics.Color(0xFFFADADA)

private val DarkBackground = androidx.compose.ui.graphics.Color(0xFF0E0F13)
private val DarkOnBackground = androidx.compose.ui.graphics.Color(0xFFE3E2E6)
private val DarkSurface = androidx.compose.ui.graphics.Color(0xFF0E0F13)
private val DarkOnSurface = androidx.compose.ui.graphics.Color(0xFFE3E2E6)
private val DarkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF2A2C33)
private val DarkOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC4C6CF)
private val DarkOutline = androidx.compose.ui.graphics.Color(0xFF8E9099)
private val DarkOutlineVariant = androidx.compose.ui.graphics.Color(0xFF44474E)
private val DarkSurfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFF23252B)
private val DarkSurfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF1D1F25)

val GitNotesDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerHigh = DarkSurfaceContainerHigh
)

private val LightPrimary = androidx.compose.ui.graphics.Color(0xFF4A4E86)
private val LightOnPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
private val LightSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFDFE1F0)
private val LightOnSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF2C2F3D)
private val LightTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFAD8EE)
private val LightOnTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF3F2A3C)
private val LightBackground = androidx.compose.ui.graphics.Color(0xFFFDFBFF)
private val LightOnBackground = androidx.compose.ui.graphics.Color(0xFF1B1B1F)
private val LightSurface = androidx.compose.ui.graphics.Color(0xFFFDFBFF)
private val LightOnSurface = androidx.compose.ui.graphics.Color(0xFF1B1B1F)
private val LightSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE2E1EC)
private val LightOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF45464F)

val GitNotesLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)
