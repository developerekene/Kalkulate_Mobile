package com.devekene.kalkulate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val expression: String,
  val result: String,
  val numericValue: Double,
  val angleMode: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isPinned: Boolean = false,
)
