package com.personal.fuel

import com.personal.fuel.ui.navigation.FuelLayout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The window sizes the app actually has to handle on a Fold.
 *
 * The dp figures are representative of a large foldable rather than measured
 * from the device; the point of the rule is that it follows window size, so
 * these also stand in for split screen and free-form windows.
 */
class FuelLayoutTest {

    @Test
    fun `cover screen portrait is a single column with a bottom bar`() {
        val layout = FuelLayout.forWindow(widthDp = 393, heightDp = 852)

        assertFalse(layout.useRail)
        assertFalse(layout.twoPane)
    }

    @Test
    fun `cover screen landscape swaps the bottom bar for a rail`() {
        // Very wide, very short: a bottom bar would eat scarce vertical space.
        val layout = FuelLayout.forWindow(widthDp = 852, heightDp = 393)

        assertTrue(layout.useRail)
        assertTrue(layout.twoPane)
    }

    @Test
    fun `inner display portrait gets two panes despite being only medium width`() {
        val layout = FuelLayout.forWindow(widthDp = 673, heightDp = 841)

        assertTrue(layout.useRail)
        assertTrue(layout.twoPane)
    }

    @Test
    fun `inner display landscape gets two panes`() {
        val layout = FuelLayout.forWindow(widthDp = 841, heightDp = 673)

        assertTrue(layout.useRail)
        assertTrue(layout.twoPane)
    }

    @Test
    fun `half of the inner display in split screen falls back to one column`() {
        val layout = FuelLayout.forWindow(widthDp = 336, heightDp = 841)

        assertFalse(layout.useRail)
        assertFalse(layout.twoPane)
    }

    @Test
    fun `a tall narrow window never uses the short-landscape rule`() {
        // Guards the landscape clause against firing on portrait windows.
        val layout = FuelLayout.forWindow(widthDp = 400, heightDp = 400)

        assertFalse(layout.useRail)
        assertFalse(layout.twoPane)
    }

    @Test
    fun `a medium window narrower than the two-pane threshold keeps one pane`() {
        val layout = FuelLayout.forWindow(widthDp = 610, heightDp = 900)

        assertTrue(layout.useRail)
        assertFalse(layout.twoPane)
    }
}
