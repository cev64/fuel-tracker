package com.personal.fuel.utilities

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptics where there is no [android.view.View] to route them through: widget
 * tap callbacks run in this app's process but draw through the launcher, and an
 * Activity launched from a widget should buzz before its first frame exists.
 *
 * The effects are chosen to match [FuelHaptics] as closely as the vibrator API
 * allows, so a button in a widget feels like the same button in the app.
 *
 * Vibrations are tagged as touch feedback, so the system scales or suppresses
 * them according to the phone's haptic settings.
 */
object FuelVibration {

    fun perform(context: Context, haptic: FuelHaptic) {
        val vibrator = vibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        val effect = effectFor(vibrator, haptic)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(
                effect,
                VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_TOUCH)
                    .build(),
            )
        } else {
            // VibrationAttributes only exists from API 33; API 30-32 uses the
            // AudioAttributes overload, which conveys the same touch usage.
            @Suppress("DEPRECATION")
            vibrator.vibrate(effect, TOUCH_AUDIO_ATTRIBUTES)
        }
    }

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    private fun effectFor(vibrator: Vibrator, haptic: FuelHaptic): VibrationEffect = when (haptic) {
        FuelHaptic.Light -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
        FuelHaptic.Press -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        FuelHaptic.Reject -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
        // A firm click followed by a lighter tick — the same "done" shape as the
        // platform's CONFIRM constant. Falls back on hardware without primitives.
        FuelHaptic.Confirm -> if (
            vibrator.areAllPrimitivesSupported(
                VibrationEffect.Composition.PRIMITIVE_CLICK,
                VibrationEffect.Composition.PRIMITIVE_TICK,
            )
        ) {
            VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.6f, 70)
                .compose()
        } else {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        }
    }

    private val TOUCH_AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
}
