package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CalculationEntity::class], version = 1, exportSchema = false)
abstract class KalkulateDatabase : RoomDatabase() {
  abstract fun calculationDao(): CalculationDao

  companion object {
    @Volatile private var INSTANCE: KalkulateDatabase? = null

    fun getInstance(context: Context): KalkulateDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                KalkulateDatabase::class.java,
                "kalkulate_database",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
