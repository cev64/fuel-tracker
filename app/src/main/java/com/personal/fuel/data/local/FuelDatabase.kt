package com.personal.fuel.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FoodEntryEntity::class, DayBurnEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class FuelDatabase : RoomDatabase() {

    abstract fun fuelDao(): FuelDao

    companion object {
        private const val NAME = "fuel.db"

        fun create(context: Context): FuelDatabase =
            Room.databaseBuilder(context.applicationContext, FuelDatabase::class.java, NAME)
                .build()
    }
}
