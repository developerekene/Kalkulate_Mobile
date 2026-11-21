package com.devekene.kalkulate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devekene.kalkulate.math.ConversionUnit
import com.devekene.kalkulate.math.PhysicalConstant
import com.devekene.kalkulate.math.UnitCategory
import com.devekene.kalkulate.math.UnitConverterEngine
import com.devekene.kalkulate.ui.UnitConverterUiState
import com.devekene.kalkulate.ui.theme.JetBrainsMonoFontFamily

@Composable
fun UnitsScreen(
  state: UnitConverterUiState,
  onSelectCategory: (UnitCategory) -> Unit,
  onSelectFromUnit: (ConversionUnit) -> Unit,
  onSelectToUnit: (ConversionUnit) -> Unit,
  onSwapUnits: () -> Unit,
  onUpdateInput: (String) -> Unit,
  onSendToCalculator: (String) -> Unit,
  onCopyText: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val units = UnitConverterEngine.unitsByCategory.getValue(state.category)
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Column {
      Text(
        text = "Unit & Constant Lab",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
        text = "Convert scientific quantities or send to calculator",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Category Chips
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      UnitCategory.entries.forEach { cat ->
        FilterChip(
          selected = cat == state.category,
          onClick = { onSelectCategory(cat) },
          label = { Text(cat.title) },
          modifier = Modifier.testTag("unit_cat_${cat.name}"),
        )
      }
    }

    // From Unit Card
    UnitCard(
      label = "From",
      selectedUnit = state.fromUnit,
      availableUnits = units,
      onSelectUnit = onSelectFromUnit,
    ) {
      OutlinedTextField(
        value = state.inputText,
        onValueChange = onUpdateInput,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle =
          MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMonoFontFamily),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("unit_input_field"),
      )
    }

    // Swap Button
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      FilledTonalIconButton(
        onClick = onSwapUnits,
        modifier = Modifier.size(48.dp).testTag("unit_swap_button"),
      ) {
        Icon(Icons.Rounded.SwapVert, contentDescription = "Swap units")
      }
    }

    // To Unit Card
    UnitCard(
      label = "To",
      selectedUnit = state.toUnit,
      availableUnits = units,
      onSelectUnit = onSelectToUnit,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = state.outputText,
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.displayMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f).testTag("unit_output_text"),
        )
        IconButton(onClick = { onCopyText(state.outputText) }) {
          Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy converted value")
        }
      }
    }

    Button(
      onClick = {
        if (state.outputText != "—") onSendToCalculator(state.outputText)
      },
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("unit_send_to_calc"),
      shape = RoundedCornerShape(14.dp),
    ) {
      Icon(
        Icons.AutoMirrored.Rounded.Send,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text("Use in Calculator")
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Physical Constants Reference Section
    Text(
      text = "Physical & Mathematical Constants",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onBackground,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      UnitConverterEngine.physicalConstants.forEach { constant ->
        ConstantRowCard(
          constant = constant,
          onInsert = { onSendToCalculator(constant.insertToken) },
          onCopy = { onCopyText(constant.insertToken) },
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
  }
}

@Composable
private fun UnitCard(
  label: String,
  selectedUnit: ConversionUnit,
  availableUnits: List<ConversionUnit>,
  onSelectUnit: (ConversionUnit) -> Unit,
  content: @Composable () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box {
          Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "${selectedUnit.name} (${selectedUnit.symbol})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Select unit",
                modifier = Modifier.size(18.dp),
              )
            }
          }

          DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            availableUnits.forEach { unit ->
              DropdownMenuItem(
                text = { Text("${unit.name} (${unit.symbol})") },
                onClick = {
                  onSelectUnit(unit)
                  expanded = false
                },
              )
            }
          }
        }
      }
      content()
    }
  }
}

@Composable
fun ConstantRowCard(
  constant: PhysicalConstant,
  onInsert: () -> Unit,
  onCopy: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().clickable(onClick = onInsert),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.secondaryContainer,
          modifier = Modifier.size(42.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = constant.symbol,
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
        }
        Column {
          Text(
            text = constant.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${constant.insertToken} ${constant.unit}",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }

      IconButton(onClick = onCopy) {
        Icon(
          imageVector = Icons.Rounded.ContentCopy,
          contentDescription = "Copy ${constant.name}",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}
