package com.personal.fuel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.fuel.FuelApp
import com.personal.fuel.data.prefs.SettingsRepository
import com.personal.fuel.domain.model.AppearanceSettings
import com.personal.fuel.domain.model.DailyGoals
import com.personal.fuel.domain.model.ThemeMode
import com.personal.fuel.domain.model.WidgetBackground
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppearanceSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppearanceSettings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    val goals: StateFlow<DailyGoals> = settingsRepository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyGoals())

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    fun setWidgetBackground(background: WidgetBackground) {
        viewModelScope.launch { settingsRepository.setWidgetBackground(background) }
    }

    fun setGoals(goals: DailyGoals) {
        viewModelScope.launch { settingsRepository.setGoals(goals) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FuelApp
                SettingsViewModel(app.container.settingsRepository)
            }
        }
    }
}
