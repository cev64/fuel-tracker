package com.personal.fuel

import com.personal.fuel.utilities.FuelFormat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class FuelFormatTest {

    private val today = LocalDate.of(2026, 8, 11)

    @Test
    fun `relative day titles`() {
        assertEquals("Today", FuelFormat.dayTitle(today, today))
        assertEquals("Yesterday", FuelFormat.dayTitle(today.minusDays(1), today))
        assertEquals("Tomorrow", FuelFormat.dayTitle(today.plusDays(1), today))
    }

    @Test
    fun `older days fall back to the weekday name`() {
        val title = FuelFormat.dayTitle(LocalDate.of(2026, 8, 3), today)

        assertEquals(
            LocalDate.of(2026, 8, 3).dayOfWeek
                .getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()),
            title,
        )
    }

    @Test
    fun `numbers round and group`() {
        assertEquals("1,842", FuelFormat.number(1841.6))
        assertEquals("0", FuelFormat.number(0.4))
    }

    @Test
    fun `signed numbers always carry a sign`() {
        assertEquals("+512", FuelFormat.signedNumber(512.0))
        assertEquals("-240", FuelFormat.signedNumber(-240.0))
        assertEquals("+0", FuelFormat.signedNumber(0.0))
    }

    @Test
    fun `editable numbers drop trailing zeros and blank out zero`() {
        assertEquals("", FuelFormat.editableNumber(0.0))
        assertEquals("120", FuelFormat.editableNumber(120.0))
        assertEquals("12.5", FuelFormat.editableNumber(12.5))
    }

    @Test
    fun `week ranges collapse within a month`() {
        assertEquals(
            "Aug 3–9",
            FuelFormat.weekRange(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 9)),
        )
        assertEquals(
            "Aug 1",
            FuelFormat.weekRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1)),
        )
    }

    @Test
    fun `month title includes the year`() {
        assertEquals("August 2026", FuelFormat.monthTitle(YearMonth.of(2026, 8)))
    }
}
