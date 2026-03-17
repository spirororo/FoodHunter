package com.example.foodhunter.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkPalette = darkColorScheme(
    primary = Emerald80,
    secondary = Sand80,
    tertiary = Peach80
)

private val LightPalette = lightColorScheme(
    primary = Emerald40,
    secondary = Sand40,
    tertiary = Peach40
)

@Composable
fun FoodHunterTheme(
    useDark: Boolean = isSystemInDarkTheme(),
    dynamicColors: Boolean = true,
    content: @Composable () -> Unit
) {
    // на андроид 12+ используем динамические цвета, иначе свои
    val palette = when {
        dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (useDark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        useDark -> DarkPalette
        else -> LightPalette
    }

    MaterialTheme(
        colorScheme = palette,
        typography = AppTypography,
        content = content
    )
}
