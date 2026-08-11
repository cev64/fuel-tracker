package com.personal.fuel.domain.model

import java.time.LocalDate

/** A single logged food item on a given day. */
data class FoodEntry(
    val id: Long = 0L,
    val date: LocalDate,
    val name: String,
    val calories: Double,
    val protein: Double,
    val fiber: Double,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * A previously logged food, collapsed by name, used for one-tap re-logging from
 * the Log screen and from the home-screen widget.
 */
data class FoodTemplate(
    val name: String,
    val calories: Double,
    val protein: Double,
    val fiber: Double,
)
