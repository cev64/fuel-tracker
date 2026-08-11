package com.personal.fuel.utilities

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToLong

/** Formatting shared by the screens and the widgets. */
object FuelFormat {

    /**
     * Formatters are rebuilt when the system locale changes rather than being
     * frozen at class-load time.
     */
    private class Formatters(locale: Locale) {
        val monthDayYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", locale)
        val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
        val shortMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", locale)
        val shortMonthDay: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", locale)
    }

    @Volatile
    private var cached: Pair<Locale, Formatters>? = null

    private val formatters: Formatters
        get() {
            val locale = Locale.getDefault()
            cached?.let { (cachedLocale, formatters) ->
                if (cachedLocale == locale) return formatters
            }
            return Formatters(locale).also { cached = locale to it }
        }

    fun dayTitle(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        today.plusDays(1) -> "Tomorrow"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    fun daySubtitle(date: LocalDate): String = date.format(formatters.monthDayYear)

    fun monthTitle(month: YearMonth): String = month.atDay(1).format(formatters.monthYear)

    /** "Aug 3–9" for a range, "Aug 31" for a single day. */
    fun weekRange(start: LocalDate, endInclusive: LocalDate): String = when {
        start == endInclusive -> start.format(formatters.shortMonthDay)
        start.month == endInclusive.month ->
            "${start.format(formatters.shortMonthDay)}–${endInclusive.dayOfMonth}"
        else -> "${start.format(formatters.shortMonthDay)}–${endInclusive.format(formatters.shortMonthDay)}"
    }

    fun monthAbbreviation(month: YearMonth): String = month.atDay(1).format(formatters.shortMonth)

    /** Rounded whole number with locale grouping, e.g. 1,842. */
    fun number(value: Double): String =
        NumberFormat.getIntegerInstance(Locale.getDefault()).format(value.roundToLong())

    /** Same as [number] but always carries a sign, e.g. +512 / -240. */
    fun signedNumber(value: Double): String {
        val rounded = value.roundToLong()
        return if (rounded >= 0) "+${number(rounded.toDouble())}" else number(rounded.toDouble())
    }

    /** Trims a macro value for display in a text field: 120.0 -> "120". */
    fun editableNumber(value: Double): String =
        if (value == 0.0) "" else if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
