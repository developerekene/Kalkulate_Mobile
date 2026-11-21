package com.devekene.kalkulate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devekene.kalkulate.math.AngleMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.kalkulateDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "kalkulate_prefs")

data class UserPreferences(
  val angleMode: AngleMode = AngleMode.DEG,
  val isScientificMode: Boolean = false,
  val decimalPrecision: Int = 10,
  val hapticEnabled: Boolean = true,
  val memoryValue: Double = 0.0,
)

class UserPreferencesRepository(private val context: Context) {

  private object Keys {
    val ANGLE_MODE = stringPreferencesKey("angle_mode")
    val SCIENTIFIC_MODE = booleanPreferencesKey("scientific_mode")
    val DECIMAL_PRECISION = intPreferencesKey("decimal_precision")
    val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    val MEMORY_VALUE = doublePreferencesKey("memory_value")
  }

  val preferencesStream: Flow<UserPreferences> =
    context.kalkulateDataStore.data.map { prefs ->
      val angle =
        prefs[Keys.ANGLE_MODE]?.let { name -> AngleMode.entries.find { it.name == name } }
          ?: AngleMode.DEG
      UserPreferences(
        angleMode = angle,
        isScientificMode = prefs[Keys.SCIENTIFIC_MODE] ?: false,
        decimalPrecision = (prefs[Keys.DECIMAL_PRECISION] ?: 10).coerceIn(2, 12),
        hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
        memoryValue = prefs[Keys.MEMORY_VALUE] ?: 0.0,
      )
    }

  suspend fun setAngleMode(mode: AngleMode) {
    context.kalkulateDataStore.edit { it[Keys.ANGLE_MODE] = mode.name }
  }

  suspend fun setScientificMode(enabled: Boolean) {
    context.kalkulateDataStore.edit { it[Keys.SCIENTIFIC_MODE] = enabled }
  }

  suspend fun setDecimalPrecision(precision: Int) {
    context.kalkulateDataStore.edit { it[Keys.DECIMAL_PRECISION] = precision.coerceIn(2, 12) }
  }

  suspend fun setHapticEnabled(enabled: Boolean) {
    context.kalkulateDataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
  }

  suspend fun setMemoryValue(value: Double) {
    context.kalkulateDataStore.edit { it[Keys.MEMORY_VALUE] = value }
  }
}
