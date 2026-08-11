package com.personal.fuel.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.personal.fuel.domain.model.ThemeMode

/**
 * Colours the Fuel design uses that Material 3 has no slot for: the macro accent
 * colours, the two extra surface steps, and the hairline borders that give the
 * cards their outline.
 */
data class FuelColors(
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val borderStrong: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    /** Filled surfaces (buttons, active tabs) — always pairs with [onAccent]. */
    val accent: Color,
    val accentPressed: Color,
    val accentDim: Color,
    val onAccent: Color,
    /** Accent used for text and outlines, where the fill colour is too light. */
    val accentOnSurface: Color,
    val calories: Color,
    val protein: Color,
    val fiber: Color,
    val burn: Color,
)

private val DarkFuelColors = FuelColors(
    surface2 = DarkSurface2,
    surface3 = DarkSurface3,
    border = DarkBorder,
    borderStrong = DarkBorderStrong,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    accent = Lime,
    accentPressed = LimePressed,
    accentDim = LimeDim,
    onAccent = Ink,
    accentOnSurface = Lime,
    calories = SignalRed,
    protein = SignalBlue,
    fiber = Lime,
    burn = SignalOrange,
)

private val LightFuelColors = FuelColors(
    surface2 = LightSurface2,
    surface3 = LightSurface3,
    border = LightBorder,
    borderStrong = LightBorderStrong,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    accent = LimePressed,
    accentPressed = Lime,
    accentDim = LimeDim,
    onAccent = Ink,
    accentOnSurface = LimeInk,
    calories = LightRed,
    protein = LightBlue,
    fiber = LimeInk,
    burn = LightOrange,
)

private val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Ink,
    primaryContainer = LimeDim,
    onPrimaryContainer = Lime,
    secondary = SignalBlue,
    onSecondary = Ink,
    tertiary = SignalOrange,
    onTertiary = Ink,
    background = Ink,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = DarkSurface2,
    surfaceContainerHigh = DarkSurface3,
    outline = DarkBorderStrong,
    outlineVariant = DarkBorder,
    error = SignalRed,
    onError = Ink,
    scrim = Color(0xCC000000),
)

private val LightColors = lightColorScheme(
    primary = LimePressed,
    onPrimary = Ink,
    primaryContainer = LimeDim,
    onPrimaryContainer = LimeInk,
    secondary = LightBlue,
    onSecondary = Color.White,
    tertiary = LightOrange,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightSurface2,
    surfaceContainerHigh = LightSurface3,
    outline = LightBorderStrong,
    outlineVariant = LightBorder,
    error = LightRed,
    onError = Color.White,
    scrim = Color(0x99000000),
)

val FuelShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

private val LocalFuelColors = staticCompositionLocalOf { DarkFuelColors }

object FuelTheme {
    val colors: FuelColors
        @Composable @ReadOnlyComposable get() = LocalFuelColors.current
}

@Composable
fun FuelTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && supportsDynamic && dark -> dynamicDarkColorScheme(context)
        dynamicColor && supportsDynamic -> dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }

    // The macro accents stay branded even under dynamic colour: they encode
    // meaning (calories / protein / fiber), so they must not shift with the
    // wallpaper.
    val fuelColors = if (dark) DarkFuelColors else LightFuelColors

    CompositionLocalProvider(LocalFuelColors provides fuelColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FuelTypography,
            shapes = FuelShapes,
            content = content,
        )
    }
}
