package com.personal.fuel

import com.personal.fuel.domain.usecase.GoalProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalProgressTest {

    @Test
    fun `an empty day leaves the ring empty`() {
        assertEquals(0f, GoalProgress.fraction(0.0, 2_000.0), 0.001f)
    }

    @Test
    fun `half the goal fills half the ring`() {
        assertEquals(0.5f, GoalProgress.fraction(1_000.0, 2_000.0), 0.001f)
    }

    @Test
    fun `hitting the goal fills the ring exactly`() {
        assertEquals(1f, GoalProgress.fraction(2_000.0, 2_000.0), 0.001f)
    }

    @Test
    fun `going over the goal starts a second lap`() {
        assertEquals(1.5f, GoalProgress.fraction(3_000.0, 2_000.0), 0.001f)
    }

    @Test
    fun `a huge overshoot stops at two laps`() {
        assertEquals(GoalProgress.MAX_LAPS, GoalProgress.fraction(90_000.0, 2_000.0), 0.001f)
    }

    @Test
    fun `an untracked goal never divides by zero`() {
        assertEquals(0f, GoalProgress.fraction(1_500.0, 0.0), 0.001f)
        assertEquals(0f, GoalProgress.fraction(1_500.0, -10.0), 0.001f)
    }

    @Test
    fun `negative intake is treated as nothing logged`() {
        assertEquals(0f, GoalProgress.fraction(-5.0, 2_000.0), 0.001f)
    }
}
