package com.personal.fuel.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.personal.fuel.domain.model.AppearanceSettings
import com.personal.fuel.domain.model.DailyGoals
import com.personal.fuel.domain.model.ThemeMode
import com.personal.fuel.domain.model.WidgetBackground
import com.personal.fuel.domain.repository.WidgetNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Preferences and daily goals. Small, structureless values that do not warrant a
 * database table.
 *
 * Every write refreshes the widgets, because goals and widget style both change
 * what a widget draws.
 */
class SettingsRepository(
    private val context: Context,
    private val widgetNotifier: WidgetNotifier,
) {

    val settings: Flow<AppearanceSettings> = context.settingsDataStore.data.map { prefs ->
        AppearanceSettings(
            themeMode = ThemeMode.fromStorage(prefs[KEY_THEME_MODE]),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
            widgetBackground = WidgetBackground.fromStorage(prefs[KEY_WIDGET_BACKGROUND]),
        )
    }

    val goals: Flow<DailyGoals> = context.settingsDataStore.data.map { prefs ->
        DailyGoals(
            calories = prefs[KEY_GOAL_CALORIES] ?: DailyGoals.DEFAULT_CALORIES,
            protein = prefs[KEY_GOAL_PROTEIN] ?: DailyGoals.DEFAULT_PROTEIN,
            fiber = prefs[KEY_GOAL_FIBER] ?: DailyGoals.DEFAULT_FIBER,
        )
    }

    /** Snapshot reads for widgets, which render once rather than subscribing. */
    suspend fun currentGoals(): DailyGoals = goals.first()

    suspend fun currentWidgetBackground(): WidgetBackground = settings.first().widgetBackground

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setWidgetBackground(background: WidgetBackground) {
        context.settingsDataStore.edit { it[KEY_WIDGET_BACKGROUND] = background.name }
        widgetNotifier.onDataChanged()
    }

    suspend fun setGoals(goals: DailyGoals) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_GOAL_CALORIES] = goals.calories.coerceAtLeast(0.0)
            prefs[KEY_GOAL_PROTEIN] = goals.protein.coerceAtLeast(0.0)
            prefs[KEY_GOAL_FIBER] = goals.fiber.coerceAtLeast(0.0)
        }
        widgetNotifier.onDataChanged()
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_WIDGET_BACKGROUND = stringPreferencesKey("widget_background")
        val KEY_GOAL_CALORIES = doublePreferencesKey("goal_calories")
        val KEY_GOAL_PROTEIN = doublePreferencesKey("goal_protein")
        val KEY_GOAL_FIBER = doublePreferencesKey("goal_fiber")
    }
}
