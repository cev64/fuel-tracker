package com.personal.fuel.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.personal.fuel.widgets.GlanceWidgetNotifier
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Widgets show "today", so they go stale the moment the date rolls over. This
 * redraws them shortly after midnight; the app also refreshes them on every
 * write and on launch.
 */
class DailyWidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        GlanceWidgetNotifier(applicationContext).onDataChanged()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "fuel-daily-widget-refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyWidgetRefreshWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntilNextMidnight(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        private fun millisUntilNextMidnight(): Long {
            val now = java.time.LocalDateTime.now()
            val nextMidnight = LocalDate.now().plusDays(1).atTime(LocalTime.of(0, 1))
            return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0L)
        }
    }
}
