package com.devekene.kalkulate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devekene.kalkulate.math.AngleMode
import com.devekene.kalkulate.ui.GraphUiState
import com.devekene.kalkulate.ui.theme.JetBrainsMonoFontFamily
import com.devekene.kalkulate.ui.theme.LocalKalkulateKeyColors
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun GraphScreen(
  state: GraphUiState,
  evaluateAt: (String, Double, AngleMode) -> Double?,
  formatNumber: (Double) -> String,
  onUpdateFunction: (Int, String) -> Unit,
  onToggleVisibility: (Int) -> Unit,
  onPan: (Double, Double) -> Unit,
  onZoom: (Double) -> Unit,
  onResetViewport: () -> Unit,
  onToggleTrace: () -> Unit,
  onUpdateTraceX: (Double) -> Unit,
  onToggleAngleMode: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val keyColors = LocalKalkulateKeyColors.current
  val plotColors = keyColors.graphColors
  var selectedSlot by remember { mutableIntStateOf(0) }
  val scrollState = rememberScrollState()
  val textMeasurer = rememberTextMeasurer()

  val presets =
    listOf("sin(x)", "cos(x)", "tan(x)", "x^2 - 4", "exp(-x^2)", "ln(abs(x))", "sin(x)/x")

  Column(
    modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Header Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = "Function Grapher",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          text = "Drag canvas to pan • Trace points",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          onClick = onToggleAngleMode,
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.testTag("graph_angle_toggle"),
        ) {
          Text(
            text = state.graphAngleMode.shortLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          )
        }
        FilterChip(
          selected = state.traceEnabled,
          onClick = onToggleTrace,
          label = { Text("Trace") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Rounded.MyLocation,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          },
          modifier = Modifier.testTag("graph_trace_toggle"),
        )
      }
    }

    // Interactive Plotter Canvas
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = keyColors.displayBackground,
      border = BorderStroke(1.dp, keyColors.displayBorder),
      modifier = Modifier.fillMaxWidth().height(290.dp),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        val crosshairColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

        Canvas(
          modifier =
            Modifier.fillMaxSize()
              .clipToBounds()
              .pointerInput(state.xSpan) {
                detectDragGestures { change, dragAmount ->
                  change.consume()
                  val unitsPerPx = state.xSpan / size.width.coerceAtLeast(1)
                  onPan(-dragAmount.x * unitsPerPx, dragAmount.y * unitsPerPx)
                }
              }
              .testTag("graph_canvas")
        ) {
          val w = size.width
          val h = size.height
          if (w <= 0f || h <= 0f) return@Canvas

          val xMin = state.centerX - state.xSpan / 2.0
          val xMax = state.centerX + state.xSpan / 2.0
          val ySpan = state.xSpan * (h / w.toDouble())
          val yMin = state.centerY - ySpan / 2.0
          val yMax = state.centerY + ySpan / 2.0

          fun toScreenX(x: Double): Float = ((x - xMin) / (xMax - xMin) * w).toFloat()
          fun toScreenY(y: Double): Float = (h - (y - yMin) / (yMax - yMin) * h).toFloat()

          // Compute nice grid step
          val rawStep = state.xSpan / 6.0
          val mag = 10.0.pow(floor(log10(rawStep)))
          val norm = rawStep / mag
          val step =
            when {
              norm < 1.5 -> 1.0 * mag
              norm < 3.5 -> 2.0 * mag
              norm < 7.5 -> 5.0 * mag
              else -> 10.0 * mag
            }

          // Vertical grid lines
          var gx = ceil(xMin / step) * step
          while (gx <= xMax) {
            val sx = toScreenX(gx)
            drawLine(gridColor, Offset(sx, 0f), Offset(sx, h), strokeWidth = 1f)
            if (abs(gx) > step * 0.1) {
              val axisY = toScreenY(0.0).coerceIn(16f, h - 24f)
              val label = formatNumber(gx)
              drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset((sx + 3f).coerceIn(4f, w - 36f), axisY + 3f),
                style =
                  TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = labelColor,
                  ),
              )
            }
            gx += step
          }

          // Horizontal grid lines
          var gy = ceil(yMin / step) * step
          while (gy <= yMax) {
            val sy = toScreenY(gy)
            drawLine(gridColor, Offset(0f, sy), Offset(w, sy), strokeWidth = 1f)
            if (abs(gy) > step * 0.1) {
              val axisX = toScreenX(0.0).coerceIn(4f, w - 42f)
              val label = formatNumber(gy)
              drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(axisX + 4f, (sy - 14f).coerceIn(4f, h - 18f)),
                style =
                  TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = labelColor,
                  ),
              )
            }
            gy += step
          }

          // Main X & Y axes
          if (yMin <= 0.0 && yMax >= 0.0) {
            val ay = toScreenY(0.0)
            drawLine(axisColor, Offset(0f, ay), Offset(w, ay), strokeWidth = 2f)
          }
          if (xMin <= 0.0 && xMax >= 0.0) {
            val ax = toScreenX(0.0)
            drawLine(axisColor, Offset(ax, 0f), Offset(ax, h), strokeWidth = 2f)
          }

          // Plot active functions
          val samples = (w / 2f).toInt().coerceIn(140, 360)
          state.functions.forEachIndexed { index, slot ->
            if (!slot.isVisible || slot.expression.isBlank()) return@forEachIndexed
            val curveColor = plotColors[index % plotColors.size]
            val path = Path()
            var penDown = false
            var prevY: Double? = null

            for (s in 0..samples) {
              val x = xMin + (xMax - xMin) * (s.toDouble() / samples)
              val y = evaluateAt(slot.expression, x, state.graphAngleMode)
              if (y == null || y.isNaN() || y.isInfinite()) {
                penDown = false
                prevY = null
                continue
              }
              val sx = toScreenX(x)
              val sy = toScreenY(y)

              // Avoid drawing vertical spikes across asymptotes (like tan(x))
              val jumpTooLarge = prevY != null && abs(y - prevY) > ySpan * 1.2
              if (!penDown || jumpTooLarge) {
                path.moveTo(sx, sy)
                penDown = true
              } else {
                path.lineTo(sx, sy)
              }
              prevY = y
            }
            drawPath(
              path = path,
              color = curveColor,
              style = Stroke(width = 4.5f, cap = StrokeCap.Round),
            )
          }

          // Trace Crosshair
          if (state.traceEnabled) {
            val tx = state.traceX.coerceIn(xMin, xMax)
            val screenTx = toScreenX(tx)
            drawLine(
              color = crosshairColor,
              start = Offset(screenTx, 0f),
              end = Offset(screenTx, h),
              strokeWidth = 2f,
            )
            state.functions.forEachIndexed { index, slot ->
              if (!slot.isVisible || slot.expression.isBlank()) return@forEachIndexed
              val ty = evaluateAt(slot.expression, tx, state.graphAngleMode)
              if (ty != null && !ty.isNaN() && !ty.isInfinite()) {
                val screenTy = toScreenY(ty)
                if (screenTy in -20f..(h + 20f)) {
                  val c = plotColors[index % plotColors.size]
                  drawCircle(color = c, radius = 9f, center = Offset(screenTx, screenTy))
                  drawCircle(color = Color.White, radius = 4f, center = Offset(screenTx, screenTy))
                }
              }
            }
          }
        }

        // Zoom & Reset Floating Controls
        Row(
          modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
          ) {
            Row {
              IconButton(
                onClick = { onZoom(1.4) },
                modifier = Modifier.size(40.dp).testTag("graph_zoom_in"),
              ) {
                Icon(Icons.Rounded.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
              }
              IconButton(
                onClick = { onZoom(1.0 / 1.4) },
                modifier = Modifier.size(40.dp).testTag("graph_zoom_out"),
              ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
              }
              IconButton(
                onClick = onResetViewport,
                modifier = Modifier.size(40.dp).testTag("graph_reset"),
              ) {
                Icon(
                  Icons.Rounded.CenterFocusStrong,
                  contentDescription = "Reset Viewport",
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    }

    // Trace Slider & Readout
    if (state.traceEnabled) {
      val xMin = state.centerX - state.xSpan / 2.0
      val xMax = state.centerX + state.xSpan / 2.0
      val traceFraction = ((state.traceX - xMin) / (xMax - xMin)).toFloat().coerceIn(0f, 1f)

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "x = ${formatNumber(state.traceX)}",
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            state.functions.forEachIndexed { idx, slot ->
              if (slot.isVisible && slot.expression.isNotBlank()) {
                val y = evaluateAt(slot.expression, state.traceX, state.graphAngleMode)
                Text(
                  text = "f${idx + 1}: ${y?.let(formatNumber) ?: "undef"}",
                  fontFamily = JetBrainsMonoFontFamily,
                  color = plotColors[idx % plotColors.size],
                  style = MaterialTheme.typography.labelMedium,
                )
              }
            }
          }
          Slider(
            value = traceFraction,
            onValueChange = { frac -> onUpdateTraceX(xMin + frac * (xMax - xMin)) },
            modifier = Modifier.testTag("graph_trace_slider"),
          )
        }
      }
    }

    // Function Slots Editor
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      state.functions.forEachIndexed { idx, slot ->
        val slotColor = plotColors[idx % plotColors.size]
        val isSelected = selectedSlot == slot.id
        Surface(
          onClick = { selectedSlot = slot.id },
          shape = RoundedCornerShape(16.dp),
          color =
            if (isSelected) MaterialTheme.colorScheme.surfaceContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
          border =
            BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) slotColor else MaterialTheme.colorScheme.outlineVariant,
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(shape = CircleShape, color = slotColor, modifier = Modifier.size(10.dp)) {}
            Text(
              text = "f${idx + 1}(x) =",
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.SemiBold,
              color = slotColor,
            )
            OutlinedTextField(
              value = slot.expression,
              onValueChange = { onUpdateFunction(slot.id, it) },
              placeholder = { Text("e.g. sin(x) or x^2") },
              singleLine = true,
              textStyle =
                MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFontFamily),
              modifier = Modifier.weight(1f).testTag("graph_input_${slot.id}"),
            )
            IconButton(onClick = { onToggleVisibility(slot.id) }) {
              Icon(
                imageVector =
                  if (slot.isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                contentDescription = "Toggle visibility",
                tint =
                  if (slot.isVisible) slotColor
                  else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              )
            }
          }
        }
      }
    }

    // Quick Presets Row
    Text(
      text = "Quick Presets (inserts into f${selectedSlot + 1})",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      presets.forEach { preset ->
        AssistChip(
          onClick = { onUpdateFunction(selectedSlot, preset) },
          label = { Text(preset, fontFamily = JetBrainsMonoFontFamily) },
        )
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
  }
}
