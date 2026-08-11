package com.personal.fuel

import android.app.Application
import android.content.Context
import com.personal.fuel.data.backup.FuelBackupManager
import com.personal.fuel.data.local.FuelDatabase
import com.personal.fuel.data.prefs.SettingsRepository
import com.personal.fuel.data.repository.FuelRepositoryImpl
import com.personal.fuel.domain.repository.FuelRepository
import com.personal.fuel.widgets.GlanceWidgetNotifier
import com.personal.fuel.workers.DailyWidgetRefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency container.
 *
 * The app is small enough that a hand-written container beats a DI framework
 * here, and widgets/workers can reach it from a plain [Context] without any
 * extra entry-point plumbing.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: FuelDatabase by lazy { FuelDatabase.create(appContext) }

    private val widgetNotifier = GlanceWidgetNotifier(appContext)

    val repository: FuelRepository by lazy {
        FuelRepositoryImpl(database.fuelDao(), widgetNotifier)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext, widgetNotifier)
    }

    val backupManager: FuelBackupManager by lazy {
        FuelBackupManager(appContext, database.fuelDao(), settingsRepository, widgetNotifier)
    }

    /** For work that must outlive a screen, such as widget-triggered writes. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

class FuelApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        DailyWidgetRefreshWorker.schedule(this)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as FuelApp).container
