package com.personal.fuel.data.backup

import android.content.Context
import android.net.Uri
import com.personal.fuel.data.local.DayBurnEntity
import com.personal.fuel.data.local.FoodEntryEntity
import com.personal.fuel.data.local.FuelDao
import com.personal.fuel.data.prefs.SettingsRepository
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FuelBackup
import com.personal.fuel.domain.repository.WidgetNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Writes and reads the whole log as a JSON file the user picks.
 *
 * Uses the storage access framework, so the file lives wherever the user chose
 * and no storage permission is involved.
 */
class FuelBackupManager(
    private val context: Context,
    private val dao: FuelDao,
    private val settingsRepository: SettingsRepository,
    private val widgetNotifier: WidgetNotifier,
) {

    suspend fun export(destination: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val entries = dao.getAllEntries().map { it.toDomain() }
            val burns = dao.getAllBurns().associate { LocalDate.parse(it.dateKey) to it.burnedCalories }
            val backup = FuelBackup(
                entries = entries,
                burns = burns,
                goals = settingsRepository.currentGoals(),
                exportedAt = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            )

            context.contentResolver.openOutputStream(destination, "wt")?.use { stream ->
                stream.write(FuelBackupCodec.encode(backup).toByteArray())
            } ?: error("Could not write to that file")

            entries.size
        }
    }

    /**
     * Replaces everything currently stored with the contents of [source].
     *
     * Replace rather than merge: the reason to restore is that the log is gone
     * or wrong, and merging would silently double every item if a file were
     * imported twice.
     */
    suspend fun restore(source: Uri): Result<FuelBackup> = withContext(Dispatchers.IO) {
        runCatching {
            val json = context.contentResolver.openInputStream(source)?.use { stream ->
                stream.readBytes().decodeToString()
            } ?: error("Could not read that file")

            val backup = FuelBackupCodec.decode(json)
            require(backup.entries.isNotEmpty() || backup.burns.isNotEmpty()) {
                "That backup has nothing in it"
            }

            dao.replaceAll(
                entries = backup.entries.map { it.toEntity() },
                burns = backup.burns.map { (date, burned) ->
                    DayBurnEntity(dateKey = date.toString(), burnedCalories = burned)
                },
            )
            backup.goals?.let { settingsRepository.setGoals(it) }
            widgetNotifier.onDataChanged()

            backup
        }
    }
}

private fun FoodEntryEntity.toDomain() = FoodEntry(
    id = id,
    date = LocalDate.parse(dateKey),
    name = name,
    calories = calories,
    protein = protein,
    fiber = fiber,
    createdAt = createdAt,
)

private fun FoodEntry.toEntity() = FoodEntryEntity(
    // Ids are reassigned on insert so a restored file never collides with
    // whatever was there before.
    id = 0L,
    dateKey = date.toString(),
    name = name,
    calories = calories,
    protein = protein,
    fiber = fiber,
    createdAt = createdAt,
)
