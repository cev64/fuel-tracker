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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.personal.fuel.BuildConfig
import com.personal.fuel.domain.model.AppearanceSettings
import com.personal.fuel.domain.model.ThemeMode
import com.personal.fuel.ui.components.FuelCard
import com.personal.fuel.ui.components.SectionLabel
import com.personal.fuel.ui.theme.FuelTheme

@Composable
fun SettingsScreen(
    settings: AppearanceSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
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
                        onCheckedChange = onDynamicColorChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FuelTheme.colors.onAccent,
                            checkedTrackColor = FuelTheme.colors.accent,
                        ),
                    )
                }
            }
        }

        FuelCard {
            SectionLabel("Widgets")
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Long-press the home screen, choose Widgets, then Fuel. " +
                    "The Quick Log widget adds food without opening the app; the Today " +
                    "widget shows totals and deficit. Both resize.",
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
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) FuelTheme.colors.accent else FuelTheme.colors.surface2)
            .clickable(onClick = onClick)
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
