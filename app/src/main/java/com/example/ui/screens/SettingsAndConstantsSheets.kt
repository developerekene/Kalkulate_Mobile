package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UserPreferences
import com.example.math.AngleMode
import com.example.math.UnitConverterEngine
import com.example.ui.theme.ThemeMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
  preferences: UserPreferences,
  onDismiss: () -> Unit,
  onSelectTheme: (ThemeMode) -> Unit,
  onSelectAngleMode: (AngleMode) -> Unit,
  onPrecisionChange: (Int) -> Unit,
  onHapticChange: (Boolean) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )

      // Theme selector
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Interface Theme",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          ThemeMode.entries.forEach { mode ->
            FilterChip(
              selected = preferences.themeMode == mode,
              onClick = { onSelectTheme(mode) },
              label = { Text(mode.label) },
              modifier = Modifier.testTag("theme_chip_${mode.name}"),
            )
          }
        }
      }

      // Angle Mode selector
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Default Angle Unit",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AngleMode.entries.forEach { mode ->
            FilterChip(
              selected = preferences.angleMode == mode,
              onClick = { onSelectAngleMode(mode) },
              label = { Text("${mode.shortLabel} (${mode.fullName})") },
              modifier = Modifier.testTag("settings_angle_${mode.name}"),
            )
          }
        }
      }

      // Decimal Precision Slider
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Decimal Precision",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${preferences.decimalPrecision} digits",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        Slider(
          value = preferences.decimalPrecision.toFloat(),
          onValueChange = { onPrecisionChange(it.roundToInt()) },
          valueRange = 2f..12f,
          steps = 9,
          modifier = Modifier.testTag("precision_slider"),
        )
      }

      // Haptic Feedback Switch
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Key Haptic Feedback",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Subtle tactile vibration when tapping keys",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = preferences.hapticEnabled,
          onCheckedChange = onHapticChange,
          modifier = Modifier.testTag("haptic_switch"),
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstantsBottomSheet(
  onDismiss: () -> Unit,
  onInsertConstant: (String) -> Unit,
  onCopyConstant: (String) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = stringResource(R.string.constants_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Tap any constant to insert it into your calculation.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      UnitConverterEngine.physicalConstants.forEach { constant ->
        ConstantRowCard(
          constant = constant,
          onInsert = {
            onInsertConstant(constant.insertToken)
            onDismiss()
          },
          onCopy = { onCopyConstant(constant.insertToken) },
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
