package io.lcalmsky.github.ghosttyclaude

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WindowPositionTest {

    @Test
    fun `cell size calculation from font size 14`() {
        val fontSize = 14.0
        val cellWidth = fontSize * 0.6
        val cellHeight = fontSize * 1.45

        assertEquals(8.4, cellWidth, 0.01)
        assertEquals(20.3, cellHeight, 0.01)
    }

    @Test
    fun `cell size calculation from font size 13`() {
        val fontSize = 13.0
        val cellWidth = fontSize * 0.6
        val cellHeight = fontSize * 1.45

        assertEquals(7.8, cellWidth, 0.01)
        assertEquals(18.85, cellHeight, 0.01)
    }

    @Test
    fun `cols calculation with padding subtracted`() {
        val widthPx = 960
        val paddingX = 8
        val cellWidth = 14.0 * 0.6 // 8.4

        val cols = ((widthPx - 2 * paddingX) / cellWidth).toInt()
        assertEquals(112, cols) // (960 - 16) / 8.4 = 112.38 -> 112
    }

    @Test
    fun `rows calculation with padding subtracted`() {
        val heightPx = 1050
        val paddingY = 4
        val cellHeight = 14.0 * 1.45 // 20.3

        val rows = ((heightPx - 2 * paddingY) / cellHeight).toInt()
        assertEquals(51, rows) // (1050 - 8) / 20.3 = 51.33 -> 51
    }

    @Test
    fun `DEFAULT returns empty args`() {
        val args = WindowPosition.DEFAULT.toGhosttyArgs()
        assertEquals(emptyList(), args)
    }
}
