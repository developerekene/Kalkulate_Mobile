package com.devekene.kalkulate

import com.devekene.kalkulate.data.PeriodicTableData
import com.devekene.kalkulate.math.EvaluationResult
import com.devekene.kalkulate.math.MathExpressionEvaluator
import com.devekene.kalkulate.math.ScienceConstantDiscipline
import com.devekene.kalkulate.math.ScienceConstantsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScienceReferenceTest {

  @Test
  fun `science constants cover all five STEM disciplines and evaluate cleanly`() {
    val evaluator = MathExpressionEvaluator()
    val disciplines = ScienceConstantsRepository.allConstants.map { it.discipline }.toSet()
    assertEquals(ScienceConstantDiscipline.entries.toSet(), disciplines)
    ScienceConstantDiscipline.entries.forEach { discipline ->
      val count = ScienceConstantsRepository.allConstants.count { it.discipline == discipline }
      assertTrue("Expected constants for $discipline", count >= 5)
    }
    ScienceConstantsRepository.allConstants.forEach { constant ->
      val res = evaluator.evaluate(constant.insertToken)
      assertTrue("Constant ${constant.id} failed to evaluate: ${constant.insertToken}", res is EvaluationResult.Success)
    }
  }

  @Test
  fun `periodic table contains all 118 elements with unique grid cells and details`() {
    val elements = PeriodicTableData.elements
    assertEquals(118, elements.size)
    assertEquals((1..118).toList(), elements.map { it.atomicNumber })
    assertEquals(118, elements.map { it.gridRow to it.gridCol }.toSet().size)
    elements.forEach { el ->
      assertTrue("Missing symbol for ${el.atomicNumber}", el.symbol.isNotBlank())
      assertTrue("Missing name for ${el.atomicNumber}", el.name.isNotBlank())
      assertTrue("Missing properties for ${el.symbol}", el.properties.isNotBlank())
      assertTrue("Missing whereFound for ${el.symbol}", el.whereFound.isNotBlank())
    }
  }
}
