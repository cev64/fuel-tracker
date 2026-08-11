package com.personal.fuel.domain.repository

/**
 * Lets the data layer tell home-screen widgets that their snapshot is stale
 * without depending on Glance. Implemented in the widgets package.
 */
fun interface WidgetNotifier {
    suspend fun onDataChanged()
}
