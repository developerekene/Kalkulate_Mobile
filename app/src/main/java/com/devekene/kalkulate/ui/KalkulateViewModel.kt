package com.devekene.kalkulate.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devekene.kalkulate.data.CalculationEntity
import com.devekene.kalkulate.data.HistoryRepository
import com.devekene.kalkulate.data.KalkulateDatabase
import com.devekene.kalkulate.data.UserPreferences
import com.devekene.kalkulate.data.UserPreferencesRepository
import com.devekene.kalkulate.math.AngleMode
import com.devekene.kalkulate.math.ConversionUnit
import com.devekene.kalkulate.math.EvaluationResult
import com.devekene.kalkulate.math.MathExpressionEvaluator
import com.devekene.kalkulate.math.UnitCategory
import com.devekene.kalkulate.math.UnitConverterEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class KalkulateTab(val label: String) {
  CALCULATOR("Calculator"),
  GRAPH("Graph"),
  UNITS("Units"),
  HISTORY("History"),
}

data class GraphFunctionSlot(
  val id: Int,
  val expression: String,
  val isVisible: Boolean = true,
)

data class GraphUiState(
  val functions: List<GraphFunctionSlot> =
    listOf(
      GraphFunctionSlot(0, "sin(x)", true),
      GraphFunctionSlot(1, "0.5 * cos(2 * x)", true),
      GraphFunctionSlot(2, "", false),
    ),
  val centerX: Double = 0.0,
  val centerY: Double = 0.0,
  val xSpan: Double = 12.0,
  val traceEnabled: Boolean = false,
  val traceX: Double = 0.0,
  val graphAngleMode: AngleMode = AngleMode.RAD,
)

data class UnitConverterUiState(
  val category: UnitCategory = UnitCategory.LENGTH,
  val fromUnit: ConversionUnit = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.LENGTH)[0],
  val toUnit: ConversionUnit = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.LENGTH)[5],
  val inputText: String = "100",
  val outputText: String = "0.0621371192",
)

data class CalculatorUiState(
  val expression: String = "",
  val lastSolvedExpression: String = "",
  val livePreview: String = "",
  val errorMessage: String? = null,
  val justEvaluated: Boolean = false,
  val isSecondMode: Boolean = false,
  val isHypMode: Boolean = false,
  val ansValue: Double = 0.0,
  val ansFormatted: String = "0",
)

data class KalkulateUiState(
  val selectedTab: KalkulateTab = KalkulateTab.CALCULATOR,
  val calculator: CalculatorUiState = CalculatorUiState(),
  val preferences: UserPreferences = UserPreferences(),
  val history: List<CalculationEntity> = emptyList(),
  val historySearchQuery: String = "",
  val showPinnedOnly: Boolean = false,
  val graph: GraphUiState = GraphUiState(),
  val unitConverter: UnitConverterUiState = UnitConverterUiState(),
)

class KalkulateViewModel(application: Application) : AndroidViewModel(application) {

  private val evaluator = MathExpressionEvaluator()
  private val database = KalkulateDatabase.getInstance(application)
  private val historyRepository = HistoryRepository(database.calculationDao())
  private val preferencesRepository = UserPreferencesRepository(application)

  private val selectedTab = MutableStateFlow(KalkulateTab.CALCULATOR)
  private val calculatorState = MutableStateFlow(CalculatorUiState())
  private val historySearch = MutableStateFlow("")
  private val showPinnedOnly = MutableStateFlow(false)
  private val graphState = MutableStateFlow(GraphUiState())
  private val unitConverterState = MutableStateFlow(UnitConverterUiState())

  private val coreState =
    combine(
      selectedTab,
      calculatorState,
      preferencesRepository.preferencesStream,
      historyRepository.historyStream,
    ) { tab, calc, prefs, history ->
      Quadruple(tab, calc, prefs, history)
    }

