package com.example.pia_claseordinaria.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors matching colors.xml / values-night/colors.xml
val PrimaryLight = Color(0xFF0056D2)
val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)
val OnPrimaryLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF1B1B1F)
val OnSurfaceLight = Color(0xFF1B1B1F)
val SurfaceVariantLight = Color(0xFFE1E2EC)
val OutlineLight = Color(0xFF757780)

val PrimaryDark = Color(0xFF82B1FF)
val BackgroundDark = Color(0xFF0F1015)
val SurfaceDark = Color(0xFF1A1C29)
val OnPrimaryDark = Color(0xFF001945)
val OnBackgroundDark = Color(0xFFF0F0F5)
val OnSurfaceDark = Color(0xFFF0F0F5)
val SurfaceVariantDark = Color(0xFF2E303F)
val OutlineDark = Color(0xFF3E4154)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun HousePortTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
