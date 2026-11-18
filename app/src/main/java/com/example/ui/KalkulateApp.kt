package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.ConstantsBottomSheet
import com.example.ui.screens.GraphScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.screens.UnitsScreen
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalkulateApp(
  uiState: KalkulateUiState,
  viewModel: KalkulateViewModel,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  var showHistorySheet by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showConstantsSheet by remember { mutableStateOf(false) }

  val copyWithFeedback: (String) -> Unit = { text ->
    if (text.isNotBlank()) {
      clipboardManager.setText(AnnotatedString(text))
      scope.launch {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar("Copied: $text")
      }
    }
  }

  // Handle back press on secondary tabs to return to Calculator
  BackHandler(enabled = uiState.selectedTab != KalkulateTab.CALCULATOR) {
    viewModel.selectTab(KalkulateTab.CALCULATOR)
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isExpanded = maxWidth >= 720.dp

    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      topBar = {
        KalkulateTopBar(
          themeMode = uiState.preferences.themeMode,
          onToggleTheme = viewModel::toggleDarkLightTheme,
          onOpenSettings = { showSettingsSheet = true },
          modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        )
      },
      bottomBar = {
        if (!isExpanded) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            windowInsets = WindowInsets.navigationBars,
            tonalElevation = 2.dp,
          ) {
            KalkulateTab.entries.forEach { tab ->
              val selected = uiState.selectedTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { viewModel.selectTab(tab) },
                icon = {
                  Icon(
                    imageVector = tabIcon(tab, selected),
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag("nav_tab_${tab.name}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier =
          Modifier.fillMaxSize()
            .padding(innerPadding)
            .then(
              if (isExpanded) Modifier.windowInsetsPadding(WindowInsets.navigationBars)
              else Modifier
            )
      ) {
        if (isExpanded) {
          NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            Spacer(modifier = Modifier.weight(0.2f))
            KalkulateTab.entries
              .filter { it != KalkulateTab.HISTORY } // History shown as side-pane on wide screens
              .forEach { tab ->
                val selected = uiState.selectedTab == tab
                NavigationRailItem(
                  selected = selected,
                  onClick = { viewModel.selectTab(tab) },
                  icon = {
                    Icon(
                      imageVector = tabIcon(tab, selected),
                      contentDescription = tab.label,
                    )
                  },
                  label = { Text(tab.label) },
                  modifier = Modifier.testTag("rail_tab_${tab.name}"),
                )
              }
            Spacer(modifier = Modifier.weight(1f))
          }
          VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          when (uiState.selectedTab) {
            KalkulateTab.CALCULATOR -> {
              if (isExpanded) {
                // Canonical List-Detail / Supporting-Pane layout on wide screens
                Row(modifier = Modifier.fillMaxSize()) {
                  CalculatorScreen(
                    state = uiState.calculator,
                    isScientificMode = uiState.preferences.isScientificMode,
                    angleMode = uiState.preferences.angleMode,
                    memoryValue = uiState.preferences.memoryValue,
                    hapticEnabled = uiState.preferences.hapticEnabled,
                    onDigit = viewModel::appendDigit,
                    onOperator = viewModel::appendOperator,
                    onFunction = viewModel::appendFunction,
                    onConstant = viewModel::appendConstant,
                    onSquare = viewModel::appendSquare,
                    onCube = viewModel::appendCube,
                    onReciprocal = viewModel::appendReciprocal,
                    onSmartParen = viewModel::appendSmartParenthesis,
                    onToggleSign = viewModel::toggleSign,
                    onBackspace = viewModel::backspace,
                    onClearAll = viewModel::clearAll,
                    onEvaluate = viewModel::evaluate,
                    onToggleScientific = viewModel::toggleScientificMode,
                    onCycleAngleMode = viewModel::cycleAngleMode,
                    onToggleSecond = viewModel::toggleSecondMode,
                    onToggleHyp = viewModel::toggleHypMode,
                    onMemoryClear = viewModel::memoryClear,
                    onMemoryRecall = viewModel::memoryRecall,
                    onMemoryAdd = viewModel::memoryAdd,
                    onMemorySubtract = viewModel::memorySubtract,
                    onMemoryStore = viewModel::memoryStore,
                    onOpenHistorySheet = { showHistorySheet = true },
                    onOpenConstantsSheet = { showConstantsSheet = true },
                    onCopyResult = copyWithFeedback,
                    showHistoryButton = false,
                    modifier = Modifier.weight(1.25f).fillMaxHeight(),
                  )
                  VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                  HistoryScreen(
                    history = uiState.history,
                    searchQuery = uiState.historySearchQuery,
                    showPinnedOnly = uiState.showPinnedOnly,
                    onSearchChange = viewModel::updateHistorySearch,
                    onTogglePinnedFilter = viewModel::togglePinnedFilter,
                    onUseExpression = { viewModel.reuseHistoryEntry(it, useResultOnly = false) },
                    onUseResult = { viewModel.reuseHistoryEntry(it, useResultOnly = true) },
                    onTogglePin = viewModel::togglePinHistory,
                    onDeleteEntry = viewModel::deleteHistoryEntry,
                    onClearAll = viewModel::clearAllHistory,
                    onCopyResult = copyWithFeedback,
                    compactMode = true,
                    modifier = Modifier.weight(0.85f).fillMaxHeight(),
                  )
                }
              } else {
                CalculatorScreen(
                  state = uiState.calculator,
                  isScientificMode = uiState.preferences.isScientificMode,
                  angleMode = uiState.preferences.angleMode,
                  memoryValue = uiState.preferences.memoryValue,
                  hapticEnabled = uiState.preferences.hapticEnabled,
                  onDigit = viewModel::appendDigit,
                  onOperator = viewModel::appendOperator,
                  onFunction = viewModel::appendFunction,
                  onConstant = viewModel::appendConstant,
                  onSquare = viewModel::appendSquare,
                  onCube = viewModel::appendCube,
                  onReciprocal = viewModel::appendReciprocal,
                  onSmartParen = viewModel::appendSmartParenthesis,
                  onToggleSign = viewModel::toggleSign,
                  onBackspace = viewModel::backspace,
                  onClearAll = viewModel::clearAll,
                  onEvaluate = viewModel::evaluate,
                  onToggleScientific = viewModel::toggleScientificMode,
                  onCycleAngleMode = viewModel::cycleAngleMode,
                  onToggleSecond = viewModel::toggleSecondMode,
                  onToggleHyp = viewModel::toggleHypMode,
                  onMemoryClear = viewModel::memoryClear,
                  onMemoryRecall = viewModel::memoryRecall,
                  onMemoryAdd = viewModel::memoryAdd,
                  onMemorySubtract = viewModel::memorySubtract,
                  onMemoryStore = viewModel::memoryStore,
                  onOpenHistorySheet = { showHistorySheet = true },
                  onOpenConstantsSheet = { showConstantsSheet = true },
                  onCopyResult = copyWithFeedback,
                  showHistoryButton = true,
                )
              }
            }
            KalkulateTab.GRAPH -> {
              GraphScreen(
                state = uiState.graph,
                evaluateAt = viewModel::evaluateFunctionAt,
                formatNumber = { viewModel.formatDisplayNumber(it) },
                onUpdateFunction = viewModel::updateGraphFunction,
                onToggleVisibility = viewModel::toggleGraphFunctionVisibility,
                onPan = viewModel::panGraph,
                onZoom = viewModel::zoomGraph,
                onResetViewport = viewModel::resetGraphViewport,
                onToggleTrace = viewModel::toggleGraphTrace,
                onUpdateTraceX = viewModel::updateGraphTraceX,
                onToggleAngleMode = viewModel::toggleGraphAngleMode,
              )
            }
            KalkulateTab.UNITS -> {
              UnitsScreen(
                state = uiState.unitConverter,
                onSelectCategory = viewModel::selectUnitCategory,
                onSelectFromUnit = viewModel::selectFromUnit,
                onSelectToUnit = viewModel::selectToUnit,
                onSwapUnits = viewModel::swapUnits,
                onUpdateInput = viewModel::updateUnitInput,
                onSendToCalculator = viewModel::sendValueToCalculator,
                onCopyText = copyWithFeedback,
              )
            }
            KalkulateTab.HISTORY -> {
              HistoryScreen(
                history = uiState.history,
                searchQuery = uiState.historySearchQuery,
                showPinnedOnly = uiState.showPinnedOnly,
                onSearchChange = viewModel::updateHistorySearch,
                onTogglePinnedFilter = viewModel::togglePinnedFilter,
                onUseExpression = { viewModel.reuseHistoryEntry(it, useResultOnly = false) },
                onUseResult = { viewModel.reuseHistoryEntry(it, useResultOnly = true) },
                onTogglePin = viewModel::togglePinHistory,
                onDeleteEntry = viewModel::deleteHistoryEntry,
                onClearAll = viewModel::clearAllHistory,
                onCopyResult = copyWithFeedback,
              )
            }
          }
        }
      }
    }
  }

  // Quick History Modal Sheet (1-tap from Calculator Display)
  if (showHistorySheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showHistorySheet = false },
      sheetState = sheetState,
    ) {
      Box(modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp, max = 560.dp)) {
        HistoryScreen(
          history = uiState.history,
          searchQuery = uiState.historySearchQuery,
          showPinnedOnly = uiState.showPinnedOnly,
          onSearchChange = viewModel::updateHistorySearch,
          onTogglePinnedFilter = viewModel::togglePinnedFilter,
          onUseExpression = {
            viewModel.reuseHistoryEntry(it, useResultOnly = false)
            showHistorySheet = false
          },
          onUseResult = {
            viewModel.reuseHistoryEntry(it, useResultOnly = true)
            showHistorySheet = false
          },
          onTogglePin = viewModel::togglePinHistory,
          onDeleteEntry = viewModel::deleteHistoryEntry,
          onClearAll = viewModel::clearAllHistory,
          onCopyResult = copyWithFeedback,
          compactMode = true,
        )
      }
    }
  }

  if (showSettingsSheet) {
    SettingsBottomSheet(
      preferences = uiState.preferences,
      onDismiss = { showSettingsSheet = false },
      onSelectTheme = viewModel::setThemeMode,
      onSelectAngleMode = viewModel::setAngleMode,
      onPrecisionChange = viewModel::setDecimalPrecision,
      onHapticChange = viewModel::setHapticEnabled,
    )
  }

  if (showConstantsSheet) {
    ConstantsBottomSheet(
      onDismiss = { showConstantsSheet = false },
      onInsertConstant = viewModel::sendValueToCalculator,
      onCopyConstant = copyWithFeedback,
    )
  }
}

@Composable
private fun KalkulateTopBar(
  themeMode: ThemeMode,
  onToggleTheme: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(32.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "Σ",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
        Text(
          text = "Kalkulate",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onToggleTheme,
          modifier = Modifier.size(44.dp).testTag("toggle_theme_button"),
        ) {
          Icon(
            imageVector =
              if (themeMode == ThemeMode.DARK) Icons.Rounded.LightMode
              else Icons.Rounded.DarkMode,
            contentDescription =
              if (themeMode == ThemeMode.DARK) "Switch to Light Mode" else "Switch to Dark Mode",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.size(44.dp).testTag("open_settings_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = "Display and Precision Settings",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  }
}

private fun tabIcon(tab: KalkulateTab, selected: Boolean) =
  when (tab) {
    KalkulateTab.CALCULATOR ->
      if (selected) Icons.Rounded.Calculate else Icons.Outlined.Calculate
    KalkulateTab.GRAPH ->
      if (selected) Icons.Rounded.ShowChart else Icons.Outlined.ShowChart
    KalkulateTab.UNITS ->
      if (selected) Icons.Rounded.Straighten else Icons.Outlined.Straighten
    KalkulateTab.HISTORY ->
      if (selected) Icons.Rounded.History else Icons.Outlined.History
  }
