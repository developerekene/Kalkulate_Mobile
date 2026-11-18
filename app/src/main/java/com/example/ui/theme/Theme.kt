package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
  DARK("Dark"),
  LIGHT("Light"),
  SYSTEM("System"),
}

@Immutable
data class KalkulateKeyColors(
  val displayBackground: Color,
  val displayBorder: Color,
  val numberKey: Color,
  val numberKeyContent: Color,
  val operatorKey: Color,
  val operatorKeyContent: Color,
  val scientificKey: Color,
  val scientificKeyContent: Color,
  val utilityKey: Color,
  val utilityKeyContent: Color,
  val equalsKey: Color,
  val equalsKeyContent: Color,
  val activeToggleKey: Color,
  val activeToggleContent: Color,
  val graphColors: List<Color>,
)

val LocalKalkulateKeyColors = staticCompositionLocalOf {
  KalkulateKeyColors(
    displayBackground = DarkDisplaySurface,
    displayBorder = DarkOutline,
    numberKey = DarkSurfaceContainerLow,
    numberKeyContent = DarkTextPrimary,
    operatorKey = DarkPrimaryContainer,
    operatorKeyContent = DarkPrimary,
    scientificKey = DarkSurfaceContainer,
    scientificKeyContent = DarkTextPrimary,
    utilityKey = DarkSurfaceContainerHigh,
    utilityKeyContent = DarkTextSecondary,
    equalsKey = DarkSecondary,
    equalsKeyContent = DarkOnSecondary,
    activeToggleKey = DarkTertiaryContainer,
    activeToggleContent = DarkTertiary,
    graphColors = listOf(DarkPrimary, DarkSecondary, DarkTertiary),
  )
}

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.55f),
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.6f),
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
  )

private val DarkKeyPalette =
  KalkulateKeyColors(
    displayBackground = DarkDisplaySurface,
    displayBorder = DarkOutline,
    numberKey = DarkSurfaceContainerLow,
    numberKeyContent = DarkTextPrimary,
    operatorKey = DarkPrimaryContainer,
    operatorKeyContent = DarkPrimary,
    scientificKey = DarkSurfaceContainer,
    scientificKeyContent = DarkTextPrimary,
    utilityKey = DarkSurfaceContainerHigh,
    utilityKeyContent = DarkTextSecondary,
    equalsKey = DarkSecondary,
    equalsKeyContent = DarkOnSecondary,
    activeToggleKey = DarkTertiaryContainer,
    activeToggleContent = DarkTertiary,
    graphColors = listOf(DarkPrimary, DarkSecondary, DarkTertiary),
  )

private val LightKeyPalette =
  KalkulateKeyColors(
    displayBackground = LightDisplaySurface,
    displayBorder = LightOutline,
    numberKey = LightSurface,
    numberKeyContent = LightTextPrimary,
    operatorKey = LightPrimaryContainer,
    operatorKeyContent = LightPrimary,
    scientificKey = LightSurfaceContainerLow,
    scientificKeyContent = LightTextPrimary,
    utilityKey = LightSurfaceContainer,
    utilityKeyContent = LightTextSecondary,
    equalsKey = LightSecondary,
    equalsKeyContent = LightOnSecondary,
    activeToggleKey = LightTertiaryContainer,
    activeToggleContent = LightTertiary,
    graphColors = listOf(LightPrimary, LightSecondary, LightTertiary),
  )

val KalkulateShapes =
  Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun KalkulateTheme(
  themeMode: ThemeMode = ThemeMode.DARK,
  content: @Composable () -> Unit,
) {
  val useDarkTheme =
    when (themeMode) {
      ThemeMode.DARK -> true
      ThemeMode.LIGHT -> false
      ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

  val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
  val keyColors = if (useDarkTheme) DarkKeyPalette else LightKeyPalette

  CompositionLocalProvider(LocalKalkulateKeyColors provides keyColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = KalkulateTypography,
      shapes = KalkulateShapes,
      content = content,
    )
  }
}
