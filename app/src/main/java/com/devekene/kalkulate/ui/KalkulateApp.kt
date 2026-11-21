package com.devekene.kalkulate.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.sharp.AutoGraph
import androidx.compose.material.icons.sharp.Calculate
import androidx.compose.material.icons.sharp.History
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material.icons.sharp.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devekene.kalkulate.ui.screens.AboutDeveloperScreen
import com.devekene.kalkulate.ui.screens.AppSettingsScreen
import com.devekene.kalkulate.ui.screens.CalculatorScreen
import com.devekene.kalkulate.ui.screens.CommunityScreen
import com.devekene.kalkulate.ui.screens.ConstantsBottomSheet
import com.devekene.kalkulate.ui.screens.DisplayAndPrecisionScreen
import com.devekene.kalkulate.ui.screens.GraphScreen
import com.devekene.kalkulate.ui.screens.HistoryScreen
import com.devekene.kalkulate.ui.screens.MenuDestination
import com.devekene.kalkulate.ui.screens.MenuModalSheet
import com.devekene.kalkulate.ui.screens.SubScreenTopBar
import com.devekene.kalkulate.ui.screens.UnitsScreen
import com.devekene.kalkulate.ui.theme.JetBrainsMonoFontFamily
import com.devekene.kalkulate.ui.theme.SpaceGroteskFontFamily
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
  var showMenuModal by remember { mutableStateOf(false) }
  var activeMenuScreen by remember { mutableStateOf<MenuDestination?>(null) }
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

  // Handle back press on menu sub-screens first, then secondary tabs
  BackHandler(enabled = activeMenuScreen != null || uiState.selectedTab != KalkulateTab.CALCULATOR) {
    if (activeMenuScreen != null) {
      activeMenuScreen = null
    } else {
      viewModel.selectTab(KalkulateTab.CALCULATOR)
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isExpanded = maxWidth >= 720.dp

    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      topBar = {
        val currentSubScreen = activeMenuScreen
        if (currentSubScreen != null) {
          SubScreenTopBar(
            title = currentSubScreen.title,
            onBack = { activeMenuScreen = null },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
          )
        } else {
          KalkulateTopBar(
            onOpenMenu = { showMenuModal = true },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
          )
        }
      },
      bottomBar = {
        if (!isExpanded && activeMenuScreen == null) {
          FloatingKalkulateBottomBar(
            selectedTab = uiState.selectedTab,
            onSelectTab = viewModel::selectTab,
          )
        }
      },
    ) { innerPadding ->
      val currentSubScreen = activeMenuScreen
      if (currentSubScreen != null) {
        Box(
          modifier =
            Modifier.fillMaxSize()
              .padding(innerPadding)
              .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
          when (currentSubScreen) {
            MenuDestination.DISPLAY_PRECISION -> {
              DisplayAndPrecisionScreen(
                preferences = uiState.preferences,
                onSelectAngleMode = viewModel::setAngleMode,
                onPrecisionChange = viewModel::setDecimalPrecision,
                onHapticChange = viewModel::setHapticEnabled,
              )
            }
            MenuDestination.ABOUT_DEVELOPER -> {
              AboutDeveloperScreen()
            }
            MenuDestination.SETTINGS -> {
              AppSettingsScreen(
                preferences = uiState.preferences,
                graphAngleMode = uiState.graph.graphAngleMode,
                historyCount = uiState.history.size,
                onSetScientificMode = viewModel::setScientificMode,
                onToggleGraphAngleMode = viewModel::toggleGraphAngleMode,
                onClearMemory = viewModel::memoryClear,
                onClearHistory = viewModel::clearAllHistory,
              )
            }
            MenuDestination.COMMUNITY -> {
              CommunityScreen()
            }
          }
        }
      } else {
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
                .filter { it != KalkulateTab.HISTORY }
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
                      onScientificExponent = viewModel::appendScientificExponent,
                      onRandom = viewModel::appendRandom,
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
                    onScientificExponent = viewModel::appendScientificExponent,
                    onRandom = viewModel::appendRandom,
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
  }

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

  if (showMenuModal) {
    MenuModalSheet(
      onDismiss = { showMenuModal = false },
      onNavigate = { destination -> activeMenuScreen = destination },
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

/**
 * Fancy floating pill bottom navigation bar with 20.dp left and right horizontal margins,
 * crisp blue border, elevation, animated active indicator pill, and sharp icons.
 */
@Composable
private fun FloatingKalkulateBottomBar(
  selectedTab: KalkulateTab,
  onSelectTab: (KalkulateTab) -> Unit,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 10.dp,
      tonalElevation = 4.dp,
      border = BorderStroke(1.25.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
      modifier = Modifier.fillMaxWidth().testTag("floating_bottom_nav"),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        KalkulateTab.entries.forEach { tab ->
          val selected = selectedTab == tab
          val pillColor by
            animateColorAsState(
              targetValue =
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent,
              label = "tab_pill_bg",
            )
          val contentColor by
            animateColorAsState(
              targetValue =
                if (selected) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant,
              label = "tab_pill_fg",
            )
          val iconScale by
            animateFloatAsState(
              targetValue = if (selected) 1.08f else 1f,
              animationSpec = spring(stiffness = Spring.StiffnessMedium),
              label = "tab_icon_scale",
            )

          Box(
            modifier =
              Modifier.weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(pillColor)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(bounded = true),
                  onClick = { onSelectTab(tab) },
                )
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .testTag("nav_tab_${tab.name}"),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Icon(
                imageVector = tabIcon(tab, selected),
                contentDescription = tab.label,
                tint = contentColor,
                modifier =
                  Modifier.size(22.dp).graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                  },
              )
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun KalkulateTopBar(
  onOpenMenu: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      KalkulateWordmarkLogo()

      IconButton(
        onClick = onOpenMenu,
        modifier = Modifier.size(48.dp).testTag("open_menu_button"),
      ) {
        Icon(
          imageVector = Icons.Sharp.Menu,
          contentDescription = "Open Menu",
          tint = MaterialTheme.colorScheme.onBackground,
        )
      }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
  }
}

/**
 * Custom Kalkulate brand logo that fuses a miniature blue calculator (LCD display + 2x2 keypad)
 * directly inside the "Kalkulate" wordmark.
 */
@Composable
private fun KalkulateWordmarkLogo(modifier: Modifier = Modifier) {
  val primaryBlue = MaterialTheme.colorScheme.primary
  val containerBlue = MaterialTheme.colorScheme.primaryContainer
  val textPrimary = MaterialTheme.colorScheme.onBackground

  Row(
    modifier =
      modifier.semantics(mergeDescendants = true) { contentDescription = "Kalkulate" }
        .testTag("kalkulate_logo"),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "K",
      fontFamily = SpaceGroteskFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
      letterSpacing = (-0.5).sp,
      color = primaryBlue,
    )

    // Miniature calculator body fused inside the wordmark (replaces 'a' silhouette)
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = primaryBlue,
      border = BorderStroke(1.dp, primaryBlue),
      modifier = Modifier.padding(horizontal = 1.5.dp).offset(y = 1.dp).width(22.dp).height(24.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxSize().padding(2.5.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(5.dp)
              .background(color = Color.White, shape = RoundedCornerShape(2.dp)),
          contentAlignment = Alignment.CenterEnd,
        ) {
          Box(
            modifier =
              Modifier.padding(end = 1.5.dp)
                .width(6.dp)
                .height(1.5.dp)
                .background(primaryBlue, RoundedCornerShape(1.dp))
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            MiniCalcCell(symbol = "+", bg = containerBlue, fg = primaryBlue, modifier = Modifier.weight(1f))
            MiniCalcCell(symbol = "×", bg = containerBlue, fg = primaryBlue, modifier = Modifier.weight(1f))
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            MiniCalcCell(symbol = "−", bg = containerBlue, fg = primaryBlue, modifier = Modifier.weight(1f))
            MiniCalcCell(symbol = "=", bg = Color.White, fg = primaryBlue, modifier = Modifier.weight(1f))
          }
        }
      }
    }

    Text(
      text = "lkulate",
      fontFamily = SpaceGroteskFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 23.sp,
      letterSpacing = (-0.5).sp,
      color = textPrimary,
    )
  }
}

@Composable
private fun MiniCalcCell(
  symbol: String,
  bg: Color,
  fg: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.height(6.5.dp).background(color = bg, shape = RoundedCornerShape(1.5.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = symbol,
      fontFamily = JetBrainsMonoFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 5.5.sp,
      lineHeight = 6.sp,
      color = fg,
    )
  }
}

private fun tabIcon(tab: KalkulateTab, selected: Boolean) =
  when (tab) {
    KalkulateTab.CALCULATOR ->
      if (selected) Icons.Sharp.Calculate else Icons.Outlined.Calculate
    KalkulateTab.GRAPH ->
      if (selected) Icons.Sharp.AutoGraph else Icons.Outlined.AutoGraph
    KalkulateTab.UNITS ->
      if (selected) Icons.Sharp.Straighten else Icons.Outlined.Straighten
    KalkulateTab.HISTORY ->
      if (selected) Icons.Sharp.History else Icons.Outlined.History
  }
