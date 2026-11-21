package com.devekene.kalkulate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devekene.kalkulate.R
import com.devekene.kalkulate.data.CalculationEntity
import com.devekene.kalkulate.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  history: List<CalculationEntity>,
  searchQuery: String,
  showPinnedOnly: Boolean,
  onSearchChange: (String) -> Unit,
  onTogglePinnedFilter: () -> Unit,
  onUseExpression: (CalculationEntity) -> Unit,
  onUseResult: (CalculationEntity) -> Unit,
  onTogglePin: (CalculationEntity) -> Unit,
  onDeleteEntry: (Long) -> Unit,
  onClearAll: () -> Unit,
  onCopyResult: (String) -> Unit,
  compactMode: Boolean = false,
  modifier: Modifier = Modifier,
) {
  var showClearDialog by remember { mutableStateOf(false) }

  val filteredHistory =
    remember(history, searchQuery, showPinnedOnly) {
      history.filter { item ->
        val matchesPinned = !showPinnedOnly || item.isPinned
        val matchesQuery =
          searchQuery.isBlank() ||
            item.expression.contains(searchQuery, ignoreCase = true) ||
            item.result.contains(searchQuery, ignoreCase = true)
        matchesPinned && matchesQuery
      }
    }

  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text(stringResource(R.string.history_clear_confirm_title)) },
      text = { Text(stringResource(R.string.history_clear_confirm_body)) },
      confirmButton = {
        TextButton(
          onClick = {
            onClearAll()
            showClearDialog = false
          },
          modifier = Modifier.testTag("confirm_clear_history_button"),
        ) {
          Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) {
          Text(stringResource(R.string.cancel))
        }
      },
    )
  }

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = if (compactMode) 12.dp else 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Header & Clear All
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = stringResource(R.string.history_title),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = "${history.size} saved calculation${if (history.size == 1) "" else "s"}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      if (history.isNotEmpty()) {
        TextButton(
          onClick = { showClearDialog = true },
          modifier = Modifier.testTag("clear_history_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteSweep,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.clear),
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    }

    // Search + Filter Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search expressions...") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }) {
              Icon(Icons.Rounded.Close, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.weight(1f).testTag("history_search_input"),
      )

      FilterChip(
        selected = showPinnedOnly,
        onClick = onTogglePinnedFilter,
        label = { Text("Pinned") },
        leadingIcon = {
          Icon(
            imageVector =
              if (showPinnedOnly) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        },
        modifier = Modifier.testTag("history_pinned_filter"),
      )
    }

    // List or Empty State
    if (filteredHistory.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.size(64.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
              )
            }
          }
          Text(
            text =
              if (history.isEmpty()) stringResource(R.string.history_empty_title)
              else "No matching entries",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text =
              if (history.isEmpty()) stringResource(R.string.history_empty_subtitle)
              else "Try clearing your search or filter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("history_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
      ) {
        items(filteredHistory, key = { it.id }) { entry ->
          HistoryCard(
            entry = entry,
            onUseExpression = { onUseExpression(entry) },
            onUseResult = { onUseResult(entry) },
            onTogglePin = { onTogglePin(entry) },
            onDelete = { onDeleteEntry(entry.id) },
            onCopy = { onCopyResult(entry.result) },
          )
        }
      }
    }
  }
}

@Composable
private fun HistoryCard(
  entry: CalculationEntity,
  onUseExpression: () -> Unit,
  onUseResult: () -> Unit,
  onTogglePin: () -> Unit,
  onDelete: () -> Unit,
  onCopy: () -> Unit,
) {
  val timeFormatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border =
      BorderStroke(
        width = 1.dp,
        color =
          if (entry.isPinned) MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
          else MaterialTheme.colorScheme.outlineVariant,
      ),
    modifier = Modifier.fillMaxWidth().testTag("history_card_${entry.id}"),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
          ) {
            Text(
              text = entry.angleMode,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            )
          }
          Text(
            text = timeFormatter.format(Date(entry.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onTogglePin, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector =
                if (entry.isPinned) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
              contentDescription = if (entry.isPinned) "Unpin" else "Pin",
              tint =
                if (entry.isPinned) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(19.dp),
            )
          }
          IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector = Icons.Rounded.ContentCopy,
              contentDescription = "Copy result",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector = Icons.Rounded.DeleteOutline,
              contentDescription = "Delete entry",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(19.dp),
            )
          }
        }
      }

      // Tappable expression to insert expression
      Text(
        text = entry.expression,
        fontFamily = JetBrainsMonoFontFamily,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier =
          Modifier.fillMaxWidth()
            .clickable(onClick = onUseExpression)
            .padding(vertical = 2.dp),
      )

      // Tappable result to insert result
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clickable(onClick = onUseResult)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Tap expr or result to reuse",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        )
        Text(
          text = "= ${entry.result}",
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
