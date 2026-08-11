package com.personal.fuel.ui.navigation

/**
 * Which shell the app draws for a given window.
 *
 * Decided from the window's actual size rather than the width size class alone,
 * because both of the Fold's interesting cases sit awkwardly across the class
 * boundaries: the inner display in portrait is around 670dp wide — MEDIUM, but
 * wide enough for two panes — while the cover screen in landscape is very wide
 * and very short, where a bottom bar costs height the content needs.
 *
 * Using the window size also means split screen and free-form windows are
 * handled by the same rule, with no device checks anywhere.
 */
data class FuelLayout(
    val useRail: Boolean,
    val twoPane: Boolean,
) {
    companion object {
        /** A navigation rail replaces the bottom bar from here up. */
        const val RAIL_WIDTH_DP = 600

        /** Two panes fit from here up — the inner display, either orientation. */
        const val TWO_PANE_WIDTH_DP = 640

        /** Below this, a landscape window cannot spare height for a bottom bar. */
        const val SHORT_HEIGHT_DP = 480

        fun forWindow(widthDp: Int, heightDp: Int): FuelLayout {
            val landscapeAndShort = widthDp > heightDp && heightDp < SHORT_HEIGHT_DP
            return FuelLayout(
                useRail = widthDp >= RAIL_WIDTH_DP || landscapeAndShort,
                twoPane = widthDp >= TWO_PANE_WIDTH_DP,
            )
        }
    }
}