  val uiState: StateFlow<KalkulateUiState> =
    combine(coreState, historySearch, showPinnedOnly, graphState, unitConverterState) {
      core,
      search,
      pinnedOnly,
      graph,
      units ->
      val prefs = core.third
      KalkulateUiState(
        selectedTab = core.first,
        calculator = core.second,
        preferences = prefs,
        history = core.fourth,
        historySearchQuery = search,
        showPinnedOnly = pinnedOnly,
        graph = graph,
        unitConverter = units,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = KalkulateUiState(),
    )

  init {
    recalculateUnitOutput()
  }

  fun selectTab(tab: KalkulateTab) {
    selectedTab.value = tab
  }

  fun toggleScientificMode() {
    val current = uiState.value.preferences.isScientificMode
    viewModelScope.launch { preferencesRepository.setScientificMode(!current) }
  }

  fun setScientificMode(enabled: Boolean) {
    viewModelScope.launch { preferencesRepository.setScientificMode(enabled) }
  }

  fun cycleAngleMode() {
    val next = uiState.value.preferences.angleMode.next()
    viewModelScope.launch {
      preferencesRepository.setAngleMode(next)
      updateLivePreview(calculatorState.value.expression, next, uiState.value.preferences.decimalPrecision)
    }
  }

  fun setAngleMode(mode: AngleMode) {
    viewModelScope.launch {
      preferencesRepository.setAngleMode(mode)
      updateLivePreview(calculatorState.value.expression, mode, uiState.value.preferences.decimalPrecision)
    }
  }

  fun setDecimalPrecision(precision: Int) {
    viewModelScope.launch {
      preferencesRepository.setDecimalPrecision(precision)
      updateLivePreview(
        calculatorState.value.expression,
        uiState.value.preferences.angleMode,
        precision,
      )
    }
  }

  fun setHapticEnabled(enabled: Boolean) {
    viewModelScope.launch { preferencesRepository.setHapticEnabled(enabled) }
  }

  fun toggleSecondMode() {
    calculatorState.update { it.copy(isSecondMode = !it.isSecondMode) }
  }

  fun toggleHypMode() {
    calculatorState.update { it.copy(isHypMode = !it.isHypMode) }
  }

  fun appendDigit(digit: String) {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) "" else calc.expression
    val updated =
      if (digit == ".") {
        val currentToken = currentTrailingNumber(base)
        if (currentToken.contains('.')) base
        else if (currentToken.isEmpty()) "${base}0." else "$base."
      } else {
        "$base$digit"
      }
    updateExpression(updated, justEvaluated = false)
  }

