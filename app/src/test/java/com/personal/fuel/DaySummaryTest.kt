package com.personal.fuel

import com.personal.fuel.domain.model.DaySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DaySummaryTest {

    private val date = LocalDate.of(2026, 8, 11)

    @Test
    fun `a day with no burn has no deficit`() {
        val summary = DaySummary(date = date, calories = 1800.0, entryCount = 3)

        assertNull(summary.deficit)
        assertFalse(summary.hasBurn)
        assertTrue(summary.hasEntries)
    }

    @Test
    fun `eating less than burned is a positive deficit`() {
        val summary = DaySummary(date = date, calories = 1800.0, burned = 2400.0, entryCount = 3)

        assertEquals(600.0, summary.deficit!!, 0.001)
    }

    @Test
    fun `eating more than burned is a surplus`() {
        val summary = DaySummary(date = date, calories = 2900.0, burned = 2400.0, entryCount = 5)

        assertEquals(-500.0, summary.deficit!!, 0.001)
    }

    @Test
    fun `an empty day reports nothing logged`() {
        val summary = DaySummary.empty(date)

        assertFalse(summary.hasEntries)
        assertNull(summary.deficit)
        assertEquals(0.0, summary.calories, 0.001)
    }
}
