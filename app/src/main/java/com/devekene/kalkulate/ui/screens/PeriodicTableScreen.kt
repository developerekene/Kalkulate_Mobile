package com.devekene.kalkulate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.Send
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.ContentCopy
import androidx.compose.material.icons.sharp.GridView
import androidx.compose.material.icons.sharp.List
import androidx.compose.material.icons.sharp.Public
import androidx.compose.material.icons.sharp.Science
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devekene.kalkulate.data.ChemicalElement
import com.devekene.kalkulate.data.ElementCategory
import com.devekene.kalkulate.data.PeriodicTableData
import com.devekene.kalkulate.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicTableScreen(
  onInsertAtomicMass: (String) -> Unit,
  onCopyText: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<ElementCategory?>(null) }
  var isClassicTableView by remember { mutableStateOf(true) }
  var selectedElement by remember { mutableStateOf<ChemicalElement?>(null) }

  val filteredElements =
    remember(searchQuery, selectedCategory) {
      PeriodicTableData.elements.filter { el ->
        val matchesCategory = selectedCategory == null || el.category == selectedCategory
        val matchesQuery =
          searchQuery.isBlank() ||
            el.name.contains(searchQuery, ignoreCase = true) ||
            el.symbol.contains(searchQuery, ignoreCase = true) ||
            el.atomicNumber.toString() == searchQuery.trim()
        matchesCategory && matchesQuery
      }
    }

  val showTableGrid = isClassicTableView && searchQuery.isBlank() && selectedCategory == null

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search name, symbol, or #...") },
        leadingIcon = { Icon(Icons.Sharp.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Sharp.Close, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.weight(1f).testTag("periodic_search_input"),
      )

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
      ) {
        IconButton(
          onClick = { isClassicTableView = !isClassicTableView },
          modifier = Modifier.size(52.dp).testTag("periodic_view_toggle"),
        ) {
          Icon(
            imageVector = if (isClassicTableView) Icons.Sharp.List else Icons.Sharp.GridView,
            contentDescription =
              if (isClassicTableView) "Switch to List View" else "Switch to Periodic Table View",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterChip(
        selected = selectedCategory == null,
        onClick = { selectedCategory = null },
        label = { Text("All 118") },
        modifier = Modifier.testTag("periodic_cat_all"),
      )
      ElementCategory.entries.forEach { category ->
        FilterChip(
          selected = selectedCategory == category,
          onClick = {
            selectedCategory = if (selectedCategory == category) null else category
          },
          label = { Text(category.label) },
          modifier = Modifier.testTag("periodic_cat_${category.name}"),
        )
      }
    }

    if (showTableGrid) {
      ClassicPeriodicTableGrid(
        elements = PeriodicTableData.elements,
        onSelectElement = { selectedElement = it },
        modifier = Modifier.fillMaxSize().testTag("periodic_classic_grid"),
      )
    } else if (filteredElements.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = "No elements match \"$searchQuery\"",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("periodic_element_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
      ) {
        items(filteredElements, key = { it.atomicNumber }) { element ->
          ElementListRow(
            element = element,
            onClick = { selectedElement = element },
          )
        }
      }
    }
  }

  val activeElement = selectedElement
  if (activeElement != null) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { selectedElement = null },
      sheetState = sheetState,
    ) {
      ElementDetailSheetContent(
        element = activeElement,
        onInsertMass = {
          selectedElement = null
          onInsertAtomicMass(activeElement.numericMass.toString())
        },
        onCopySymbol = { onCopyText(activeElement.symbol) },
      )
    }
  }
}

