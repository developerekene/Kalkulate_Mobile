package com.devekene.kalkulate.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: CalculationDao) {
  val historyStream: Flow<List<CalculationEntity>> = dao.observeHistory()

  suspend fun recordCalculation(
    expression: String,
    result: String,
    numericValue: Double,
    angleMode: String,
  ) {
    dao.insert(
      CalculationEntity(
        expression = expression,
        result = result,
        numericValue = numericValue,
        angleMode = angleMode,
      )
    )
  }

  suspend fun togglePin(entry: CalculationEntity) {
    dao.setPinned(entry.id, !entry.isPinned)
  }

  suspend fun deleteById(id: Long) {
    dao.deleteById(id)
  }

  suspend fun clearAll() {
    dao.clearAll()
  }
}
