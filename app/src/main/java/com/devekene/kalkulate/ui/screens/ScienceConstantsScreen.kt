package com.devekene.kalkulate.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.Send
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.ContentCopy
import androidx.compose.material.icons.sharp.Science
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devekene.kalkulate.math.ScienceConstant
import com.devekene.kalkulate.math.ScienceConstantDiscipline
import com.devekene.kalkulate.math.ScienceConstantsRepository
import com.devekene.kalkulate.ui.theme.JetBrainsMonoFontFamily

@Composable
fun ScienceConstantsScreen(
  onInsertConstant: (String) -> Unit,
  onCopyConstant: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedDiscipline by remember { mutableStateOf<ScienceConstantDiscipline?>(null) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredConstants =
    remember(selectedDiscipline, searchQuery) {
      ScienceConstantsRepository.allConstants.filter { item ->
        val matchesDiscipline = selectedDiscipline == null || item.discipline == selectedDiscipline
        val matchesQuery =
          searchQuery.isBlank() ||
            item.name.contains(searchQuery, ignoreCase = true) ||
            item.symbol.contains(searchQuery, ignoreCase = true) ||
            item.unit.contains(searchQuery, ignoreCase = true) ||
            item.discipline.title.contains(searchQuery, ignoreCase = true)
        matchesDiscipline && matchesQuery
      }
    }

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = "Constants across Engineering, Math, Physics, Biology & Chemistry",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search constants, symbols, or units...") },
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
      modifier = Modifier.fillMaxWidth().testTag("science_constants_search"),
    )

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterChip(
        selected = selectedDiscipline == null,
        onClick = { selectedDiscipline = null },
        label = { Text("All (${ScienceConstantsRepository.allConstants.size})") },
        modifier = Modifier.testTag("const_filter_all"),
      )
      ScienceConstantDiscipline.entries.forEach { discipline ->
        FilterChip(
          selected = selectedDiscipline == discipline,
          onClick = {
            selectedDiscipline = if (selectedDiscipline == discipline) null else discipline
          },
          label = { Text(discipline.title) },
          modifier = Modifier.testTag("const_filter_${discipline.name}"),
        )
      }
    }

    if (filteredConstants.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Sharp.Science,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
          )
          Text(
            text = "No matching constants found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("science_constants_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
      ) {
        items(filteredConstants, key = { it.id }) { constant ->
          ScienceConstantCard(
            constant = constant,
            onUse = { onInsertConstant(constant.insertToken) },
            onCopy = { onCopyConstant(constant.insertToken) },
          )
        }
      }
    }
  }
}

@Composable
private fun ScienceConstantCard(
  constant: ScienceConstant,
  onUse: () -> Unit,
  onCopy: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().testTag("const_card_${constant.id}"),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(48.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = constant.symbol,
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
              maxLines = 1,
            )
          }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = constant.name,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = constant.discipline.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }

          Text(
            text = "${constant.valueDisplay} ${constant.unit}",
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }

      Text(
        text = constant.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(
          onClick = onUse,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).height(48.dp).testTag("const_use_${constant.id}"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Sharp.Send,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Use in Calculator")
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          IconButton(
            onClick = onCopy,
            modifier = Modifier.size(48.dp).testTag("const_copy_${constant.id}"),
          ) {
            Icon(
              imageVector = Icons.Sharp.ContentCopy,
              contentDescription = "Copy ${constant.name}",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
    }
  }
}
