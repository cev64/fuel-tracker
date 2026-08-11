package com.personal.fuel.domain.model

import java.time.LocalDate

/**
 * One calendar-grid row of a month: the days that share a row in the month view,
 * with their combined deficit.
 *
 * [deficit] is null when no day in the range has a burn recorded, matching the
 * "—" shown by the original web app.
 */
data class WeekSummary(
    val start: LocalDate,
    val endInclusive: LocalDate,
    val deficit: Double?,
    val trackedDays: Int,
)
