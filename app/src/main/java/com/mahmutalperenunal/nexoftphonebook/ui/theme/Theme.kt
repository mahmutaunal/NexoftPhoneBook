package com.mahmutalperenunal.nexoftphonebook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2979FF),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF1565C0),

    background = Color(0xFFF6F6F6),
    onBackground = Color(0xFF1D1B20),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D1B20),

    onSurfaceVariant = Color(0xFF757575),

    outlineVariant = Color(0xFFEEEEEE),

    error = Color(0xFFB3261E),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF82B1FF),
    onPrimary = Color(0xFF00296B),

    primaryContainer = Color(0xFF00478F),
    onPrimaryContainer = Color(0xFFD6E4FF),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E2),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE2E2E2),

    onSurfaceVariant = Color(0xFFC4C7C5),
    outlineVariant = Color(0xFF444746),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun NexoftPhoneBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}