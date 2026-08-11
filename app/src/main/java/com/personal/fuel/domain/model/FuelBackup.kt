package com.personal.fuel.domain.model

import java.time.LocalDate

/** Everything Fuel stores, in a form that can be written to a file. */
data class FuelBackup(
    val entries: List<FoodEntry>,
    val burns: Map<LocalDate, Double>,
    val goals: DailyGoals?,
    val exportedAt: String? = null,
) {
    val dayCount: Int get() = (entries.map { it.date } + burns.keys).distinct().size
}
