package com.personal.fuel.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.ui.DayUiState
import com.personal.fuel.ui.components.BurnTile
import com.personal.fuel.ui.components.CircleIconButton
import com.personal.fuel.ui.components.DeficitTile
import com.personal.fuel.ui.components.EmptyState
import com.personal.fuel.ui.components.FoodItemRow
import com.personal.fuel.ui.components.FuelButton
import com.personal.fuel.ui.components.FuelCard
import com.personal.fuel.ui.components.FuelTextField
import com.personal.fuel.ui.components.MacroFieldRow
import com.personal.fuel.ui.components.SectionLabel
import com.personal.fuel.ui.components.TotalsStrip
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelFormat

/**
 * The day view: totals, burn/deficit, the logged items, and an add form for the
 * day currently being viewed.
 *
 * Layouts:
 *  - compact: one scrolling column, exactly like the web app.
 *  - expanded: [TodaySummaryPane] and [TodayLogPane] sit side by side so the
 *    totals stay visible while scrolling a long list of items.
 *  - transition: the day being viewed lives in the shared ViewModel, so folding
 *    or unfolding rearranges the panes without changing the selected day.
 */
@Composable
fun TodayScreen(
    state: DayUiState,
    onShiftDay: (Long) -> Unit,
    onSetBurn: (Double) -> Unit,
    onAdd: (name: String, calories: Double, protein: Double, fiber: Double) -> Boolean,
    onStartEdit: (Long) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (FoodEntry, String, Double, Double, Double) -> Unit,
    onDelete: (FoodEntry) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TodaySummaryPane(state = state, onShiftDay = onShiftDay, onSetBurn = onSetBurn)
        TodayLogPane(
            state = state,
            onAdd = onAdd,
            onStartEdit = onStartEdit,
            onCancelEdit = onCancelEdit,
            onSaveEdit = onSaveEdit,
            onDelete = onDelete,
        )
    }
}

/** Day navigation, macro totals, burn and deficit. */
@Composable
fun TodaySummaryPane(
    state: DayUiState,
    onShiftDay: (Long) -> Unit,
    onSetBurn: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DayNavigator(state = state, onShiftDay = onShiftDay)
        TotalsStrip(
            calories = state.summary.calories,
            protein = state.summary.protein,
            fiber = state.summary.fiber,
        )
        BurnAndDeficitRow(state = state, onSetBurn = onSetBurn)
    }
}

/** The logged items plus the add-to-this-day form. */
@Composable
fun TodayLogPane(
    state: DayUiState,
    onAdd: (name: String, calories: Double, protein: Double, fiber: Double) -> Boolean,
    onStartEdit: (Long) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (FoodEntry, String, Double, Double, Double) -> Unit,
    onDelete: (FoodEntry) -> Unit,
    modifier: Modifier = Modifier,
    showAddForm: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ItemsLoggedCard(
            state = state,
            onStartEdit = onStartEdit,
            onCancelEdit = onCancelEdit,
            onSaveEdit = onSaveEdit,
            onDelete = onDelete,
        )
        if (showAddForm) {
            AddToDayCard(dayLabel = FuelFormat.dayTitle(state.date), onAdd = onAdd)
        }
    }
}

@Composable
private fun DayNavigator(
    state: DayUiState,
    onShiftDay: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CircleIconButton(
            icon = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Previous day",
            onClick = { onShiftDay(-1) },
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = FuelFormat.dayTitle(state.date),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = FuelFormat.daySubtitle(state.date),
                style = MaterialTheme.typography.bodyMedium,
                color = FuelTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        CircleIconButton(
            icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Next day",
            onClick = { onShiftDay(1) },
        )
    }
}

@Composable
private fun BurnAndDeficitRow(
    state: DayUiState,
    onSetBurn: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Free text while the field is focused; the stored value is only written on
    // commit so a half-typed number never reaches the database or the widgets.
    var burnText by remember(state.date, state.summary.burned) {
        mutableStateOf(FuelFormat.editableNumber(state.summary.burned))
    }
    val focusManager = LocalFocusManager.current

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BurnTile(
            value = burnText,
            onValueChange = { burnText = it },
            onCommit = {
                onSetBurn(burnText.toDoubleOrNull() ?: 0.0)
                focusManager.clearFocus()
            },
            modifier = Modifier.weight(1f),
        )
        DeficitTile(deficit = state.summary.deficit, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ItemsLoggedCard(
    state: DayUiState,
    onStartEdit: (Long) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (FoodEntry, String, Double, Double, Double) -> Unit,
    onDelete: (FoodEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, FuelTheme.colors.border, RoundedCornerShape(16.dp)),
    ) {
        SectionLabel(
            text = "Items logged",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
        HorizontalDivider(thickness = 1.dp, color = FuelTheme.colors.border)

        if (state.entries.isEmpty()) {
            EmptyState(icon = "🍽", message = "Nothing logged yet")
        } else {
            state.entries.forEachIndexed { index, entry ->
                FoodItemRow(
                    entry = entry,
                    isEditing = state.editingEntryId == entry.id,
                    onEdit = { onStartEdit(entry.id) },
                    onCancelEdit = onCancelEdit,
                    onSave = { name, calories, protein, fiber ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveEdit(entry, name, calories, protein, fiber)
                    },
                    onDelete = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete(entry)
                    },
                )
                if (index != state.entries.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = FuelTheme.colors.border)
                }
            }
        }
    }
}

@Composable
private fun AddToDayCard(
    dayLabel: String,
    onAdd: (name: String, calories: Double, protein: Double, fiber: Double) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var fiber by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current

    val submit = {
        val added = onAdd(
            name,
            calories.toDoubleOrNull() ?: 0.0,
            protein.toDoubleOrNull() ?: 0.0,
            fiber.toDoubleOrNull() ?: 0.0,
        )
        if (added) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            name = ""
            calories = ""
            protein = ""
            fiber = ""
            focusManager.clearFocus()
        }
    }

    FuelCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        SectionLabel("Add item")
        Spacer(Modifier.height(12.dp))
        FuelTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Food item",
            imeAction = ImeAction.Next,
            contentDescription = "Food item",
        )
        Spacer(Modifier.height(10.dp))
        MacroFieldRow(
            calories = calories,
            protein = protein,
            fiber = fiber,
            onCaloriesChange = { calories = it },
            onProteinChange = { protein = it },
            onFiberChange = { fiber = it },
            onSubmit = submit,
        )
        Spacer(Modifier.height(12.dp))
        FuelButton(text = "Add to $dayLabel", onClick = submit)
    }
}
