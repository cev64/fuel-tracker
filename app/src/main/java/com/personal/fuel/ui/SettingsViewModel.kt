package com.personal.fuel.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.fuel.FuelApp
import com.personal.fuel.data.backup.FuelBackupManager
import com.personal.fuel.data.prefs.SettingsRepository
import com.personal.fuel.domain.model.AppearanceSettings
import com.personal.fuel.domain.model.DailyGoals
import com.personal.fuel.domain.model.ThemeMode
import com.personal.fuel.domain.model.WidgetBackground
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: FuelBackupManager,
) : ViewModel() {

    private val _messages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val messages: SharedFlow<String> = _messages

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

    fun exportTo(destination: Uri) {
        viewModelScope.launch {
            backupManager.export(destination)
                .onSuccess { count -> _messages.tryEmit("Exported $count items") }
                .onFailure { error -> _messages.tryEmit(error.message ?: "Export failed") }
        }
    }

    fun restoreFrom(source: Uri) {
        viewModelScope.launch {
            backupManager.restore(source)
                .onSuccess { backup ->
                    _messages.tryEmit("Restored ${backup.entries.size} items across ${backup.dayCount} days")
                }
                .onFailure { error -> _messages.tryEmit(error.message ?: "Restore failed") }
        }
    }

    /** Suggested filename for the export picker. */
    fun defaultBackupFileName(): String =
        "fuel-backup-${LocalDate.now()}.json"

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FuelApp
                SettingsViewModel(app.container.settingsRepository, app.container.backupManager)
            }
        }
    }
}
