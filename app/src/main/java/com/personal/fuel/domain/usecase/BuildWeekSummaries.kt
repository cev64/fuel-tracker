package com.personal.fuel.domain.usecase

import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.WeekSummary
import java.time.LocalDate
import java.time.YearMonth

/**
 * Groups a month into the rows shown by the calendar grid (Sunday-start, split
 * at month boundaries) and totals the deficit for each row.
 *
 * Days without a recorded burn are skipped entirely rather than counted as a
 * zero deficit, so a partially tracked week still reports a useful number.
 */
object BuildWeekSummaries {

    operator fun invoke(
        month: YearMonth,
        summaries: Map<LocalDate, DaySummary>,
    ): List<WeekSummary> {
        val daysInMonth = month.lengthOfMonth()
        // DayOfWeek is Monday=1..Sunday=7; the grid starts on Sunday.
        val leadingBlanks = month.atDay(1).dayOfWeek.value % 7

        val rows = mutableListOf<List<LocalDate>>()
        var current = mutableListOf<LocalDate>()
        repeat(leadingBlanks) { current.add(PLACEHOLDER) }

        for (day in 1..daysInMonth) {
            current.add(month.atDay(day))
            if (current.size == 7) {
                rows.add(current.filterNot { it == PLACEHOLDER })
                current = mutableListOf()
            }
        }
        if (current.isNotEmpty()) rows.add(current.filterNot { it == PLACEHOLDER })

        return rows.filter { it.isNotEmpty() }.map { days ->
            var total = 0.0
            var trackedDays = 0
            for (date in days) {
                val deficit = summaries[date]?.deficit ?: continue
                total += deficit
                trackedDays++
            }
            WeekSummary(
                start = days.first(),
                endInclusive = days.last(),
                deficit = if (trackedDays == 0) null else total,
                trackedDays = trackedDays,
            )
        }
    }

    /** Stands in for the empty leading cells of the first grid row. */
    private val PLACEHOLDER: LocalDate = LocalDate.MIN
}
