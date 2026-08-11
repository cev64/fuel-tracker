package com.personal.fuel.domain.model

import java.time.LocalDate

/**
 * Aggregated totals for one day.
 *
 * [burned] is manually entered by the user; a day with no burn recorded has no
 * meaningful deficit, which is why [deficit] is nullable rather than 0.
 */
data class DaySummary(
    val date: LocalDate,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val fiber: Double = 0.0,
    val burned: Double = 0.0,
    val entryCount: Int = 0,
) {
    val hasEntries: Boolean get() = entryCount > 0

    val hasBurn: Boolean get() = burned > 0.0

    /** Burned minus consumed. Positive means a deficit, negative a surplus. */
    val deficit: Double? get() = if (hasBurn) burned - calories else null

    companion object {
        fun empty(date: LocalDate): DaySummary = DaySummary(date = date)
    }
}
