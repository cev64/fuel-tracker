package com.personal.fuel.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.personal.fuel.domain.model.FoodTemplate
import com.personal.fuel.ui.components.FieldLabel
import com.personal.fuel.ui.components.FuelButton
import com.personal.fuel.ui.components.FuelCard
import com.personal.fuel.ui.components.FuelTextField
import com.personal.fuel.ui.components.MacroFieldRow
import com.personal.fuel.ui.components.SectionLabel
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelFormat
import java.time.LocalDate

/**
 * The Log screen: a single "add food" form that always writes to today, plus
 * one-tap re-logging of recently eaten foods.
 *
 * Layouts:
 *  - compact (cover screen): the form fills the width, quick-add wraps below.
 *  - expanded (unfolded): this screen is the left pane and the live day log sits
 *    beside it, so the totals update as items are added.
 */
@Composable
fun LogScreen(
    recentFoods: List<FoodTemplate>,
    onAdd: (name: String, calories: Double, protein: Double, fiber: Double) -> Boolean,
    onQuickAdd: (FoodTemplate) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FuelCard {
            SectionLabel("Add food")
            Spacer(Modifier.height(16.dp))
            FieldLabel("Food item")
            Spacer(Modifier.height(6.dp))
            FuelTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. Chicken breast",
                imeAction = ImeAction.Next,
                contentDescription = "Food item",
            )
            Spacer(Modifier.height(12.dp))
            MacroFieldRow(
                calories = calories,
                protein = protein,
                fiber = fiber,
                onCaloriesChange = { calories = it },
                onProteinChange = { protein = it },
                onFiberChange = { fiber = it },
                onSubmit = submit,
            )
            Spacer(Modifier.height(16.dp))
            FuelButton(text = "Add to log", onClick = submit)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Goes to ${FuelFormat.dayTitle(LocalDate.now())} · " +
                    FuelFormat.daySubtitle(LocalDate.now()),
                style = MaterialTheme.typography.bodySmall,
                color = FuelTheme.colors.textTertiary,
            )
        }

        if (recentFoods.isNotEmpty()) {
            FuelCard(contentPadding = PaddingValues(16.dp)) {
                SectionLabel("Quick add")
                Spacer(Modifier.height(12.dp))
                QuickAddChips(recentFoods = recentFoods, onQuickAdd = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onQuickAdd(it)
                })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddChips(
    recentFoods: List<FoodTemplate>,
    onQuickAdd: (FoodTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        recentFoods.forEach { template ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(FuelTheme.colors.surface2)
                    .border(1.dp, FuelTheme.colors.border, RoundedCornerShape(10.dp))
                    .clickable { onQuickAdd(template) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = FuelFormat.number(template.calories),
                    style = MaterialTheme.typography.bodySmall,
                    color = FuelTheme.colors.calories,
                )
            }
        }
    }
}
