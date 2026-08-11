package com.personal.fuel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.fuel.ui.DeepLinkTarget
import com.personal.fuel.ui.FuelRoot
import com.personal.fuel.ui.FuelViewModel
import com.personal.fuel.ui.SettingsViewModel
import com.personal.fuel.ui.navigation.FuelDestination
import com.personal.fuel.ui.theme.FuelTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    /**
     * Set from the launching intent. Widgets deep-link into a specific screen —
     * and, for a day-specific widget tap, a specific day.
     */
    private var pendingDeepLink by mutableStateOf<DeepLinkTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingDeepLink = parseDeepLink(intent)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            FuelTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
                val fuelViewModel: FuelViewModel = viewModel(factory = FuelViewModel.Factory)
                FuelRoot(
                    viewModel = fuelViewModel,
                    settingsViewModel = settingsViewModel,
                    deepLink = pendingDeepLink,
                    onDeepLinkHandled = { pendingDeepLink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink = parseDeepLink(intent)
    }

    private fun parseDeepLink(intent: Intent?): DeepLinkTarget? {
        val uri = intent?.data ?: return null
        if (uri.scheme != FuelDestination.SCHEME) return null
        val destination = FuelDestination.fromRoute(uri.host)
        val date = uri.getQueryParameter("date")?.let { value ->
            runCatching { LocalDate.parse(value) }.getOrNull()
        }
        return DeepLinkTarget(destination = destination, date = date)
    }
}