  fun appendConstant(token: String) {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) "" else calc.expression
    updateExpression("$base$token", justEvaluated = false)
  }

  fun appendScientificExponent() {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) calc.expression else calc.expression
    val lastChar = base.lastOrNull()
    val updated =
      when {
        base.isEmpty() || lastChar == null || !lastChar.isDigit() -> "${base}1E"
        currentTrailingNumber(base).contains('E', ignoreCase = true) -> base
        else -> "${base}E"
      }
    updateExpression(updated, justEvaluated = false)
  }

  fun appendRandom() {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) "" else calc.expression
    val randomVal = String.format(java.util.Locale.US, "%.4f", Math.random())
    updateExpression("$base$randomVal", justEvaluated = false)
  }

  fun appendFunction(funcPrefix: String) {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) "" else calc.expression
    updateExpression("$base$funcPrefix", justEvaluated = false)
  }

  fun appendOperator(op: String) {
    val calc = calculatorState.value
    val base =
      if (calc.justEvaluated) {
        calc.expression.ifEmpty { calc.ansFormatted }
      } else {
        calc.expression
      }
    if (base.isEmpty()) {
      if (op == "−" || op == "-") {
        updateExpression("−", justEvaluated = false)
      }
      return
    }
    val binaryOps = listOf("+", "−", "×", "÷", "^", " mod ", " nCr ", " nPr ")
    val updated =
      when {
        op in binaryOps && binaryOps.any { base.endsWith(it) } -> {
          val matched = binaryOps.first { base.endsWith(it) }
          base.dropLast(matched.length) + op
        }
        else -> "$base$op"
      }
    updateExpression(updated, justEvaluated = false)
  }

  fun appendSquare() {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) calc.expression else calc.expression
    if (base.isEmpty()) return
    updateExpression("$base^2", justEvaluated = false)
  }

  fun appendCube() {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) calc.expression else calc.expression
    if (base.isEmpty()) return
    updateExpression("$base^3", justEvaluated = false)
  }

  fun appendReciprocal() {
    val calc = calculatorState.value
    val base = calc.expression
    if (base.isEmpty()) {
      updateExpression("1÷(", justEvaluated = false)
    } else if (calc.justEvaluated) {
      updateExpression("1÷($base)", justEvaluated = false)
    } else {
      updateExpression("$base^(-1)", justEvaluated = false)
    }
  }

  fun appendSmartParenthesis() {
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) "" else calc.expression
    val openCount = base.count { it == '(' }
    val closeCount = base.count { it == ')' }
    val lastChar = base.lastOrNull()
    val canClose =
      openCount > closeCount &&
        lastChar != null &&
        (lastChar.isDigit() ||
          lastChar == ')' ||
          lastChar == 'π' ||
          lastChar == 'e' ||
          lastChar == 'φ' ||
          lastChar == '!' ||
          lastChar == '%' ||
          base.endsWith("Ans"))
    val next = if (canClose) "$base)" else "$base("
    updateExpression(next, justEvaluated = false)
  }

  fun toggleSign() {
    val calc = calculatorState.value
    val expr = calc.expression
    if (expr.isEmpty()) {
      updateExpression("−", justEvaluated = false)
      return
    }
    if (expr.startsWith("−") && expr.drop(1).all { it.isDigit() || it == '.' }) {
      updateExpression(expr.drop(1), justEvaluated = false)
      return
    }
    if (expr.all { it.isDigit() || it == '.' }) {
      updateExpression("−$expr", justEvaluated = false)
      return
    }
    val updated =
      if (expr.startsWith("−(") && expr.endsWith(")")) {
        expr.drop(2).dropLast(1)
      } else {
        "−($expr)"
      }
    updateExpression(updated, justEvaluated = false)
  }

  fun backspace() {
    val calc = calculatorState.value
    if (calc.justEvaluated) {
      clearAll()
      return
    }
    val expr = calc.expression
    if (expr.isEmpty()) return
    val multiCharTokens =
      listOf(
        "sinh⁻¹(",
        "cosh⁻¹(",
        "tanh⁻¹(",
        "sin⁻¹(",
        "cos⁻¹(",
        "tan⁻¹(",
        "asinh(",
        "acosh(",
        "atanh(",
        "sinh(",
        "cosh(",
        "tanh(",
        "asin(",
        "acos(",
        "atan(",
        "log₁₀(",
        "log₂(",
        "cbrt(",
        "sqrt(",
        "sin(",
        "cos(",
        "tan(",
        "csc(",
        "sec(",
        "cot(",
        "log(",
        "abs(",
        "exp(",
        "ln(",
        " nCr ",
        " nPr ",
        " mod ",
        "Ans",
        "10^(",
        "2^(",
        "e^(",
        "√(",
        "∛(",
      )
    val matchedToken = multiCharTokens.firstOrNull { expr.endsWith(it) }
    val updated =
      if (matchedToken != null) {
        expr.dropLast(matchedToken.length)
      } else {
        expr.dropLast(1)
      }
    updateExpression(updated, justEvaluated = false)
  }

  fun clearAll() {
    calculatorState.value =
      calculatorState.value.copy(
        expression = "",
        lastSolvedExpression = "",
        livePreview = "",
        errorMessage = null,
        justEvaluated = false,
      )
  }

  fun evaluate() {
    val calc = calculatorState.value
    val expr = calc.expression.trim()
    if (expr.isEmpty()) return

    val prefs = uiState.value.preferences
    when (
      val result =
        evaluator.evaluate(
          expression = expr,
          angleMode = prefs.angleMode,
          ansValue = calc.ansValue,
          precision = prefs.decimalPrecision,
        )
    ) {
      is EvaluationResult.Success -> {
        calculatorState.value =
          calc.copy(
            expression = result.formatted,
            lastSolvedExpression = "$expr =",
            livePreview = "",
            errorMessage = null,
            justEvaluated = true,
            ansValue = result.value,
            ansFormatted = result.formatted,
          )
        viewModelScope.launch {
          historyRepository.recordCalculation(
            expression = expr,
            result = result.formatted,
            numericValue = result.value,
            angleMode = prefs.angleMode.shortLabel,
          )
        }
      }
      is EvaluationResult.Error -> {
        calculatorState.value =
          calc.copy(
            errorMessage = result.message,
            livePreview = "",
          )
      }
    }
  }

  // Memory Operations
  fun memoryClear() {
    viewModelScope.launch { preferencesRepository.setMemoryValue(0.0) }
  }

  fun memoryRecall() {
    val mem = uiState.value.preferences.memoryValue
    val formatted = evaluator.formatNumber(mem, uiState.value.preferences.decimalPrecision)
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) "" else calc.expression
    updateExpression("$base$formatted", justEvaluated = false)
  }

  fun memoryStore() {
    val currentVal = resolveCurrentNumericValue() ?: return
    viewModelScope.launch { preferencesRepository.setMemoryValue(currentVal) }
  }

  fun memoryAdd() {
    val currentVal = resolveCurrentNumericValue() ?: return
    val newVal = uiState.value.preferences.memoryValue + currentVal
    viewModelScope.launch { preferencesRepository.setMemoryValue(newVal) }
  }

  fun memorySubtract() {
    val currentVal = resolveCurrentNumericValue() ?: return
    val newVal = uiState.value.preferences.memoryValue - currentVal
    viewModelScope.launch { preferencesRepository.setMemoryValue(newVal) }
  }

  private fun resolveCurrentNumericValue(): Double? {
    val calc = calculatorState.value
    val prefs = uiState.value.preferences
    val target = calc.expression.ifEmpty { return null }
    return when (
      val res =
        evaluator.evaluate(
          expression = target,
          angleMode = prefs.angleMode,
          ansValue = calc.ansValue,
          precision = prefs.decimalPrecision,
        )
    ) {
      is EvaluationResult.Success -> res.value
      is EvaluationResult.Error -> null
    }
  }

  private fun updateExpression(newExpr: String, justEvaluated: Boolean) {
    calculatorState.update {
      it.copy(
        expression = newExpr,
        errorMessage = null,
        justEvaluated = justEvaluated,
      )
    }
    val prefs = uiState.value.preferences
    updateLivePreview(newExpr, prefs.angleMode, prefs.decimalPrecision)
  }

  private fun updateLivePreview(expr: String, angleMode: AngleMode, precision: Int) {
    val trimmed = expr.trim()
    if (trimmed.isEmpty() || trimmed.toDoubleOrNull() != null) {
      calculatorState.update { it.copy(livePreview = "") }
      return
    }
    val result =
      evaluator.evaluate(
        expression = trimmed,
        angleMode = angleMode,
        ansValue = calculatorState.value.ansValue,
        precision = precision,
      )
    val preview =
      when (result) {
        is EvaluationResult.Success ->
          if (result.formatted == trimmed) "" else "= ${result.formatted}"
        is EvaluationResult.Error -> ""
      }
    calculatorState.update { it.copy(livePreview = preview) }
  }

  private fun currentTrailingNumber(expr: String): String {
    var i = expr.length - 1
    while (i >= 0 && (expr[i].isDigit() || expr[i] == '.')) {
      i--
    }
    return expr.substring(i + 1)
  }

  // History Actions
  fun updateHistorySearch(query: String) {
    historySearch.value = query
  }

  fun togglePinnedFilter() {
    showPinnedOnly.update { !it }
  }

  fun reuseHistoryEntry(entry: CalculationEntity, useResultOnly: Boolean) {
    val calc = calculatorState.value
    val textToInsert = if (useResultOnly) entry.result else entry.expression
    val updated =
      if (calc.justEvaluated || calc.expression.isEmpty()) textToInsert
      else "${calc.expression}$textToInsert"
    updateExpression(updated, justEvaluated = false)
    selectedTab.value = KalkulateTab.CALCULATOR
  }

  fun togglePinHistory(entry: CalculationEntity) {
    viewModelScope.launch { historyRepository.togglePin(entry) }
  }

  fun deleteHistoryEntry(id: Long) {
    viewModelScope.launch { historyRepository.deleteById(id) }
  }

  fun clearAllHistory() {
    viewModelScope.launch { historyRepository.clearAll() }
  }

  // Graph Actions
  fun updateGraphFunction(slotId: Int, expression: String) {
    graphState.update { state ->
      state.copy(
        functions =
          state.functions.map { slot ->
            if (slot.id == slotId) slot.copy(expression = expression, isVisible = expression.isNotBlank())
            else slot
          }
      )
    }
  }

  fun toggleGraphFunctionVisibility(slotId: Int) {
    graphState.update { state ->
      state.copy(
        functions =
          state.functions.map { slot ->
            if (slot.id == slotId) slot.copy(isVisible = !slot.isVisible) else slot
          }
      )
    }
  }

  fun panGraph(deltaX: Double, deltaY: Double) {
    graphState.update {
      it.copy(centerX = it.centerX + deltaX, centerY = it.centerY + deltaY)
    }
  }

  fun zoomGraph(scaleFactor: Double) {
    graphState.update {
      val nextSpan = (it.xSpan / scaleFactor).coerceIn(1.0, 200.0)
      it.copy(xSpan = nextSpan)
    }
  }

  fun resetGraphViewport() {
    graphState.update {
      it.copy(centerX = 0.0, centerY = 0.0, xSpan = 12.0, traceX = 0.0)
    }
  }

  fun toggleGraphTrace() {
    graphState.update { it.copy(traceEnabled = !it.traceEnabled, traceX = it.centerX) }
  }

  fun updateGraphTraceX(x: Double) {
    graphState.update { it.copy(traceX = x) }
  }

  fun toggleGraphAngleMode() {
    graphState.update {
      val next = if (it.graphAngleMode == AngleMode.RAD) AngleMode.DEG else AngleMode.RAD
      it.copy(graphAngleMode = next)
    }
  }

  fun evaluateFunctionAt(expression: String, x: Double, angleMode: AngleMode): Double? {
    if (expression.isBlank()) return null
    return when (
      val res =
        evaluator.evaluate(
          expression = expression,
          angleMode = angleMode,
          ansValue = calculatorState.value.ansValue,
          xValue = x,
          precision = 8,
        )
    ) {
      is EvaluationResult.Success -> res.value
      is EvaluationResult.Error -> null
    }
  }

  fun formatDisplayNumber(value: Double, precision: Int = 6): String =
    evaluator.formatNumber(value, precision)

  // Unit Converter Actions
  fun selectUnitCategory(category: UnitCategory) {
    val units = UnitConverterEngine.unitsByCategory.getValue(category)
    val from = units.first()
    val to = if (units.size > 1) units[1] else units.first()
    unitConverterState.update {
      it.copy(category = category, fromUnit = from, toUnit = to)
    }
    recalculateUnitOutput()
  }

  fun selectFromUnit(unit: ConversionUnit) {
    unitConverterState.update { it.copy(fromUnit = unit) }
    recalculateUnitOutput()
  }

  fun selectToUnit(unit: ConversionUnit) {
    unitConverterState.update { it.copy(toUnit = unit) }
    recalculateUnitOutput()
  }

  fun swapUnits() {
    unitConverterState.update {
      it.copy(fromUnit = it.toUnit, toUnit = it.fromUnit)
    }
    recalculateUnitOutput()
  }

  fun updateUnitInput(input: String) {
    val sanitized = input.filter { it.isDigit() || it == '.' || it == '-' }
    unitConverterState.update { it.copy(inputText = sanitized) }
    recalculateUnitOutput()
  }

  fun sendValueToCalculator(valueText: String) {
    val clean = valueText.trim()
    if (clean.isEmpty()) return
    val calc = calculatorState.value
    val base = if (calc.justEvaluated) "" else calc.expression
    updateExpression("$base$clean", justEvaluated = false)
    selectedTab.value = KalkulateTab.CALCULATOR
  }

  private fun recalculateUnitOutput() {
    val state = unitConverterState.value
    val parsed = state.inputText.toDoubleOrNull()
    val result =
      if (parsed == null) {
        "—"
      } else {
        val converted =
          UnitConverterEngine.convert(parsed, state.category, state.fromUnit, state.toUnit)
        evaluator.formatNumber(converted, uiState.value.preferences.decimalPrecision)
      }
    unitConverterState.update { it.copy(outputText = result) }
  }

  private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
  )
}
