package com.personal.fuel.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Dates are stored as ISO `yyyy-MM-dd` strings. They sort and range-compare
 * lexicographically, which keeps month queries simple, and they stay readable
 * when inspecting the database directly.
 */
@Entity(
    tableName = "food_entries",
    indices = [Index("dateKey"), Index("createdAt")],
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateKey: String,
    val name: String,
    val calories: Double,
    val protein: Double,
    val fiber: Double,
    val createdAt: Long,
)

@Entity(tableName = "day_burns")
data class DayBurnEntity(
    @PrimaryKey val dateKey: String,
    @ColumnInfo(name = "burnedCalories") val burnedCalories: Double,
)

/** Projection for the "recently logged foods" query. */
data class FoodTemplateProjection(
    val name: String,
    val calories: Double,
    val protein: Double,
    val fiber: Double,
    val lastUsedAt: Long,
)
