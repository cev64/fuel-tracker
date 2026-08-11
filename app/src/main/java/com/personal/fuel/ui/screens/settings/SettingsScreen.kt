package com.personal.fuel.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.personal.fuel.BuildConfig
import com.personal.fuel.domain.model.AppearanceSettings
import com.personal.fuel.domain.model.DailyGoals
import com.personal.fuel.domain.model.ThemeMode
import com.personal.fuel.domain.model.WidgetBackground
import com.personal.fuel.ui.components.FuelButton
import com.personal.fuel.ui.components.FuelCard
import com.personal.fuel.ui.components.MacroFieldRow
import com.personal.fuel.ui.components.SectionLabel
import com.personal.fuel.utilities.FuelFormat
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelHaptic
import com.personal.fuel.utilities.rememberFuelHaptics

@Composable
fun SettingsScreen(
    settings: AppearanceSettings,
    goals: DailyGoals,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onWidgetBackgroundChange: (WidgetBackground) -> Unit,
    onGoalsChange: (DailyGoals) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptics = rememberFuelHaptics()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FuelCard {
            SectionLabel("Appearance")
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    ThemeChip(
                        label = mode.label(),
                        selected = settings.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wallpaper colours",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Use Android dynamic colour. Macro colours stay branded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FuelTheme.colors.textSecondary,
                        )
                    }
                    Switch(
                        checked = settings.dynamicColor,
                        onCheckedChange = {
                            haptics.perform(FuelHaptic.Press)
                            onDynamicColorChange(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FuelTheme.colors.onAccent,
                            checkedTrackColor = FuelTheme.colors.accent,
                        ),
                    )
                }
            }
        }

        DailyGoalsCard(goals = goals, onGoalsChange = onGoalsChange)

        FuelCard {
            SectionLabel("Widget style")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Applies to every Fuel widget.",
                style = MaterialTheme.typography.bodySmall,
                color = FuelTheme.colors.textSecondary,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                WidgetBackground.entries.forEach { option ->
                    ThemeChip(
                        label = option.label(),
                        selected = settings.widgetBackground == option,
                        onClick = { onWidgetBackgroundChange(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Transparent keeps a light scrim so the figures stay readable " +
                    "over a bright wallpaper.",
                style = MaterialTheme.typography.bodySmall,
                color = FuelTheme.colors.textTertiary,
            )
        }

        FuelCard {
            SectionLabel("Widgets")
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Long-press the home screen, choose Widgets, then Fuel. " +
                    "Rings shows progress against the goals above; Quick Log adds food " +
                    "without opening the app; Macros is the compact row; Today shows " +
                    "totals and deficit. All of them resize.",
                style = MaterialTheme.typography.bodyMedium,
                color = FuelTheme.colors.textSecondary,
            )
        }

        FuelCard {
            SectionLabel("About")
            Spacer(Modifier.height(12.dp))
            AboutRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            AboutRow("Package", BuildConfig.APPLICATION_ID)
            AboutRow("Storage", "On-device only")
        }
    }
}

/**
 * Daily targets the ring widget fills against. Committed with the button rather
 * than on every keystroke, so a half-typed number never reaches the widgets.
 */
@Composable
private fun DailyGoalsCard(
    goals: DailyGoals,
    onGoalsChange: (DailyGoals) -> Unit,
    modifier: Modifier = Modifier,
) {
    var calories by remember(goals) { mutableStateOf(FuelFormat.editableNumber(goals.calories)) }
    var protein by remember(goals) { mutableStateOf(FuelFormat.editableNumber(goals.protein)) }
    var fiber by remember(goals) { mutableStateOf(FuelFormat.editableNumber(goals.fiber)) }
    val focusManager = LocalFocusManager.current

    val save = {
        onGoalsChange(
            DailyGoals(
                calories = calories.toDoubleOrNull() ?: 0.0,
                protein = protein.toDoubleOrNull() ?: 0.0,
                fiber = fiber.toDoubleOrNull() ?: 0.0,
            )
        )
        focusManager.clearFocus()
    }

    FuelCard(modifier = modifier) {
        SectionLabel("Daily goals")
        Spacer(Modifier.height(6.dp))
        Text(
            text = "What the widget rings fill against. Set one to 0 to leave it untracked.",
            style = MaterialTheme.typography.bodySmall,
            color = FuelTheme.colors.textSecondary,
        )
        Spacer(Modifier.height(14.dp))
        MacroFieldRow(
            calories = calories,
            protein = protein,
            fiber = fiber,
            onCaloriesChange = { calories = it },
            onProteinChange = { protein = it },
            onFiberChange = { fiber = it },
            onSubmit = save,
        )
        Spacer(Modifier.height(14.dp))
        FuelButton(text = "Save goals", onClick = save)
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = FuelTheme.colors.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberFuelHaptics()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) FuelTheme.colors.accent else FuelTheme.colors.surface2)
            .clickable {
                haptics.perform(FuelHaptic.Press)
                onClick()
            }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) FuelTheme.colors.onAccent else FuelTheme.colors.textSecondary,
        )
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

private fun WidgetBackground.label(): String = when (this) {
    WidgetBackground.SOLID -> "Solid"
    WidgetBackground.TRANSPARENT -> "Transparent"
}
