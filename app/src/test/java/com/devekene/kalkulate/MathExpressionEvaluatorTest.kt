package com.devekene.kalkulate

import com.devekene.kalkulate.math.AngleMode
import com.devekene.kalkulate.math.EvaluationResult
import com.devekene.kalkulate.math.MathExpressionEvaluator
import com.devekene.kalkulate.math.UnitCategory
import com.devekene.kalkulate.math.UnitConverterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathExpressionEvaluatorTest {

  private val evaluator = MathExpressionEvaluator()

  @Test
  fun `basic arithmetic and order of operations`() {
    val res = evaluator.evaluate("2 + 3 × 4 − 6 ÷ 2")
    assertTrue(res is EvaluationResult.Success)
    assertEquals("11", (res as EvaluationResult.Success).formatted)
  }

  @Test
  fun `trigonometry respects DEG RAD and GRAD`() {
    val degSin = evaluator.evaluate("sin(30)", AngleMode.DEG) as EvaluationResult.Success
    assertEquals("0.5", degSin.formatted)

    val degCos90 = evaluator.evaluate("cos(90)", AngleMode.DEG) as EvaluationResult.Success
    assertEquals("0", degCos90.formatted)

    val radSin = evaluator.evaluate("sin(π ÷ 2)", AngleMode.RAD) as EvaluationResult.Success
    assertEquals("1", radSin.formatted)

    val gradSin = evaluator.evaluate("sin(100)", AngleMode.GRAD) as EvaluationResult.Success
    assertEquals("1", gradSin.formatted)
  }

  @Test
  fun `inverse trig and hyperbolic functions`() {
    val asinRes = evaluator.evaluate("sin⁻¹(0.5)", AngleMode.DEG) as EvaluationResult.Success
    assertEquals("30", asinRes.formatted)

    val coshRes = evaluator.evaluate("cosh(0)", AngleMode.RAD) as EvaluationResult.Success
    assertEquals("1", coshRes.formatted)

    val cscRes = evaluator.evaluate("csc(30)", AngleMode.DEG) as EvaluationResult.Success
    assertEquals("2", cscRes.formatted)
  }

  @Test
  fun `implicit multiplication factorial and combinatorics`() {
    val implicit = evaluator.evaluate("2(3 + 4) + 5!") as EvaluationResult.Success
    assertEquals("134", implicit.formatted)

    val comb = evaluator.evaluate("6 nCr 2") as EvaluationResult.Success
    assertEquals("15", comb.formatted)

    val perm = evaluator.evaluate("5 nPr 3") as EvaluationResult.Success
    assertEquals("60", perm.formatted)
  }

  @Test
  fun `domain errors are caught gracefully`() {
    val divZero = evaluator.evaluate("5 ÷ 0")
    assertTrue(divZero is EvaluationResult.Error)

    val tan90 = evaluator.evaluate("tan(90)", AngleMode.DEG)
    assertTrue(tan90 is EvaluationResult.Error)
  }

  @Test
  fun `unit converter temperature and length`() {
    val tempUnits = UnitConverterEngine.unitsByCategory.getValue(UnitCategory.TEMPERATURE)
    val c = tempUnits[0]
    val f = tempUnits[1]
    assertEquals(212.0, UnitConverterEngine.convert(100.0, UnitCategory.TEMPERATURE, c, f), 1e-6)
  }
}
