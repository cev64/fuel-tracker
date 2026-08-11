package com.personal.fuel.widgets

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.personal.fuel.domain.repository.WidgetNotifier

/**
 * Redraws every installed Fuel widget after the data changes. Widgets render a
 * snapshot rather than subscribing to the database, so this is what keeps a
 * widget in step with edits made inside the app.
 */
class GlanceWidgetNotifier(private val context: Context) : WidgetNotifier {

    override suspend fun onDataChanged() {
        runCatching {
            QuickLogWidget().updateAll(context)
            TodaySummaryWidget().updateAll(context)
            MacrosWidget().updateAll(context)
        }.onFailure { error ->
            // A failed widget refresh must never take down the write that
            // triggered it.
            Log.w(TAG, "Widget refresh failed", error)
        }
    }

    private companion object {
        const val TAG = "FuelWidgets"
    }
}
