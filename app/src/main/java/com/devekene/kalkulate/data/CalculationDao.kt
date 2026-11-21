package com.devekene.kalkulate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
  @Query("SELECT * FROM calculation_history ORDER BY isPinned DESC, timestamp DESC")
  fun observeHistory(): Flow<List<CalculationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entry: CalculationEntity)

  @Query("UPDATE calculation_history SET isPinned = :pinned WHERE id = :id")
  suspend fun setPinned(id: Long, pinned: Boolean)

  @Query("DELETE FROM calculation_history WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM calculation_history WHERE isPinned = 0")
  suspend fun clearUnpinned()

  @Query("DELETE FROM calculation_history")
  suspend fun clearAll()
}
