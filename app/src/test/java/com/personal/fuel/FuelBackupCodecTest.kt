package com.personal.fuel

import com.personal.fuel.data.backup.FuelBackupCodec
import com.personal.fuel.domain.model.DailyGoals
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FuelBackup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FuelBackupCodecTest {

    private val day = LocalDate.of(2026, 8, 11)
    private val otherDay = LocalDate.of(2026, 8, 12)

    private val backup = FuelBackup(
        entries = listOf(
            FoodEntry(date = day, name = "Chicken breast", calories = 250.0, protein = 45.0, fiber = 0.0, createdAt = 1_000L),
            FoodEntry(date = day, name = "Rice", calories = 320.0, protein = 7.0, fiber = 2.0, createdAt = 2_000L),
            FoodEntry(date = otherDay, name = "Oats", calories = 180.0, protein = 6.0, fiber = 5.0, createdAt = 3_000L),
        ),
        burns = mapOf(day to 2_400.0),
        goals = DailyGoals(calories = 2_100.0, protein = 160.0, fiber = 35.0),
    )

    @Test
    fun `a backup survives a round trip`() {
        val restored = FuelBackupCodec.decode(FuelBackupCodec.encode(backup))

        assertEquals(3, restored.entries.size)
        assertEquals(backup.entries.map { it.name }.sorted(), restored.entries.map { it.name }.sorted())
        assertEquals(2_400.0, restored.burns[day]!!, 0.001)
        assertEquals(2_100.0, restored.goals!!.calories, 0.001)
        assertEquals(2, restored.dayCount)
    }

    @Test
    fun `macros and logging times are preserved exactly`() {
        val restored = FuelBackupCodec.decode(FuelBackupCodec.encode(backup))
        val chicken = restored.entries.first { it.name == "Chicken breast" }

        assertEquals(250.0, chicken.calories, 0.001)
        assertEquals(45.0, chicken.protein, 0.001)
        assertEquals(day, chicken.date)
        assertEquals(1_000L, chicken.createdAt)
    }

    @Test
    fun `days without a burn do not invent one`() {
        val restored = FuelBackupCodec.decode(FuelBackupCodec.encode(backup))

        assertNull(restored.burns[otherDay])
    }

    @Test
    fun `the web app's stored data imports directly`() {
        val pwa = """
            {
              "fuel_v1": {
                "2026-08-11": [
                  {"id": 1754900000000, "name": "Chicken breast", "cal": 250, "pro": 45, "fib": 0},
                  {"id": 1754900100000, "name": "Rice", "cal": 320, "pro": 7, "fib": 2}
                ],
                "2026-08-12": [
                  {"id": 1754986400000, "name": "Oats", "cal": 180, "pro": 6, "fib": 5}
                ]
              },
              "fuel_burns": {"2026-08-11": 2400}
            }
        """.trimIndent()

        val restored = FuelBackupCodec.decode(pwa)

        assertEquals(3, restored.entries.size)
        assertEquals(2_400.0, restored.burns[day]!!, 0.001)
        assertEquals(2, restored.dayCount)
        // The web app used Date.now() as the row id, so it doubles as the time.
        assertEquals(1_754_900_000_000L, restored.entries.first { it.name == "Chicken breast" }.createdAt)
    }

    @Test
    fun `unnamed and malformed rows are skipped rather than imported blank`() {
        val messy = """
            {"days": {"2026-08-11": {"items": [
              {"name": "", "cal": 100},
              {"name": "   ", "cal": 100},
              {"name": "Real food", "cal": 100}
            ]}}}
        """.trimIndent()

        val restored = FuelBackupCodec.decode(messy)

        assertEquals(1, restored.entries.size)
        assertEquals("Real food", restored.entries.single().name)
    }

    @Test
    fun `an unrecognised file is rejected with a readable message`() {
        val error = runCatching { FuelBackupCodec.decode("""{"something": "else"}""") }
            .exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("not a Fuel backup"))
    }

    @Test
    fun `a file that is not json at all is rejected`() {
        val error = runCatching { FuelBackupCodec.decode("not json") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }
}
