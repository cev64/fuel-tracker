package com.personal.fuel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.fuel.FuelApp
import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FoodTemplate
import com.personal.fuel.domain.model.WeekSummary
import com.personal.fuel.domain.repository.FuelRepository
import com.personal.fuel.domain.usecase.BuildWeekSummaries
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DayUiState(
    val date: LocalDate = LocalDate.now(),
    val entries: List<FoodEntry> = emptyList(),
    val summary: DaySummary = DaySummary.empty(LocalDate.now()),
    val editingEntryId: Long? = null,
)

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val summaries: Map<LocalDate, DaySummary> = emptyMap(),
    val weeks: List<WeekSummary> = emptyList(),
)

/**
 * Holds the state shared by every screen: which day is being viewed, which month
 * the calendar is showing, and which row is being edited.
 *
 * Keeping this in one activity-scoped ViewModel is what makes folding and
 * unfolding seamless — the layout changes around the state instead of resetting
 * it, and the same day stays selected in both the single-pane and two-pane
 * layouts.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FuelViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _calendarMonth = MutableStateFlow(YearMonth.now())
    private val _editingEntryId = MutableStateFlow<Long?>(null)

    private val _messages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Transient confirmations, shown as the accent pill toast. */
    val messages: SharedFlow<String> = _messages

    private data class DayData(
        val date: LocalDate,
        val entries: List<FoodEntry>,
        val summary: DaySummary,
    )

    private val dayData = _selectedDate.flatMapLatest { date ->
        combine(
            repository.observeEntries(date),
            repository.observeDaySummary(date),
        ) { entries, summary -> DayData(date, entries, summary) }
    }

    val dayState: StateFlow<DayUiState> = combine(dayData, _editingEntryId) { data, editingId ->
        DayUiState(
            date = data.date,
            entries = data.entries,
            summary = data.summary,
            // Drop a stale edit target if that row disappeared underneath us.
            editingEntryId = editingId?.takeIf { id -> data.entries.any { it.id == id } },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DayUiState())

    val calendarState: StateFlow<CalendarUiState> = _calendarMonth
        .flatMapLatest { month ->
            repository.observeMonthSummaries(month).map { summaries ->
                CalendarUiState(
                    month = month,
                    summaries = summaries,
                    weeks = BuildWeekSummaries(month, summaries),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CalendarUiState())

    val recentFoods: StateFlow<List<FoodTemplate>> = repository.observeRecentFoods(RECENT_LIMIT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun selectDate(date: LocalDate) {
        _editingEntryId.value = null
        _selectedDate.value = date
        _calendarMonth.value = YearMonth.from(date)
    }

    fun shiftDay(days: Long) {
        selectDate(_selectedDate.value.plusDays(days))
    }

    fun shiftMonth(months: Long) {
        _calendarMonth.value = _calendarMonth.value.plusMonths(months)
    }

    fun startEditing(entryId: Long) {
        _editingEntryId.value = entryId
    }

    fun stopEditing() {
        _editingEntryId.value = null
    }

    /**
     * Logs a food item. Returns false without writing anything when the name is
     * blank, which is what the submit buttons use to decide whether to clear
     * their draft.
     */
    fun addEntry(
        name: String,
        calories: Double,
        protein: Double,
        fiber: Double,
        date: LocalDate = _selectedDate.value,
    ): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            emitMessage("Enter a food name")
            return false
        }
        viewModelScope.launch {
            repository.addEntry(
                FoodEntry(
                    date = date,
                    name = trimmed,
                    calories = calories,
                    protein = protein,
                    fiber = fiber,
                )
            )
            emitMessage("Added!")
        }
        return true
    }

    fun logTemplate(template: FoodTemplate, date: LocalDate = _selectedDate.value) {
        addEntry(template.name, template.calories, template.protein, template.fiber, date)
    }

    fun updateEntry(entry: FoodEntry, name: String, calories: Double, protein: Double, fiber: Double) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            emitMessage("Enter a food name")
            return
        }
        viewModelScope.launch {
            repository.updateEntry(
                entry.copy(name = trimmed, calories = calories, protein = protein, fiber = fiber)
            )
            _editingEntryId.value = null
            emitMessage("Updated!")
        }
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry.id)
            if (_editingEntryId.value == entry.id) _editingEntryId.value = null
            emitMessage("Deleted")
        }
    }

    fun setBurn(value: Double, date: LocalDate = _selectedDate.value) {
        viewModelScope.launch { repository.setBurn(date, value) }
    }

    private fun emitMessage(message: String) {
        _messages.tryEmit(message)
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val RECENT_LIMIT = 8

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FuelApp
                FuelViewModel(app.container.repository)
            }
        }
    }
}
