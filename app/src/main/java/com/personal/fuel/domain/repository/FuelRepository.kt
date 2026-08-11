package com.personal.fuel.domain.repository

import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FoodTemplate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

/**
 * Single source of truth for logged food and burned calories.
 *
 * The observe* functions back the app UI; the suspending get* functions exist for
 * home-screen widgets, which render a snapshot rather than subscribing.
 */
interface FuelRepository {

    fun observeEntries(date: LocalDate): Flow<List<FoodEntry>>

    fun observeDaySummary(date: LocalDate): Flow<DaySummary>

    fun observeMonthSummaries(month: YearMonth): Flow<Map<LocalDate, DaySummary>>

    fun observeRecentFoods(limit: Int): Flow<List<FoodTemplate>>

    suspend fun getEntries(date: LocalDate): List<FoodEntry>

    suspend fun getDaySummary(date: LocalDate): DaySummary

    suspend fun getRecentFoods(limit: Int): List<FoodTemplate>

    suspend fun addEntry(entry: FoodEntry): Long

    suspend fun updateEntry(entry: FoodEntry)

    suspend fun deleteEntry(id: Long)

    /** A value of zero or less clears the day's burn instead of storing it. */
    suspend fun setBurn(date: LocalDate, burned: Double)
}
