package com.personal.fuel.utilities

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Fuel's haptic vocabulary. Four levels, used consistently, so the phone tells
 * you what happened before you have read the screen:
 *
 *  - [Light] you moved — navigation, day and month arrows, calendar cells
 *  - [Press] you pressed a control that does not commit anything
 *  - [Confirm] something was written — food logged, edit saved
 *  - [Reject] something was removed, or refused
 *
 * Text fields deliberately have no haptics; the keyboard already provides them.
 */
enum class FuelHaptic {
    Light,
    Press,
    Confirm,
    Reject,
}

/**
 * Haptics inside the app.
 *
 * Routed through the [View] rather than the vibrator service so the phone's own
 * touch-feedback settings — including Samsung's intensity slider — apply, and so
 * no vibrate permission is needed on this path.
 */
class FuelHaptics(private val view: View) {

    fun perform(haptic: FuelHaptic) {
        view.performHapticFeedback(haptic.viewConstant())
    }
}

private fun FuelHaptic.viewConstant(): Int = when (this) {
    // Crisp and quiet: felt, but not intrusive when moving between days.
    FuelHaptic.Light -> HapticFeedbackConstants.CLOCK_TICK
    // The classic button thump.
    FuelHaptic.Press -> HapticFeedbackConstants.VIRTUAL_KEY
    // A distinct two-part pattern the platform reserves for completed actions.
    FuelHaptic.Confirm -> HapticFeedbackConstants.CONFIRM
    FuelHaptic.Reject -> HapticFeedbackConstants.REJECT
}

@Composable
fun rememberFuelHaptics(): FuelHaptics {
    val view = LocalView.current
    return remember(view) { FuelHaptics(view) }
}
