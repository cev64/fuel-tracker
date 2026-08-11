package com.personal.fuel.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.WeekSummary
import com.personal.fuel.ui.CalendarUiState
import com.personal.fuel.ui.components.CircleIconButton
import com.personal.fuel.ui.components.SectionLabel
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelFormat
import com.personal.fuel.utilities.FuelHaptic
import com.personal.fuel.utilities.rememberFuelHaptics
import java.time.LocalDate

/**
 * Month overview.
 *
 * Layouts:
 *  - compact: month grid, legend, then the weekly deficit rows below it.
 *  - expanded: the grid keeps its own column while the weekly rows and the
 *    selected day's detail move into a second pane (see FuelNavigation).
 *  - transition: the visible month and selected day come from the shared
 *    ViewModel, so neither resets when the device folds.
 */
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    selectedDate: LocalDate,
    onShiftMonth: (Long) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showWeekSummaries: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MonthNavigator(state = state, onShiftMonth = onShiftMonth)
        MonthGrid(state = state, selectedDate = selectedDate, onSelectDate = onSelectDate)
        Legend()
        if (showWeekSummaries) {
            WeeklyDeficits(weeks = state.weeks)
        }
    }
}

@Composable
private fun MonthNavigator(
    state: CalendarUiState,
    onShiftMonth: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CircleIconButton(
            icon = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Previous month",
            onClick = { onShiftMonth(-1) },
        )
        Text(
            text = FuelFormat.monthTitle(state.month),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        CircleIconButton(
            icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Next month",
            onClick = { onShiftMonth(1) },
        )
    }
}

@Composable
private fun MonthGrid(
    state: CalendarUiState,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val firstDay = state.month.atDay(1)
    // DayOfWeek is Monday=1..Sunday=7; the grid starts on Sunday.
    val leadingBlanks = firstDay.dayOfWeek.value % 7
    val daysInMonth = state.month.lengthOfMonth()
    val cells = buildList {
        repeat(leadingBlanks) { add(null) }
        for (day in 1..daysInMonth) add(state.month.atDay(day))
        while (size % 7 != 0) add(null)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Square cells across a 7-column grid would otherwise grow enormous
            // on the inner display and in landscape.
            .widthIn(max = 560.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, FuelTheme.colors.border, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = FuelTheme.colors.textTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                week.forEach { date ->
                    if (date == null) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        DayCell(
                            date = date,
                            summary = state.summaries[date],
                            isToday = date == today,
                            isSelected = date == selectedDate,
                            onClick = { onSelectDate(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    summary: DaySummary?,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberFuelHaptics()
    val dayData = summary?.takeIf { it.hasEntries }
    val hasData = dayData != null
    val borderColor = when {
        isToday || isSelected -> FuelTheme.colors.accentOnSurface
        hasData -> FuelTheme.colors.border
        else -> Color.Transparent
    }
    val background = when {
        isToday -> FuelTheme.colors.accentDim
        hasData -> FuelTheme.colors.surface2
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable {
                haptics.perform(FuelHaptic.Light)
                onClick()
            }
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.sp),
            color = if (isToday) FuelTheme.colors.accentOnSurface else FuelTheme.colors.textSecondary,
        )
        if (dayData != null) {
            Text(
                text = FuelFormat.number(dayData.calories),
                style = MaterialTheme.typography.labelSmall,
                color = FuelTheme.colors.calories.copy(alpha = 0.85f),
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (dayData.calories > 0) Dot(FuelTheme.colors.calories)
                if (dayData.protein > 0) Dot(FuelTheme.colors.protein)
                if (dayData.fiber > 0) Dot(FuelTheme.colors.fiber)
            }
        }
    }
}

@Composable
private fun Dot(color: Color, size: Dp = 4.dp) {
    Box(modifier = Modifier.size(size).clip(CircleShape).background(color))
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
    ) {
        LegendItem("Calories", FuelTheme.colors.calories)
        LegendItem("Protein", FuelTheme.colors.protein)
        LegendItem("Fiber", FuelTheme.colors.fiber)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Dot(color = color, size = 6.dp)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = FuelTheme.colors.textSecondary,
        )
    }
}

@Composable
fun WeeklyDeficits(weeks: List<WeekSummary>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel("Weekly deficit", color = FuelTheme.colors.textTertiary)
        Spacer(Modifier.height(2.dp))
        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, FuelTheme.colors.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = FuelFormat.weekRange(week.start, week.endInclusive),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FuelTheme.colors.textSecondary,
                )
                val deficit = week.deficit
                Text(
                    text = if (deficit == null) "—" else "${FuelFormat.signedNumber(deficit)} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        deficit == null -> FuelTheme.colors.textTertiary
                        deficit >= 0 -> FuelTheme.colors.accentOnSurface
                        else -> FuelTheme.colors.calories
                    },
                )
            }
        }
    }
}
