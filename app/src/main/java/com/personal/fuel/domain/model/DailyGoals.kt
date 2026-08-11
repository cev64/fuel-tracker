package com.personal.fuel.domain.model

/**
 * Daily targets the ring widget fills against.
 *
 * A goal of zero means "not tracked": its ring shows an empty track rather than
 * dividing by nothing.
 */
data class DailyGoals(
    val calories: Double = DEFAULT_CALORIES,
    val protein: Double = DEFAULT_PROTEIN,
    val fiber: Double = DEFAULT_FIBER,
) {
    companion object {
        const val DEFAULT_CALORIES = 2_000.0
        const val DEFAULT_PROTEIN = 150.0
        const val DEFAULT_FIBER = 30.0
    }
}

/** How a widget paints its panel behind the content. */
enum class WidgetBackground {
    /** The Fuel card: solid near-black, like the app's surfaces. */
    SOLID,

    /**
     * Wallpaper shows through. A light scrim remains so the figures stay
     * readable over a bright or busy wallpaper.
     */
    TRANSPARENT;

    companion object {
        fun fromStorage(value: String?): WidgetBackground =
            entries.firstOrNull { it.name == value } ?: SOLID
    }
}
