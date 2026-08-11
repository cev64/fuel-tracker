package com.personal.fuel.ui.quicklog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.fuel.appContainer
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FoodTemplate
import com.personal.fuel.ui.SettingsViewModel
import com.personal.fuel.ui.components.FieldLabel
import com.personal.fuel.ui.components.FuelButton
import com.personal.fuel.ui.components.FuelCard
import com.personal.fuel.ui.components.FuelTextButton
import com.personal.fuel.ui.components.FuelTextField
import com.personal.fuel.ui.components.MacroFieldRow
import com.personal.fuel.ui.components.SectionLabel
import com.personal.fuel.ui.components.Wordmark
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelHaptic
import com.personal.fuel.utilities.FuelVibration
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * A one-card "add food" sheet launched from the home-screen widget.
 *
 * It floats over whatever is behind it and closes as soon as the item is saved,
 * so logging never requires leaving the home screen.
 */
class QuickLogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = appContainer

        // A widget button cannot vibrate from the launcher's process, so the
        // press it represents is felt here instead, before the first frame.
        if (intent?.getBooleanExtra(EXTRA_HAPTIC, false) == true) {
            FuelVibration.perform(this, FuelHaptic.Press)
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            FuelTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
                QuickLogSheet(
                    onSave = { entry ->
                        // Deliberately on the application scope: this Activity
                        // finishes immediately and must not cancel the write.
                        container.applicationScope.launch {
                            container.repository.addEntry(entry)
                        }
                        finish()
                    },
                    onDismiss = ::finish,
                    loadRecents = { container.repository.getRecentFoods(4) },
                )
            }
        }
    }

    companion object {
        /** Set by widget buttons so the tap is felt as well as seen. */
        const val EXTRA_HAPTIC = "com.personal.fuel.extra.HAPTIC"
    }
}

@Composable
private fun QuickLogSheet(
    onSave: (FoodEntry) -> Unit,
    onDismiss: () -> Unit,
    loadRecents: suspend () -> List<FoodTemplate>,
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var recents by remember { mutableStateOf<List<FoodTemplate>>(emptyList()) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        recents = loadRecents()
        focusRequester.requestFocus()
    }

    val save = {
        if (name.isNotBlank()) {
            onSave(
                FoodEntry(
                    date = LocalDate.now(),
                    name = name.trim(),
                    calories = calories.toDoubleOrNull() ?: 0.0,
                    protein = protein.toDoubleOrNull() ?: 0.0,
                    fiber = fiber.toDoubleOrNull() ?: 0.0,
                )
            )
        }
    }

    // The activity window is transparent, so the sheet paints its own scrim and
    // dismisses when the area around the card is tapped.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        FuelCard(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SectionLabel("Quick log")
                Wordmark(fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }
            Spacer(Modifier.height(16.dp))
            FieldLabel("Food item")
            Spacer(Modifier.height(6.dp))
            FuelTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. Chicken breast",
                imeAction = ImeAction.Next,
                contentDescription = "Food item",
                modifier = Modifier.focusRequester(focusRequester),
            )
            Spacer(Modifier.height(12.dp))
            MacroFieldRow(
                calories = calories,
                protein = protein,
                fiber = fiber,
                onCaloriesChange = { calories = it },
                onProteinChange = { protein = it },
                onFiberChange = { fiber = it },
                onSubmit = save,
            )

            if (recents.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                FieldLabel("Recent")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recents.forEach { template ->
                        FuelTextButton(
                            text = "${template.name} · ${template.calories.toInt()} kcal",
                            onClick = {
                                name = template.name
                                calories = template.calories.toInt().toString()
                                protein = template.protein.toInt().toString()
                                fiber = template.fiber.toInt().toString()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FuelTextButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                )
                FuelButton(text = "Add to log", onClick = save, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Logs to today",
                style = MaterialTheme.typography.bodySmall,
                color = FuelTheme.colors.textTertiary,
            )
        }
    }
}
