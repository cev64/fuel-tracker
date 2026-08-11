package com.personal.fuel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelFormat

/**
 * One logged item. Tapping edit swaps the row for an inline editor, mirroring
 * the behaviour of the web app rather than opening a separate screen.
 */
@Composable
fun FoodItemRow(
    entry: FoodEntry,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: (name: String, calories: Double, protein: Double, fiber: Double) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isEditing) {
        EditingRow(entry = entry, onCancel = onCancelEdit, onSave = onSave, modifier = modifier)
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroChip("${FuelFormat.number(entry.calories)} kcal", FuelTheme.colors.calories)
                    MacroChip("${FuelFormat.number(entry.protein)}g protein", FuelTheme.colors.protein)
                    MacroChip("${FuelFormat.number(entry.fiber)}g fiber", FuelTheme.colors.fiber)
                }
            }
            CircleIconButton(
                icon = Icons.Outlined.Edit,
                contentDescription = "Edit ${entry.name}",
                onClick = onEdit,
                size = 32.dp,
            )
            CircleIconButton(
                icon = Icons.Outlined.Close,
                contentDescription = "Delete ${entry.name}",
                onClick = onDelete,
                size = 32.dp,
            )
        }
    }
}

@Composable
private fun MacroChip(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color.copy(alpha = 0.85f),
    )
}

@Composable
private fun EditingRow(
    entry: FoodEntry,
    onCancel: () -> Unit,
    onSave: (String, Double, Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberEntryState(entry.id) { entry.name }
    var calories by rememberEntryState(entry.id) { FuelFormat.editableNumber(entry.calories) }
    var protein by rememberEntryState(entry.id) { FuelFormat.editableNumber(entry.protein) }
    var fiber by rememberEntryState(entry.id) { FuelFormat.editableNumber(entry.fiber) }

    val save = {
        if (name.isNotBlank()) {
            onSave(
                name.trim(),
                calories.toDoubleOrNull() ?: 0.0,
                protein.toDoubleOrNull() ?: 0.0,
                fiber.toDoubleOrNull() ?: 0.0,
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FuelTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Food item",
            imeAction = ImeAction.Next,
            contentDescription = "Food name",
        )
        MacroFieldRow(
            calories = calories,
            protein = protein,
            fiber = fiber,
            onCaloriesChange = { calories = it },
            onProteinChange = { protein = it },
            onFiberChange = { fiber = it },
            onSubmit = save,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
        ) {
            FuelTextButton(text = "Cancel", onClick = onCancel)
            FuelButton(
                text = "Save",
                onClick = save,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

/** Editor state keyed by entry id so switching rows starts from fresh values. */
@Composable
private fun rememberEntryState(id: Long, initial: () -> String): MutableState<String> =
    remember(id) { mutableStateOf(initial()) }
