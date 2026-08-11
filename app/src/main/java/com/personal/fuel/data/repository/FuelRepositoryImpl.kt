package com.personal.fuel.data.repository

import com.personal.fuel.data.local.DayBurnEntity
import com.personal.fuel.data.local.FoodEntryEntity
import com.personal.fuel.data.local.FoodTemplateProjection
import com.personal.fuel.data.local.FuelDao
import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FoodTemplate
import com.personal.fuel.domain.repository.FuelRepository
import com.personal.fuel.domain.repository.WidgetNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

class FuelRepositoryImpl(
    private val dao: FuelDao,
    private val widgetNotifier: WidgetNotifier,
) : FuelRepository {

    override fun observeEntries(date: LocalDate): Flow<List<FoodEntry>> =
        dao.observeEntries(date.key()).map { rows -> rows.map { it.toDomain() } }

    override fun observeDaySummary(date: LocalDate): Flow<DaySummary> =
        combine(
            dao.observeEntries(date.key()),
            dao.observeBurn(date.key()),
        ) { entries, burn ->
            summarize(date, entries, burn?.burnedCalories ?: 0.0)
        }

    override fun observeMonthSummaries(month: YearMonth): Flow<Map<LocalDate, DaySummary>> {
        val startKey = month.atDay(1).key()
        val endKey = month.atEndOfMonth().key()
        return combine(
            dao.observeEntriesInRange(startKey, endKey),
            dao.observeBurnsInRange(startKey, endKey),
        ) { entries, burns ->
            val burnsByDate = burns.associate { LocalDate.parse(it.dateKey) to it.burnedCalories }
            val entriesByDate = entries.groupBy { LocalDate.parse(it.dateKey) }
            val dates = entriesByDate.keys + burnsByDate.keys
            dates.associateWith { date ->
                summarize(date, entriesByDate[date].orEmpty(), burnsByDate[date] ?: 0.0)
            }
        }
    }

    override fun observeRecentFoods(limit: Int): Flow<List<FoodTemplate>> =
        dao.observeRecentFoods(limit).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getEntries(date: LocalDate): List<FoodEntry> =
        dao.getEntries(date.key()).map { it.toDomain() }

    override suspend fun getDaySummary(date: LocalDate): DaySummary =
        summarize(
            date = date,
            entries = dao.getEntries(date.key()),
            burned = dao.getBurn(date.key())?.burnedCalories ?: 0.0,
        )

    override suspend fun getRecentFoods(limit: Int): List<FoodTemplate> =
        dao.getRecentFoods(limit).map { it.toDomain() }

    override suspend fun addEntry(entry: FoodEntry): Long {
        val id = dao.insertEntry(entry.toEntity())
        widgetNotifier.onDataChanged()
        return id
    }

    override suspend fun updateEntry(entry: FoodEntry) {
        dao.updateEntry(entry.toEntity())
        widgetNotifier.onDataChanged()
    }

    override suspend fun deleteEntry(id: Long) {
        dao.deleteEntry(id)
        widgetNotifier.onDataChanged()
    }

    override suspend fun setBurn(date: LocalDate, burned: Double) {
        if (burned > 0.0) {
            dao.upsertBurn(DayBurnEntity(date.key(), burned))
        } else {
            dao.deleteBurn(date.key())
        }
        widgetNotifier.onDataChanged()
    }

    private fun summarize(
        date: LocalDate,
        entries: List<FoodEntryEntity>,
        burned: Double,
    ): DaySummary = DaySummary(
        date = date,
        calories = entries.sumOf { it.calories },
        protein = entries.sumOf { it.protein },
        fiber = entries.sumOf { it.fiber },
        burned = burned,
        entryCount = entries.size,
    )
}

private fun LocalDate.key(): String = toString()

private fun FoodEntryEntity.toDomain() = FoodEntry(
    id = id,
    date = LocalDate.parse(dateKey),
    name = name,
    calories = calories,
    protein = protein,
    fiber = fiber,
    createdAt = createdAt,
)

private fun FoodEntry.toEntity() = FoodEntryEntity(
    id = id,
    dateKey = date.toString(),
    name = name,
    calories = calories,
    protein = protein,
    fiber = fiber,
    createdAt = createdAt,
)

private fun FoodTemplateProjection.toDomain() = FoodTemplate(
    name = name,
    calories = calories,
    protein = protein,
    fiber = fiber,
)
