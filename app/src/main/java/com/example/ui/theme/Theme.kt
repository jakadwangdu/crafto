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
    primary = CraftoLilac,
    onPrimary = CraftoPrimaryDark,
    primaryContainer = CraftoPrimary,
    onPrimaryContainer = CraftoPrimaryContainer,
    secondary = CraftoSecondaryLight,
    onSecondary = CraftoSecondaryDark,
    secondaryContainer = CraftoSecondaryContainer,
    tertiary = CraftoTertiaryLight,
    background = CraftoBgDark,
    surface = CraftoSurfaceDark,
    surfaceVariant = CraftoSurfaceVariantDark,
    onBackground = CraftoTextPrimaryDark,
    onSurface = CraftoTextPrimaryDark,
    onSurfaceVariant = CraftoTextSecondaryDark,
    outline = CraftoBorderDark,
    outlineVariant = CraftoOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = CraftoPrimary,
    onPrimary = Color.White,
    primaryContainer = CraftoPrimaryContainer,
    onPrimaryContainer = CraftoOnPrimaryContainer,
    secondary = CraftoSecondary,
    onSecondary = Color.White,
    secondaryContainer = CraftoSecondaryContainer,
    tertiary = CraftoTertiary,
    background = CraftoBgLight,
    surface = CraftoSurfaceLight,
    surfaceVariant = CraftoSurfaceVariantLight,
    onBackground = CraftoTextPrimaryLight,
    onSurface = CraftoTextPrimaryLight,
    onSurfaceVariant = CraftoTextSecondaryLight,
    outline = CraftoOutlineLight,
    outlineVariant = CraftoBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
