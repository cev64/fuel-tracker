package com.personal.fuel.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level destinations. Each is reachable from a widget via its deep link, so
 * tapping a widget lands on the relevant screen rather than the app's home.
 */
enum class FuelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Log("log", "Log", Icons.Outlined.Add),
    Today("today", "Today", Icons.AutoMirrored.Outlined.List),
    Calendar("calendar", "Calendar", Icons.Outlined.DateRange),
    Settings("settings", "Settings", Icons.Outlined.Settings);

    val deepLink: String get() = "$SCHEME://$route"

    companion object {
        const val SCHEME = "fuel"

        fun fromRoute(route: String?): FuelDestination =
            entries.firstOrNull { it.route == route } ?: Log
    }
}
