package com.devekene.kalkulate.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class KalkulateKeyColors(
  val displayBackground: Color,
  val displayBorder: Color,
  val numberKey: Color,
  val numberKeyBorder: Color,
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
    displayBackground = LightDisplaySurface,
    displayBorder = DisplayBorder,
    numberKey = LightSurfaceContainerLow,
    numberKeyBorder = NumberKeyBorder,
    numberKeyContent = LightTextPrimary,
    operatorKey = BluePrimaryContainer,
    operatorKeyContent = BluePrimary,
    scientificKey = LightSurfaceContainer,
    scientificKeyContent = LightTextPrimary,
    utilityKey = LightSurfaceContainerHigh,
    utilityKeyContent = LightTextSecondary,
    equalsKey = BluePrimary,
    equalsKeyContent = BlueOnPrimary,
    activeToggleKey = BlueTertiaryContainer,
    activeToggleContent = BlueTertiary,
    graphColors = listOf(BluePrimary, Color(0xFF0284C7), BlueTertiary),
  )
}

private val LightColorScheme =
  lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = BlueSecondary,
    onSecondary = BlueOnSecondary,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = BlueOnSecondaryContainer,
    tertiary = BlueTertiary,
    onTertiary = BlueOnTertiary,
    tertiaryContainer = BlueTertiaryContainer,
    onTertiaryContainer = BlueOnTertiaryContainer,
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
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
  )

private val LightKeyPalette =
  KalkulateKeyColors(
    displayBackground = LightDisplaySurface,
    displayBorder = DisplayBorder,
    numberKey = LightSurfaceContainerLow,
    numberKeyBorder = NumberKeyBorder,
    numberKeyContent = LightTextPrimary,
    operatorKey = BluePrimaryContainer,
    operatorKeyContent = BluePrimary,
    scientificKey = LightSurfaceContainer,
    scientificKeyContent = LightTextPrimary,
    utilityKey = LightSurfaceContainerHigh,
    utilityKeyContent = LightTextSecondary,
    equalsKey = BluePrimary,
    equalsKeyContent = BlueOnPrimary,
    activeToggleKey = BlueTertiaryContainer,
    activeToggleContent = BlueTertiary,
    graphColors = listOf(BluePrimary, Color(0xFF0284C7), BlueTertiary),
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
fun KalkulateTheme(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalKalkulateKeyColors provides LightKeyPalette) {
    MaterialTheme(
      colorScheme = LightColorScheme,
      typography = KalkulateTypography,
      shapes = KalkulateShapes,
      content = content,
    )
  }
}
