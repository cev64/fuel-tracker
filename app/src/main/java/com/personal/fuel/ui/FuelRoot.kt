package com.personal.fuel.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import com.personal.fuel.ui.components.FuelCard
import com.personal.fuel.ui.components.FuelToastHost
import com.personal.fuel.ui.components.SectionLabel
import com.personal.fuel.ui.components.TotalsStrip
import com.personal.fuel.ui.components.Wordmark
import com.personal.fuel.ui.navigation.FuelDestination
import com.personal.fuel.ui.screens.calendar.CalendarScreen
import com.personal.fuel.ui.screens.calendar.WeeklyDeficits
import com.personal.fuel.ui.screens.log.LogScreen
import com.personal.fuel.ui.screens.settings.SettingsScreen
import com.personal.fuel.ui.screens.today.TodayLogPane
import com.personal.fuel.ui.screens.today.TodayScreen
import com.personal.fuel.ui.screens.today.TodaySummaryPane
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelFormat
import java.time.LocalDate

/** Where a widget tap should land. */
data class DeepLinkTarget(val destination: FuelDestination, val date: LocalDate?)

/**
 * Application shell.
 *
 * Navigation adapts to the available width: a bottom bar on the cover screen, a
 * navigation rail plus two-pane content on the unfolded inner display. The panes
 * are fed from the same [FuelViewModel] state, so folding rearranges the layout
 * without disturbing the selected day, month, or in-progress edit.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FuelRoot(
    viewModel: FuelViewModel,
    settingsViewModel: SettingsViewModel,
    deepLink: DeepLinkTarget?,
    onDeepLinkHandled: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val dayState by viewModel.dayState.collectAsStateWithLifecycle()
    val calendarState by viewModel.calendarState.collectAsStateWithLifecycle()
    val recentFoods by viewModel.recentFoods.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val widthClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass
    val useRail = widthClass != WindowWidthSizeClass.COMPACT
    val twoPane = widthClass == WindowWidthSizeClass.EXPANDED
    val tabletop = adaptiveInfo.windowPosture.isTabletop

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = FuelDestination.fromRoute(backStackEntry?.destination?.route)

    LaunchedEffect(deepLink) {
        val target = deepLink ?: return@LaunchedEffect
        target.date?.let(viewModel::selectDate)
        navController.navigateToTopLevel(target.destination)
        onDeepLinkHandled()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!useRail) {
                FuelBottomBar(
                    current = currentDestination,
                    onSelect = { navController.navigateToTopLevel(it) },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (useRail) {
                    FuelNavRail(
                        current = currentDestination,
                        onSelect = { navController.navigateToTopLevel(it) },
                    )
                }
                Column(modifier = Modifier.fillMaxSize().imePadding()) {
                    if (!useRail) {
                        Wordmark(modifier = Modifier.padding(start = 20.dp, top = 20.dp))
                    }
                    NavHost(
                        navController = navController,
                        startDestination = FuelDestination.Log.route,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        composable(FuelDestination.Log.route) {
                            val logScreen: @Composable (Modifier) -> Unit = { mod ->
                                LogScreen(
                                    recentFoods = recentFoods,
                                    onAdd = { name, calories, protein, fiber ->
                                        // The Log tab always writes to today, whatever
                                        // day the rest of the app is showing.
                                        viewModel.addEntry(
                                            name, calories, protein, fiber, LocalDate.now(),
                                        )
                                    },
                                    onQuickAdd = { viewModel.logTemplate(it, LocalDate.now()) },
                                    modifier = mod,
                                    contentPadding = screenPadding(),
                                )
                            }
                            if (twoPane) {
                                TwoPane(
                                    tabletop = tabletop,
                                    primary = { logScreen(Modifier.fillMaxHeight()) },
                                    secondary = {
                                        LiveDayPane(
                                            state = dayState,
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxHeight(),
                                        )
                                    },
                                )
                            } else {
                                logScreen(Modifier.fillMaxSize())
                            }
                        }

                        composable(FuelDestination.Today.route) {
                            if (twoPane) {
                                TwoPane(
                                    tabletop = tabletop,
                                    primary = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .verticalScroll(rememberScrollState())
                                                .padding(screenPadding()),
                                        ) {
                                            TodaySummaryPane(
                                                state = dayState,
                                                onShiftDay = viewModel::shiftDay,
                                                onSetBurn = { viewModel.setBurn(it) },
                                            )
                                        }
                                    },
                                    secondary = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .verticalScroll(rememberScrollState())
                                                .padding(screenPadding()),
                                        ) {
                                            TodayLogPane(
                                                state = dayState,
                                                onAdd = { name, calories, protein, fiber ->
                                                    viewModel.addEntry(name, calories, protein, fiber)
                                                },
                                                onStartEdit = viewModel::startEditing,
                                                onCancelEdit = viewModel::stopEditing,
                                                onSaveEdit = viewModel::updateEntry,
                                                onDelete = viewModel::deleteEntry,
                                            )
                                        }
                                    },
                                )
                            } else {
                                TodayScreen(
                                    state = dayState,
                                    onShiftDay = viewModel::shiftDay,
                                    onSetBurn = { viewModel.setBurn(it) },
                                    onAdd = { name, calories, protein, fiber ->
                                        viewModel.addEntry(name, calories, protein, fiber)
                                    },
                                    onStartEdit = viewModel::startEditing,
                                    onCancelEdit = viewModel::stopEditing,
                                    onSaveEdit = viewModel::updateEntry,
                                    onDelete = viewModel::deleteEntry,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = screenPadding(),
                                )
                            }
                        }

                        composable(FuelDestination.Calendar.route) {
                            if (twoPane) {
                                TwoPane(
                                    tabletop = tabletop,
                                    primary = {
                                        CalendarScreen(
                                            state = calendarState,
                                            selectedDate = selectedDate,
                                            onShiftMonth = viewModel::shiftMonth,
                                            onSelectDate = viewModel::selectDate,
                                            modifier = Modifier.fillMaxHeight(),
                                            contentPadding = screenPadding(),
                                            showWeekSummaries = false,
                                        )
                                    },
                                    secondary = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .verticalScroll(rememberScrollState())
                                                .padding(screenPadding()),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                        ) {
                                            SelectedDayCard(state = dayState)
                                            WeeklyDeficits(weeks = calendarState.weeks)
                                        }
                                    },
                                )
                            } else {
                                CalendarScreen(
                                    state = calendarState,
                                    selectedDate = selectedDate,
                                    onShiftMonth = viewModel::shiftMonth,
                                    onSelectDate = {
                                        viewModel.selectDate(it)
                                        navController.navigateToTopLevel(FuelDestination.Today)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = screenPadding(),
                                )
                            }
                        }

                        composable(FuelDestination.Settings.route) {
                            SettingsScreen(
                                settings = settings,
                                onThemeModeChange = settingsViewModel::setThemeMode,
                                onDynamicColorChange = settingsViewModel::setDynamicColor,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 640.dp),
                                contentPadding = screenPadding(),
                            )
                        }
                    }
                }
            }

            FuelToastHost(
                messages = viewModel.messages,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(innerPadding)
                    .padding(bottom = 20.dp),
            )
        }
    }
}

private fun screenPadding() = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)

/**
 * Side-by-side panes on the inner display; stacked when the device is held
 * half-open in tabletop posture, where a horizontal split matches the hinge.
 */
