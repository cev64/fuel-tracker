package com.personal.fuel.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {

    @Query("SELECT * FROM food_entries WHERE dateKey = :dateKey ORDER BY createdAt ASC, id ASC")
    fun observeEntries(dateKey: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE dateKey = :dateKey ORDER BY createdAt ASC, id ASC")
    suspend fun getEntries(dateKey: String): List<FoodEntryEntity>

    @Query(
        "SELECT * FROM food_entries WHERE dateKey >= :startKey AND dateKey <= :endKey " +
            "ORDER BY dateKey ASC, createdAt ASC"
    )
    fun observeEntriesInRange(startKey: String, endKey: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM day_burns WHERE dateKey = :dateKey")
    fun observeBurn(dateKey: String): Flow<DayBurnEntity?>

    @Query("SELECT * FROM day_burns WHERE dateKey = :dateKey")
    suspend fun getBurn(dateKey: String): DayBurnEntity?

    @Query("SELECT * FROM day_burns WHERE dateKey >= :startKey AND dateKey <= :endKey")
    fun observeBurnsInRange(startKey: String, endKey: String): Flow<List<DayBurnEntity>>

    /**
     * Most recently logged foods, one row per name. SQLite resolves the bare
     * columns against the row that produced `MAX(createdAt)`, so each template
     * carries the macros from the last time that food was logged.
     */
    @Query(
        "SELECT name, calories, protein, fiber, MAX(createdAt) AS lastUsedAt FROM food_entries " +
            "GROUP BY name COLLATE NOCASE ORDER BY lastUsedAt DESC LIMIT :limit"
    )
    fun observeRecentFoods(limit: Int): Flow<List<FoodTemplateProjection>>

    @Query(
        "SELECT name, calories, protein, fiber, MAX(createdAt) AS lastUsedAt FROM food_entries " +
            "GROUP BY name COLLATE NOCASE ORDER BY lastUsedAt DESC LIMIT :limit"
    )
    suspend fun getRecentFoods(limit: Int): List<FoodTemplateProjection>

    @Insert
    suspend fun insertEntry(entry: FoodEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Query("SELECT * FROM food_entries ORDER BY dateKey ASC, createdAt ASC")
    suspend fun getAllEntries(): List<FoodEntryEntity>

    @Query("SELECT * FROM day_burns ORDER BY dateKey ASC")
    suspend fun getAllBurns(): List<DayBurnEntity>

    @Insert
    suspend fun insertEntries(entries: List<FoodEntryEntity>)

    @Upsert
    suspend fun upsertBurns(burns: List<DayBurnEntity>)

    @Query("DELETE FROM food_entries")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM day_burns")
    suspend fun deleteAllBurns()

    /** Restores a backup atomically: a failed import must not leave a half log. */
    @Transaction
    suspend fun replaceAll(entries: List<FoodEntryEntity>, burns: List<DayBurnEntity>) {
        deleteAllEntries()
        deleteAllBurns()
        insertEntries(entries)
        upsertBurns(burns)
    }

    @Upsert
    suspend fun upsertBurn(burn: DayBurnEntity)

    @Query("DELETE FROM day_burns WHERE dateKey = :dateKey")
    suspend fun deleteBurn(dateKey: String)
}
