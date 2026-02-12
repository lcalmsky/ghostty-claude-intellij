package io.lcalmsky.github.ghosttyclaude

import java.awt.GraphicsEnvironment

enum class WindowPosition(val label: String) {
    DEFAULT("Default (no resize)"),
    LEFT_HALF("Left 1/2"),
    RIGHT_HALF("Right 1/2"),
    LEFT_THIRD("Left 1/3"),
    RIGHT_THIRD("Right 1/3"),
    LEFT_TWO_THIRDS("Left 2/3"),
    RIGHT_TWO_THIRDS("Right 2/3");

    /**
     * Returns Ghostty CLI arguments for window size and position.
     * Uses java.awt.GraphicsEnvironment to get screen dimensions.
     */
    fun toGhosttyArgs(): List<String> {
        if (this == DEFAULT) return emptyList()

        val screen = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        val bounds = screen.defaultConfiguration.bounds
        val insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(screen.defaultConfiguration)

        val screenWidth = bounds.width
        val screenHeight = bounds.height - insets.top // menu bar

        val (xRatio, widthRatio) = when (this) {
            LEFT_HALF -> 0.0 to 0.5
            RIGHT_HALF -> 0.5 to 0.5
            LEFT_THIRD -> 0.0 to (1.0 / 3)
            RIGHT_THIRD -> (2.0 / 3) to (1.0 / 3)
            LEFT_TWO_THIRDS -> 0.0 to (2.0 / 3)
            RIGHT_TWO_THIRDS -> (1.0 / 3) to (2.0 / 3)
            else -> return emptyList()
        }

        val x = (screenWidth * xRatio).toInt()
        val widthPx = (screenWidth * widthRatio).toInt()
        val y = insets.top

        // Ghostty window-width/height는 셀(글자) 단위
        // 대략적인 셀 크기: 가로 ~9px, 세로 ~18px (일반적인 터미널 폰트 기준)
        val cols = widthPx / 9
        val rows = screenHeight / 18

        return listOf(
            "--window-position-x=$x",
            "--window-position-y=$y",
            "--window-width=$cols",
            "--window-height=$rows"
        )
    }
}
