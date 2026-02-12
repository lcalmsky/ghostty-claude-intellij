package io.lcalmsky.github.ghosttyclaude

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.ceil

object GhosttyConfigReader {

    data class GhosttyConfig(
        val fontFamily: String = DEFAULT_FONT_FAMILY,
        val fontSize: Double = DEFAULT_FONT_SIZE,
        val paddingX: Int = 0,
        val paddingY: Int = 0
    ) {
        fun cellWidth(): Double {
            val font = Font(fontFamily, Font.PLAIN, fontSize.toInt())
            val frc = FontRenderContext(AffineTransform(), true, true)
            val logicalWidth = font.getStringBounds("M", frc).width
            // Ghostty rounds cell size to physical pixels on Retina displays
            val scale = displayScaleFactor()
            return ceil(logicalWidth * scale) / scale
        }

        fun cellHeight(): Double {
            val font = Font(fontFamily, Font.PLAIN, fontSize.toInt())
            val metrics = font.getLineMetrics("M", FontRenderContext(AffineTransform(), true, true))
            val logicalHeight = (metrics.ascent + metrics.descent + metrics.leading).toDouble()
            val scale = displayScaleFactor()
            return ceil(logicalHeight * scale) / scale
        }

        private fun displayScaleFactor(): Double {
            return try {
                val screen = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
                screen.defaultConfiguration.defaultTransform.scaleX
            } catch (_: Exception) {
                2.0 // macOS Retina default
            }
        }
    }

    private const val DEFAULT_FONT_FAMILY = "Menlo"
    private const val DEFAULT_FONT_SIZE = 13.0

    fun read(configPath: Path = defaultConfigPath()): GhosttyConfig {
        if (!Files.exists(configPath)) return GhosttyConfig()

        var fontFamily = DEFAULT_FONT_FAMILY
        var fontSize = DEFAULT_FONT_SIZE
        var paddingX = 0
        var paddingY = 0

        try {
            Files.readAllLines(configPath).forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach

                val parts = trimmed.split("=", limit = 2)
                if (parts.size != 2) return@forEach

                val key = parts[0].trim()
                val value = parts[1].trim()

                when (key) {
                    "font-family" -> fontFamily = value
                    "font-size" -> value.toDoubleOrNull()?.let { fontSize = it }
                    "window-padding-x" -> value.split(",")[0].trim().toIntOrNull()?.let { paddingX = it }
                    "window-padding-y" -> value.split(",")[0].trim().toIntOrNull()?.let { paddingY = it }
                }
            }
        } catch (_: Exception) {
            return GhosttyConfig()
        }

        return GhosttyConfig(fontFamily, fontSize, paddingX, paddingY)
    }

    private fun defaultConfigPath(): Path =
        Paths.get(System.getProperty("user.home"), ".config", "ghostty", "config")
}
