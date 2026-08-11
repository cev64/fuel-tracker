package com.personal.fuel.domain.usecase

/**
 * How full a progress ring should be.
 *
 * Going over the goal keeps counting rather than clamping at full: the ring
 * draws a second lap so a large overshoot is visible instead of looking the
 * same as landing exactly on target. Two laps is the ceiling — beyond that the
 * ring stops being readable and the number does the talking.
 */
object GoalProgress {

    const val MAX_LAPS = 2f

    fun fraction(value: Double, goal: Double): Float {
        if (goal <= 0.0 || value <= 0.0) return 0f
        return (value / goal).toFloat().coerceIn(0f, MAX_LAPS)
    }
}
