package com.personal.fuel

import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.usecase.BuildWeekSummaries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class BuildWeekSummariesTest {

    private fun day(date: String, calories: Double, burned: Double = 0.0) =
        LocalDate.parse(date) to DaySummary(
            date = LocalDate.parse(date),
            calories = calories,
            burned = burned,
            entryCount = 1,
        )

    @Test
    fun `august 2026 splits into the rows of the calendar grid`() {
        // 1 Aug 2026 is a Saturday, so the first grid row holds only that day.
        val weeks = BuildWeekSummaries(YearMonth.of(2026, 8), emptyMap())

        assertEquals(6, weeks.size)
        assertEquals(LocalDate.of(2026, 8, 1), weeks[0].start)
        assertEquals(LocalDate.of(2026, 8, 1), weeks[0].endInclusive)
        assertEquals(LocalDate.of(2026, 8, 2), weeks[1].start)
        assertEquals(LocalDate.of(2026, 8, 8), weeks[1].endInclusive)
        assertEquals(LocalDate.of(2026, 8, 30), weeks.last().start)
        assertEquals(LocalDate.of(2026, 8, 31), weeks.last().endInclusive)
    }

    @Test
    fun `a month starting on sunday has no split first row`() {
        // 1 Feb 2026 is a Sunday.
        val weeks = BuildWeekSummaries(YearMonth.of(2026, 2), emptyMap())

        assertEquals(LocalDate.of(2026, 2, 1), weeks[0].start)
        assertEquals(LocalDate.of(2026, 2, 7), weeks[0].endInclusive)
    }

    @Test
    fun `weeks with no recorded burn report no deficit`() {
        val summaries = mapOf(day("2026-08-03", calories = 2000.0))

        val week = BuildWeekSummaries(YearMonth.of(2026, 8), summaries)[1]

        assertNull(week.deficit)
        assertEquals(0, week.trackedDays)
    }

    @Test
    fun `deficit sums only the days that have a burn`() {
        val summaries = mapOf(
            day("2026-08-03", calories = 2000.0, burned = 2500.0),  // +500
            day("2026-08-04", calories = 2200.0, burned = 2000.0),  // -200
            day("2026-08-05", calories = 1800.0),                   // untracked
        )

        val week = BuildWeekSummaries(YearMonth.of(2026, 8), summaries)[1]

        assertEquals(300.0, week.deficit!!, 0.001)
        assertEquals(2, week.trackedDays)
    }

    @Test
    fun `days outside the month never leak into a row`() {
        val summaries = mapOf(
            day("2026-07-31", calories = 5000.0, burned = 6000.0),
            day("2026-08-01", calories = 1000.0, burned = 1500.0),
        )

        val firstRow = BuildWeekSummaries(YearMonth.of(2026, 8), summaries).first()

        assertEquals(500.0, firstRow.deficit!!, 0.001)
        assertEquals(1, firstRow.trackedDays)
    }
}
