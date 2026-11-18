package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AngleMode
import com.example.ui.CalculatorUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalKalkulateKeyColors

@Composable
fun CalculatorScreen(
  state: CalculatorUiState,
  isScientificMode: Boolean,
  angleMode: AngleMode,
  memoryValue: Double,
  hapticEnabled: Boolean,
  onDigit: (String) -> Unit,
  onOperator: (String) -> Unit,
  onFunction: (String) -> Unit,
  onConstant: (String) -> Unit,
  onSquare: () -> Unit,
  onCube: () -> Unit,
  onReciprocal: () -> Unit,
  onSmartParen: () -> Unit,
  onToggleSign: () -> Unit,
  onBackspace: () -> Unit,
  onClearAll: () -> Unit,
  onEvaluate: () -> Unit,
  onToggleScientific: () -> Unit,
  onCycleAngleMode: () -> Unit,
  onToggleSecond: () -> Unit,
  onToggleHyp: () -> Unit,
  onMemoryClear: () -> Unit,
  onMemoryRecall: () -> Unit,
  onMemoryAdd: () -> Unit,
  onMemorySubtract: () -> Unit,
  onMemoryStore: () -> Unit,
  onOpenHistorySheet: () -> Unit,
  onOpenConstantsSheet: () -> Unit,
  onCopyResult: (String) -> Unit,
  showHistoryButton: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val keyColors = LocalKalkulateKeyColors.current
  val haptic = LocalHapticFeedback.current
  val triggerHaptic = {
    if (hapticEnabled) {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // 1. Precision Display Card
    DisplayPanel(
      state = state,
      angleMode = angleMode,
      memoryValue = memoryValue,
      isScientificMode = isScientificMode,
      showHistoryButton = showHistoryButton,
      onCycleAngleMode = {
        triggerHaptic()
        onCycleAngleMode()
      },
      onToggleScientific = {
        triggerHaptic()
        onToggleScientific()
      },
      onMemoryRecall = {
        triggerHaptic()
        onMemoryRecall()
      },
      onOpenHistorySheet = onOpenHistorySheet,
      onOpenConstantsSheet = onOpenConstantsSheet,
      onCopyResult = onCopyResult,
      modifier =
        Modifier.fillMaxWidth().weight(if (isScientificMode) 0.24f else 0.34f, fill = true).heightIn(min = 126.dp),
    )

    // 2. Quick Utility Strip (Mode Switch, Angle, Sign, Ans, Memory Store)
    QuickUtilityStrip(
      isScientificMode = isScientificMode,
      angleMode = angleMode,
      onToggleScientific = {
        triggerHaptic()
        onToggleScientific()
      },
      onCycleAngleMode = {
        triggerHaptic()
        onCycleAngleMode()
      },
      onToggleSign = {
        triggerHaptic()
        onToggleSign()
      },
      onInsertAns = {
        triggerHaptic()
        onConstant("Ans")
      },
      onMemoryStore = {
        triggerHaptic()
        onMemoryStore()
      },
    )

    // 3. Collapsible Scientific Drawer
    AnimatedVisibility(
      visible = isScientificMode,
      enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
      exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(),
    ) {
      ScientificKeypad(
        isSecondMode = state.isSecondMode,
        isHypMode = state.isHypMode,
        hasMemory = memoryValue != 0.0,
        onToggleSecond = {
          triggerHaptic()
          onToggleSecond()
        },
        onToggleHyp = {
          triggerHaptic()
          onToggleHyp()
        },
        onMemoryClear = {
          triggerHaptic()
          onMemoryClear()
        },
        onMemoryRecall = {
          triggerHaptic()
          onMemoryRecall()
        },
        onMemoryAdd = {
          triggerHaptic()
          onMemoryAdd()
        },
        onMemorySubtract = {
          triggerHaptic()
          onMemorySubtract()
        },
        onFunction = {
          triggerHaptic()
          onFunction(it)
        },
        onOperator = {
          triggerHaptic()
          onOperator(it)
        },
        onConstant = {
          triggerHaptic()
          onConstant(it)
        },
        onSquare = {
          triggerHaptic()
          onSquare()
        },
        onCube = {
          triggerHaptic()
          onCube()
        },
        onReciprocal = {
          triggerHaptic()
          onReciprocal()
        },
      )
    }

    // 4. Primary Basic Keypad
    BasicKeypad(
      onDigit = {
        triggerHaptic()
        onDigit(it)
      },
      onOperator = {
        triggerHaptic()
        onOperator(it)
      },
      onSmartParen = {
        triggerHaptic()
        onSmartParen()
      },
      onBackspace = {
        triggerHaptic()
        onBackspace()
      },
      onClearAll = {
        triggerHaptic()
        onClearAll()
      },
      onEvaluate = {
        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onEvaluate()
      },
      modifier = Modifier.fillMaxWidth().weight(if (isScientificMode) 0.44f else 0.66f, fill = true),
    )
  }
}

@Composable
private fun DisplayPanel(
  state: CalculatorUiState,
  angleMode: AngleMode,
  memoryValue: Double,
  isScientificMode: Boolean,
  showHistoryButton: Boolean,
  onCycleAngleMode: () -> Unit,
  onToggleScientific: () -> Unit,
  onMemoryRecall: () -> Unit,
  onOpenHistorySheet: () -> Unit,
  onOpenConstantsSheet: () -> Unit,
  onCopyResult: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val keyColors = LocalKalkulateKeyColors.current
  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val exprScroll = rememberScrollState()

  LaunchedEffect(state.expression) { exprScroll.animateScrollTo(exprScroll.maxValue) }

  val exprTextSize =
    when {
      state.expression.length <= 10 -> 40.sp
      state.expression.length <= 16 -> 32.sp
      state.expression.length <= 24 -> 26.sp
      else -> 22.sp
    }

  Surface(
    modifier = modifier.testTag("display_panel"),
    shape = RoundedCornerShape(24.dp),
    color = keyColors.displayBackground,
    border = BorderStroke(1.dp, keyColors.displayBorder.copy(alpha = 0.7f)),
    tonalElevation = 2.dp,
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize().drawBehind {
          drawCircle(
            brush =
              Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.15f),
                radius = size.width * 0.55f,
              )
          )
          drawCircle(
            brush =
              Brush.radialGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.9f),
                radius = size.width * 0.4f,
              )
          )
        }
    ) {
      Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Top status badges & quick actions inside Display
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Angle Mode Pill
            Surface(
              onClick = onCycleAngleMode,
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
              modifier = Modifier.testTag("display_angle_badge"),
            ) {
              Text(
                text = angleMode.shortLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }

            // Mode indicator pill
            Surface(
              onClick = onToggleScientific,
              shape = RoundedCornerShape(10.dp),
              color =
                if (isScientificMode) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
              modifier = Modifier.testTag("display_mode_badge"),
            ) {
              Text(
                text = if (isScientificMode) "SCI" else "BASIC",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color =
                  if (isScientificMode) MaterialTheme.colorScheme.tertiary
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              )
            }

            if (memoryValue != 0.0) {
              Surface(
                onClick = onMemoryRecall,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.testTag("memory_indicator_badge"),
              ) {
                Text(
                  text = "M",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                )
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onOpenConstantsSheet,
              modifier = Modifier.size(40.dp).testTag("open_constants_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Science,
                contentDescription = "Physical Constants",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
            if (showHistoryButton) {
              IconButton(
                onClick = onOpenHistorySheet,
                modifier = Modifier.size(40.dp).testTag("open_history_sheet_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.History,
                  contentDescription = "Quick History Log",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(21.dp),
                )
              }
            }
          }
        }

        // Expression & Live Result Area
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.End,
        ) {
          if (state.lastSolvedExpression.isNotEmpty()) {
            Text(
              text = state.lastSolvedExpression,
              style =
                MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("last_solved_expression"),
            )
          }

          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clickable(enabled = state.expression.isNotEmpty()) {
                  onCopyResult(state.expression)
                },
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = state.expression.ifEmpty { "0" },
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = exprTextSize,
              lineHeight = exprTextSize * 1.15f,
              color =
                if (state.expression.isEmpty())
                  MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              textAlign = TextAlign.End,
              modifier =
                Modifier.weight(1f)
                  .horizontalScroll(exprScroll)
                  .testTag("calculator_expression_text"),
            )
            if (state.expression.isNotEmpty() && state.justEvaluated) {
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "Copy result",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                modifier = Modifier.size(16.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          // Error or Live Preview Line
          when {
            state.errorMessage != null -> {
              Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                modifier = Modifier.testTag("calculator_error_text"),
              )
            }
            state.livePreview.isNotEmpty() -> {
              Text(
                text = state.livePreview,
                style =
                  MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontFeatureSettings = "tnum",
                  ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("calculator_live_preview"),
              )
            }
            else -> {
              Text(
                text = " ",
                style = MaterialTheme.typography.titleLarge,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun QuickUtilityStrip(
  isScientificMode: Boolean,
  angleMode: AngleMode,
  onToggleScientific: () -> Unit,
  onCycleAngleMode: () -> Unit,
  onToggleSign: () -> Unit,
  onInsertAns: () -> Unit,
  onMemoryStore: () -> Unit,
) {
  val keyColors = LocalKalkulateKeyColors.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Primary Button to Switch Between Basic and Scientific Mode
    val modeBg by
      animateColorAsState(
        targetValue =
          if (isScientificMode) MaterialTheme.colorScheme.tertiaryContainer
          else MaterialTheme.colorScheme.primaryContainer,
        label = "sci_toggle_bg",
      )
    val modeFg by
      animateColorAsState(
        targetValue =
          if (isScientificMode) MaterialTheme.colorScheme.tertiary
          else MaterialTheme.colorScheme.primary,
        label = "sci_toggle_fg",
      )
    Surface(
      onClick = onToggleScientific,
      shape = RoundedCornerShape(14.dp),
      color = modeBg,
      border = BorderStroke(1.dp, modeFg.copy(alpha = 0.35f)),
      modifier = Modifier.heightIn(min = 44.dp).weight(1.45f).testTag("scientific_mode_toggle"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector =
            if (isScientificMode) Icons.Rounded.Calculate else Icons.Rounded.Functions,
          contentDescription = null,
          tint = modeFg,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isScientificMode) "Basic Mode" else "Scientific",
          style = MaterialTheme.typography.labelLarge,
          color = modeFg,
          maxLines = 1,
        )
      }
    }

    // Angle mode switch pill
    Surface(
      onClick = onCycleAngleMode,
      shape = RoundedCornerShape(14.dp),
      color = keyColors.utilityKey,
      modifier = Modifier.heightIn(min = 44.dp).weight(0.8f).testTag("angle_mode_button"),
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
        Text(
          text = angleMode.shortLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }

    // Sign toggle +/-
    Surface(
      onClick = onToggleSign,
      shape = RoundedCornerShape(14.dp),
      color = keyColors.utilityKey,
      modifier = Modifier.heightIn(min = 44.dp).weight(0.65f).testTag("sign_toggle_button"),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = "±",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }

    // Ans button
    Surface(
      onClick = onInsertAns,
      shape = RoundedCornerShape(14.dp),
      color = keyColors.utilityKey,
      modifier = Modifier.heightIn(min = 44.dp).weight(0.75f).testTag("ans_button"),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = "Ans",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }

    // MS (Memory Store)
    Surface(
      onClick = onMemoryStore,
      shape = RoundedCornerShape(14.dp),
      color = keyColors.utilityKey,
      modifier = Modifier.heightIn(min = 44.dp).weight(0.7f).testTag("memory_store_button"),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = "MS",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.secondary,
        )
      }
    }
  }
}

@Composable
private fun ScientificKeypad(
  isSecondMode: Boolean,
  isHypMode: Boolean,
  hasMemory: Boolean,
  onToggleSecond: () -> Unit,
  onToggleHyp: () -> Unit,
  onMemoryClear: () -> Unit,
  onMemoryRecall: () -> Unit,
  onMemoryAdd: () -> Unit,
  onMemorySubtract: () -> Unit,
  onFunction: (String) -> Unit,
  onOperator: (String) -> Unit,
  onConstant: (String) -> Unit,
  onSquare: () -> Unit,
  onCube: () -> Unit,
  onReciprocal: () -> Unit,
) {
  val keyColors = LocalKalkulateKeyColors.current
  val gap = 6.dp
  val rowHeight = 44.dp

  Column(verticalArrangement = Arrangement.spacedBy(gap)) {
    // Row 0: 2nd, HYP, Memory Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      SciKey(
        label = "2nd",
        background = if (isSecondMode) keyColors.activeToggleKey else keyColors.utilityKey,
        contentColor = if (isSecondMode) keyColors.activeToggleContent else MaterialTheme.colorScheme.onSurface,
        onClick = onToggleSecond,
        height = rowHeight,
        testTag = "sci_2nd_button",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "HYP",
        background = if (isHypMode) keyColors.activeToggleKey else keyColors.utilityKey,
        contentColor = if (isHypMode) keyColors.activeToggleContent else MaterialTheme.colorScheme.onSurface,
        onClick = onToggleHyp,
        height = rowHeight,
        testTag = "sci_hyp_button",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "MC",
        background = keyColors.utilityKey,
        contentColor =
          if (hasMemory) MaterialTheme.colorScheme.secondary
          else keyColors.utilityKeyContent,
        onClick = onMemoryClear,
        height = rowHeight,
        testTag = "sci_mc_button",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "MR",
        background = keyColors.utilityKey,
        contentColor =
          if (hasMemory) MaterialTheme.colorScheme.secondary
          else keyColors.utilityKeyContent,
        onClick = onMemoryRecall,
        height = rowHeight,
        testTag = "sci_mr_button",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "M+",
        background = keyColors.utilityKey,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onMemoryAdd,
        height = rowHeight,
        testTag = "sci_m_plus_button",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "M−",
        background = keyColors.utilityKey,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onMemorySubtract,
        height = rowHeight,
        testTag = "sci_m_minus_button",
        modifier = Modifier.weight(1f),
      )
    }

    // Row 1: Advanced Trigonometry & Hyperbolic
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      val trigLabels =
        when {
          isSecondMode && isHypMode ->
            listOf("sinh⁻¹" to "sinh⁻¹(", "cosh⁻¹" to "cosh⁻¹(", "tanh⁻¹" to "tanh⁻¹(", "|x|" to "abs(", "⌊x⌋" to "floor(", "⌈x⌉" to "ceil(")
          isHypMode ->
            listOf("sinh" to "sinh(", "cosh" to "cosh(", "tanh" to "tanh(", "csch" to "csch(", "sech" to "sech(", "coth" to "coth(")
          isSecondMode ->
            listOf("sin⁻¹" to "sin⁻¹(", "cos⁻¹" to "cos⁻¹(", "tan⁻¹" to "tan⁻¹(", "acsc" to "acsc(", "asec" to "asec(", "acot" to "acot(")
          else ->
            listOf("sin" to "sin(", "cos" to "cos(", "tan" to "tan(", "csc" to "csc(", "sec" to "sec(", "cot" to "cot(")
        }
      trigLabels.forEach { (label, token) ->
        SciKey(
          label = label,
          background = keyColors.scientificKey,
          contentColor = MaterialTheme.colorScheme.primary,
          onClick = { onFunction(token) },
          height = rowHeight,
          testTag = "sci_btn_${label}",
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Row 2: Powers, Roots & Logarithms
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      if (!isSecondMode) {
        SciKey(
          label = "x²",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = onSquare,
          height = rowHeight,
          testTag = "sci_btn_square",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "xʸ",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onOperator("^") },
          height = rowHeight,
          testTag = "sci_btn_power",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "√",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onFunction("√(") },
          height = rowHeight,
          testTag = "sci_btn_sqrt",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "ln",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onFunction("ln(") },
          height = rowHeight,
          testTag = "sci_btn_ln",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "log",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onFunction("log(") },
          height = rowHeight,
          testTag = "sci_btn_log",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "x!",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onOperator("!") },
          height = rowHeight,
          testTag = "sci_btn_factorial",
          modifier = Modifier.weight(1f),
        )
      } else {
        SciKey(
          label = "x³",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = onCube,
          height = rowHeight,
          testTag = "sci_btn_cube",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "1/x",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = onReciprocal,
          height = rowHeight,
          testTag = "sci_btn_reciprocal",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "∛",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onFunction("∛(") },
          height = rowHeight,
          testTag = "sci_btn_cbrt",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "eˣ",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onFunction("e^(") },
          height = rowHeight,
          testTag = "sci_btn_ex",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "10ˣ",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onFunction("10^(") },
          height = rowHeight,
          testTag = "sci_btn_10x",
          modifier = Modifier.weight(1f),
        )
        SciKey(
          label = "log₂",
          background = keyColors.scientificKey,
          contentColor = keyColors.scientificKeyContent,
          onClick = { onFunction("log₂(") },
          height = rowHeight,
          testTag = "sci_btn_log2",
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Row 3: Constants & Combinatorics
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      SciKey(
        label = "π",
        background = keyColors.scientificKey,
        contentColor = MaterialTheme.colorScheme.secondary,
        onClick = { onConstant("π") },
        height = rowHeight,
        testTag = "sci_btn_pi",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "e",
        background = keyColors.scientificKey,
        contentColor = MaterialTheme.colorScheme.secondary,
        onClick = { onConstant("e") },
        height = rowHeight,
        testTag = "sci_btn_e",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "φ",
        background = keyColors.scientificKey,
        contentColor = MaterialTheme.colorScheme.secondary,
        onClick = { onConstant("φ") },
        height = rowHeight,
        testTag = "sci_btn_phi",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "nCr",
        background = keyColors.scientificKey,
        contentColor = keyColors.scientificKeyContent,
        onClick = { onOperator(" nCr ") },
        height = rowHeight,
        testTag = "sci_btn_ncr",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "nPr",
        background = keyColors.scientificKey,
        contentColor = keyColors.scientificKeyContent,
        onClick = { onOperator(" nPr ") },
        height = rowHeight,
        testTag = "sci_btn_npr",
        modifier = Modifier.weight(1f),
      )
      SciKey(
        label = "mod",
        background = keyColors.scientificKey,
        contentColor = keyColors.scientificKeyContent,
        onClick = { onOperator(" mod ") },
        height = rowHeight,
        testTag = "sci_btn_mod",
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun BasicKeypad(
  onDigit: (String) -> Unit,
  onOperator: (String) -> Unit,
  onSmartParen: () -> Unit,
  onBackspace: () -> Unit,
  onClearAll: () -> Unit,
  onEvaluate: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val keyColors = LocalKalkulateKeyColors.current
  val gap = 8.dp

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(gap),
  ) {
    // Row 1: AC, ( ), %, ÷
    Row(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      CalcKey(
        label = "AC",
        background = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.error,
        onClick = onClearAll,
        testTag = "key_ac",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      CalcKey(
        label = "( )",
        background = keyColors.operatorKey,
        contentColor = keyColors.operatorKeyContent,
        onClick = onSmartParen,
        testTag = "key_paren",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      CalcKey(
        label = "%",
        background = keyColors.operatorKey,
        contentColor = keyColors.operatorKeyContent,
        onClick = { onOperator("%") },
        testTag = "key_percent",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      CalcKey(
        label = "÷",
        background = keyColors.operatorKey,
        contentColor = keyColors.operatorKeyContent,
        onClick = { onOperator("÷") },
        testTag = "key_divide",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 2: 7, 8, 9, ×
    Row(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      listOf("7", "8", "9").forEach { digit ->
        CalcKey(
          label = digit,
          background = keyColors.numberKey,
          contentColor = keyColors.numberKeyContent,
          onClick = { onDigit(digit) },
          testTag = "key_$digit",
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )
      }
      CalcKey(
        label = "×",
        background = keyColors.operatorKey,
        contentColor = keyColors.operatorKeyContent,
        onClick = { onOperator("×") },
        testTag = "key_multiply",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 3: 4, 5, 6, −
    Row(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      listOf("4", "5", "6").forEach { digit ->
        CalcKey(
          label = digit,
          background = keyColors.numberKey,
          contentColor = keyColors.numberKeyContent,
          onClick = { onDigit(digit) },
          testTag = "key_$digit",
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )
      }
      CalcKey(
        label = "−",
        background = keyColors.operatorKey,
        contentColor = keyColors.operatorKeyContent,
        onClick = { onOperator("−") },
        testTag = "key_minus",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 4: 1, 2, 3, +
    Row(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      listOf("1", "2", "3").forEach { digit ->
        CalcKey(
          label = digit,
          background = keyColors.numberKey,
          contentColor = keyColors.numberKeyContent,
          onClick = { onDigit(digit) },
          testTag = "key_$digit",
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )
      }
      CalcKey(
        label = "+",
        background = keyColors.operatorKey,
        contentColor = keyColors.operatorKeyContent,
        onClick = { onOperator("+") },
        testTag = "key_plus",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 5: 0, ., ⌫, =
    Row(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
      CalcKey(
        label = "0",
        background = keyColors.numberKey,
        contentColor = keyColors.numberKeyContent,
        onClick = { onDigit("0") },
        testTag = "key_0",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      CalcKey(
        label = ".",
        background = keyColors.numberKey,
        contentColor = keyColors.numberKeyContent,
        onClick = { onDigit(".") },
        testTag = "key_dot",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      IconCalcKey(
        contentDescription = "Backspace",
        background = keyColors.numberKey,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = onBackspace,
        testTag = "key_backspace",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.Backspace,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(22.dp),
        )
      }
      CalcKey(
        label = "=",
        background = keyColors.equalsKey,
        contentColor = keyColors.equalsKeyContent,
        onClick = onEvaluate,
        testTag = "key_equals",
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
  }
}

@Composable
private fun SciKey(
  label: String,
  background: Color,
  contentColor: Color,
  onClick: () -> Unit,
  height: Dp,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.92f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMedium),
      label = "sci_key_scale",
    )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(12.dp),
    color = background,
    modifier =
      modifier
        .height(height)
        .minimumInteractiveComponentSize()
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .testTag(testTag),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 2.dp)) {
      Text(
        text = label,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = if (label.length >= 5) 12.sp else 14.sp,
        color = contentColor,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun CalcKey(
  label: String,
  background: Color,
  contentColor: Color,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.94f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMedium),
      label = "calc_key_scale",
    )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(18.dp),
    color = background,
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .testTag(testTag),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize =
          when (label) {
            "AC", "( )" -> 19.sp
            "÷", "×", "−", "+", "=", "%" -> 24.sp
            else -> 23.sp
          },
        color = contentColor,
      )
    }
  }
}

@Composable
private fun IconCalcKey(
  contentDescription: String,
  background: Color,
  contentColor: Color,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
  iconContent: @Composable () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.94f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMedium),
      label = "icon_key_scale",
    )

  Surface(
    onClick = onClick,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(18.dp),
    color = background,
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .semantics { this.contentDescription = contentDescription }
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .testTag(testTag),
  ) {
    Box(contentAlignment = Alignment.Center) { iconContent() }
  }
}
