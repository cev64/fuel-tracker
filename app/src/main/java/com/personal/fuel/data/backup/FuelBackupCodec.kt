package com.personal.fuel.data.backup

import com.personal.fuel.domain.model.DailyGoals
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FuelBackup
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Reads and writes the backup file.
 *
 * Kept free of Android I/O so the format itself can be unit tested — the file
 * handling lives in [FuelBackupManager].
 *
 * Two shapes are accepted on the way in: Fuel's own export, and the browser
 * storage of the original PWA (`fuel_v1` / `fuel_burns`), so the web app's
 * history can be brought across without a separate importer.
 */
object FuelBackupCodec {

    const val FORMAT_VERSION = 1

    fun encode(backup: FuelBackup): String {
        val days = JSONObject()
        val dates = (backup.entries.map { it.date } + backup.burns.keys).distinct().sorted()

        for (date in dates) {
            val items = JSONArray()
            backup.entries.filter { it.date == date }.forEach { entry ->
                items.put(
                    JSONObject().apply {
                        put("name", entry.name)
                        put("cal", entry.calories)
                        put("pro", entry.protein)
                        put("fib", entry.fiber)
                        put("at", entry.createdAt)
                    }
                )
            }
            days.put(
                date.toString(),
                JSONObject().apply {
                    put("items", items)
                    backup.burns[date]?.let { put("burned", it) }
                },
            )
        }

        return JSONObject().apply {
            put("app", "fuel")
            put("version", FORMAT_VERSION)
            backup.exportedAt?.let { put("exportedAt", it) }
            backup.goals?.let { goals ->
                put(
                    "goals",
                    JSONObject().apply {
                        put("calories", goals.calories)
                        put("protein", goals.protein)
                        put("fiber", goals.fiber)
                    },
                )
            }
            put("days", days)
        }.toString(2)
    }

    /** @throws IllegalArgumentException if the file is not a Fuel or PWA backup. */
    fun decode(json: String): FuelBackup {
        val root = runCatching { JSONObject(json) }.getOrElse {
            throw IllegalArgumentException("That file is not valid JSON")
        }

        return when {
            root.has("days") -> decodeFuelExport(root)
            root.has("fuel_v1") || root.has("fuel_burns") -> decodePwaExport(root)
            else -> throw IllegalArgumentException("That file is not a Fuel backup")
        }
    }

    private fun decodeFuelExport(root: JSONObject): FuelBackup {
        val days = root.getJSONObject("days")
        val entries = mutableListOf<FoodEntry>()
        val burns = mutableMapOf<LocalDate, Double>()

        for (key in days.keys()) {
            val date = parseDate(key) ?: continue
            val day = days.getJSONObject(key)

            val items = day.optJSONArray("items") ?: JSONArray()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val name = item.optString("name").trim()
                if (name.isEmpty()) continue
                entries += FoodEntry(
                    date = date,
                    name = name,
                    calories = item.optDouble("cal", 0.0).orZero(),
                    protein = item.optDouble("pro", 0.0).orZero(),
                    fiber = item.optDouble("fib", 0.0).orZero(),
                    createdAt = item.optLong("at", 0L).takeIf { it > 0L }
                        ?: (date.toEpochDay() * MILLIS_PER_DAY + i),
                )
            }

            val burned = day.optDouble("burned", 0.0).orZero()
            if (burned > 0.0) burns[date] = burned
        }

        val goals = root.optJSONObject("goals")?.let {
            DailyGoals(
                calories = it.optDouble("calories", DailyGoals.DEFAULT_CALORIES).orZero(),
                protein = it.optDouble("protein", DailyGoals.DEFAULT_PROTEIN).orZero(),
                fiber = it.optDouble("fiber", DailyGoals.DEFAULT_FIBER).orZero(),
            )
        }

        return FuelBackup(
            entries = entries,
            burns = burns,
            goals = goals,
            exportedAt = root.optString("exportedAt").takeIf { it.isNotEmpty() },
        )
    }

    /** The web app's localStorage: `fuel_v1` holds the log, `fuel_burns` the burns. */
    private fun decodePwaExport(root: JSONObject): FuelBackup {
        val entries = mutableListOf<FoodEntry>()
        val burns = mutableMapOf<LocalDate, Double>()

        root.optJSONObject("fuel_v1")?.let { log ->
            for (key in log.keys()) {
                val date = parseDate(key) ?: continue
                val items = log.optJSONArray(key) ?: continue
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val name = item.optString("name").trim()
                    if (name.isEmpty()) continue
                    entries += FoodEntry(
                        date = date,
                        name = name,
                        calories = item.optDouble("cal", 0.0).orZero(),
                        protein = item.optDouble("pro", 0.0).orZero(),
                        fiber = item.optDouble("fib", 0.0).orZero(),
                        // The web app used Date.now() as the id, so it doubles
                        // as the original logging time.
                        createdAt = item.optLong("id", 0L).takeIf { it > 0L }
                            ?: (date.toEpochDay() * MILLIS_PER_DAY + i),
                    )
                }
            }
        }

        root.optJSONObject("fuel_burns")?.let { stored ->
            for (key in stored.keys()) {
                val date = parseDate(key) ?: continue
                val burned = stored.optDouble(key, 0.0).orZero()
                if (burned > 0.0) burns[date] = burned
            }
        }

        return FuelBackup(entries = entries, burns = burns, goals = null)
    }

    private fun parseDate(value: String): LocalDate? =
        runCatching { LocalDate.parse(value) }.getOrNull()

    /** JSON NaN and infinities would otherwise poison the totals. */
    private fun Double.orZero(): Double = if (isFinite()) this else 0.0

    private const val MILLIS_PER_DAY = 86_400_000L
}