@Composable
private fun ClassicPeriodicTableGrid(
  elements: List<ChemicalElement>,
  onSelectElement: (ChemicalElement) -> Unit,
  modifier: Modifier = Modifier,
) {
  val elementLookup = remember(elements) { elements.associateBy { it.gridRow to it.gridCol } }
  val horizontalScroll = rememberScrollState()
  val verticalScroll = rememberScrollState()
  val cellWidth = 62.dp
  val cellHeight = 68.dp
  val cellGap = 4.dp

  Column(
    modifier = modifier.verticalScroll(verticalScroll),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = "Tap any element for properties, symbol & where it is found • Scroll horizontally",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Column(
      modifier = Modifier.horizontalScroll(horizontalScroll).padding(bottom = 8.dp),
      verticalArrangement = Arrangement.spacedBy(cellGap),
    ) {
      // Main periods 1..7
      for (row in 1..7) {
        Row(horizontalArrangement = Arrangement.spacedBy(cellGap)) {
          for (col in 1..18) {
            val element = elementLookup[row to col]
            if (element != null) {
              PeriodicElementTile(
                element = element,
                onClick = { onSelectElement(element) },
                modifier = Modifier.width(cellWidth).height(cellHeight),
              )
            } else if ((row == 6 || row == 7) && col == 3) {
              SeriesCalloutTile(
                rangeText = if (row == 6) "57–71" else "89–103",
                seriesLabel = if (row == 6) "La–Lu" else "Ac–Lr",
                modifier = Modifier.width(cellWidth).height(cellHeight),
              )
            } else {
              Spacer(modifier = Modifier.width(cellWidth).height(cellHeight))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Lanthanides (row 9) & Actinides (row 10)
      for (row in listOf(9, 10)) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(cellGap),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier.width(cellWidth * 3 + cellGap * 2).height(cellHeight),
            contentAlignment = Alignment.CenterEnd,
          ) {
            Text(
              text = if (row == 9) "Lanthanides →" else "Actinides →",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(end = 8.dp),
            )
          }
          for (col in 4..18) {
            val element = elementLookup[row to col]
            if (element != null) {
              PeriodicElementTile(
                element = element,
                onClick = { onSelectElement(element) },
                modifier = Modifier.width(cellWidth).height(cellHeight),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PeriodicElementTile(
  element: ChemicalElement,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val (bg, accent) = categoryPalette(element.category)

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = bg,
    border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
    modifier = modifier.clickable(onClick = onClick).testTag("element_tile_${element.symbol}"),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
      ) {
        Text(
          text = "${element.atomicNumber}",
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = accent,
        )
      }
      Text(
        text = element.symbol,
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = element.name,
        fontSize = 8.5.sp,
        lineHeight = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun SeriesCalloutTile(
  rangeText: String,
  seriesLabel: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(4.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = rangeText,
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )
      Text(
        text = seriesLabel,
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun ElementListRow(
  element: ChemicalElement,
  onClick: () -> Unit,
) {
  val (bg, accent) = categoryPalette(element.category)

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("element_row_${element.symbol}"),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.25.dp, accent.copy(alpha = 0.5f)),
        modifier = Modifier.size(54.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxSize().padding(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Text(
            text = "${element.atomicNumber}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
          )
          Text(
            text = element.symbol,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = element.name,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "${element.category.label} • ${element.atomicMass}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
      ) {
        Text(
          text = element.standardState,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun ElementDetailSheetContent(
  element: ChemicalElement,
  onInsertMass: () -> Unit,
  onCopySymbol: () -> Unit,
) {
  val (bg, accent) = categoryPalette(element.category)

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Header Tile + Identity
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = bg,
        border = BorderStroke(2.dp, accent),
        modifier = Modifier.size(84.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxSize().padding(8.dp),
          verticalArrangement = Arrangement.SpaceBetween,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "#${element.atomicNumber}",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
          )
          Text(
            text = element.symbol,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = element.atomicMass,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = element.name,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = bg,
          border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        ) {
          Text(
            text = element.category.label,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
        Text(
          text = "Period ${element.period} • Group ${element.group} • ${element.standardState}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Technical Specs Card
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailSpecRow(label = "Symbol", value = element.symbol)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailSpecRow(label = "Atomic Number", value = "${element.atomicNumber}")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailSpecRow(label = "Atomic Mass", value = element.atomicMass)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailSpecRow(label = "Electron Config", value = element.electronConfig)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailSpecRow(label = "Electronegativity", value = element.electronegativity)
      }
    }

    // Properties Card
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Sharp.Science,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Properties",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Text(
          text = element.properties,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Where It Is Found Card
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Sharp.Public,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Where It Is Found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Text(
          text = element.whereFound,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Actions: Use Atomic Mass / Copy Symbol
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Button(
        onClick = onInsertMass,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.weight(1f).height(48.dp).testTag("element_use_mass_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Sharp.Send,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Use Atomic Mass (${element.numericMass})")
      }

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        IconButton(
          onClick = onCopySymbol,
          modifier = Modifier.size(48.dp).testTag("element_copy_symbol_button"),
        ) {
          Icon(
            imageVector = Icons.Sharp.ContentCopy,
            contentDescription = "Copy symbol ${element.symbol}",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun DetailSpecRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      fontFamily = JetBrainsMonoFontFamily,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun categoryPalette(category: ElementCategory): Pair<Color, Color> =
  when (category) {
    ElementCategory.ALKALI_METAL -> Color(0xFFFFF1F2) to Color(0xFFE11D48)
    ElementCategory.ALKALINE_EARTH -> Color(0xFFFFF7ED) to Color(0xFFEA580C)
    ElementCategory.TRANSITION_METAL -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
    ElementCategory.POST_TRANSITION_METAL -> Color(0xFFF0FDFA) to Color(0xFF0D9488)
    ElementCategory.METALLOID -> Color(0xFFECFDF5) to Color(0xFF059669)
    ElementCategory.REACTIVE_NONMETAL -> Color(0xFFF0F9FF) to Color(0xFF0284C7)
    ElementCategory.NOBLE_GAS -> Color(0xFFEEF2FF) to Color(0xFF4338CA)
    ElementCategory.LANTHANIDE -> Color(0xFFFAF5FF) to Color(0xFF7E22CE)
    ElementCategory.ACTINIDE -> Color(0xFFFDF2F8) to Color(0xFFBE185D)
  }
