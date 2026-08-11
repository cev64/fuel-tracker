package com.personal.fuel.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.personal.fuel.domain.model.AppearanceSettings
import com.personal.fuel.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Lightweight appearance preferences, kept in DataStore rather than Room. */
class SettingsRepository(private val context: Context) {

    val settings: Flow<AppearanceSettings> = context.settingsDataStore.data.map { prefs ->
        AppearanceSettings(
            themeMode = ThemeMode.fromStorage(prefs[KEY_THEME_MODE]),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }
}