@Composable
private fun TwoPane(
    tabletop: Boolean,
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tabletop) {
        Column(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) { primary() }
            Box(modifier = Modifier.weight(1f)) { secondary() }
        }
    } else {
        Row(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.45f)) { primary() }
            Box(modifier = Modifier.weight(0.55f)) { secondary() }
        }
    }
}

/** Right-hand pane of the Log screen: today's totals and items, live. */
@Composable
private fun LiveDayPane(
    state: DayUiState,
    viewModel: FuelViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(screenPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TodaySummaryPane(
            state = state,
            onShiftDay = viewModel::shiftDay,
            onSetBurn = { viewModel.setBurn(it) },
        )
        TodayLogPane(
            state = state,
            onAdd = { name, calories, protein, fiber ->
                viewModel.addEntry(name, calories, protein, fiber)
            },
            onStartEdit = viewModel::startEditing,
            onCancelEdit = viewModel::stopEditing,
            onSaveEdit = viewModel::updateEntry,
            onDelete = viewModel::deleteEntry,
            showAddForm = false,
        )
    }
}

/** Read-only summary of the day picked in the calendar grid. */
@Composable
private fun SelectedDayCard(state: DayUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "${FuelFormat.dayTitle(state.date)} · ${FuelFormat.daySubtitle(state.date)}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        TotalsStrip(
            calories = state.summary.calories,
            protein = state.summary.protein,
            fiber = state.summary.fiber,
        )
        FuelCard {
            SectionLabel("Items logged")
            Spacer(Modifier.height(10.dp))
            if (state.entries.isEmpty()) {
                Text(
                    text = "Nothing logged",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FuelTheme.colors.textTertiary,
                )
            } else {
                state.entries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${FuelFormat.number(entry.calories)} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FuelTheme.colors.calories,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FuelBottomBar(
    current: FuelDestination,
    onSelect: (FuelDestination) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        FuelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = current == destination,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FuelTheme.colors.onAccent,
                    selectedTextColor = FuelTheme.colors.accentOnSurface,
                    indicatorColor = FuelTheme.colors.accent,
                    unselectedIconColor = FuelTheme.colors.textSecondary,
                    unselectedTextColor = FuelTheme.colors.textSecondary,
                ),
            )
        }
    }
}

@Composable
private fun FuelNavRail(
    current: FuelDestination,
    onSelect: (FuelDestination) -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.background,
        header = {
            Wordmark(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                fontSize = 20.sp,
            )
        },
    ) {
        Spacer(Modifier.height(8.dp))
        FuelDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = current == destination,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = FuelTheme.colors.onAccent,
                    selectedTextColor = FuelTheme.colors.accentOnSurface,
                    indicatorColor = FuelTheme.colors.accent,
                    unselectedIconColor = FuelTheme.colors.textSecondary,
                    unselectedTextColor = FuelTheme.colors.textSecondary,
                ),
            )
        }
    }
}

/** Single-instance top-level navigation that keeps one entry on the back stack. */
private fun NavHostController.navigateToTopLevel(destination: FuelDestination) {
    navigate(destination.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
