package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ButtonGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF263238),
    onPrimaryContainer = Color(0xFFE8F8EE),
    secondary = BonusGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF37474F),
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = ButtonGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F8EE),
    onPrimaryContainer = Color(0xFF006935),
    secondary = PrimaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = DarkBlue,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurfaceCard,
    onSurface = LightText,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = LightTextMuted,
    outline = Color(0xFFE0E0E0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep InvestPro trust blue branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
