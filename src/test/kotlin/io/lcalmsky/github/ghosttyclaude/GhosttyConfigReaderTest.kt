package io.lcalmsky.github.ghosttyclaude

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class GhosttyConfigReaderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `parses valid config with all fields`() {
        val configFile = tempDir.resolve("config")
        Files.writeString(configFile, """
            font-family = JetBrainsMono Nerd Font Mono
            font-size = 14
            window-padding-x = 8
            window-padding-y = 4
        """.trimIndent())

        val config = GhosttyConfigReader.read(configFile)
        assertEquals(14.0, config.fontSize)
        assertEquals(8, config.paddingX)
        assertEquals(4, config.paddingY)
    }

    @Test
    fun `ignores comments and blank lines`() {
        val configFile = tempDir.resolve("config")
        Files.writeString(configFile, """
            # This is a comment
            font-size = 16

            # Another comment
            window-padding-x = 10
        """.trimIndent())

        val config = GhosttyConfigReader.read(configFile)
        assertEquals(16.0, config.fontSize)
        assertEquals(10, config.paddingX)
        assertEquals(0, config.paddingY)
    }

    @Test
    fun `returns defaults for missing fields`() {
        val configFile = tempDir.resolve("config")
        Files.writeString(configFile, """
            theme = Catppuccin Mocha
            cursor-style = block
        """.trimIndent())

        val config = GhosttyConfigReader.read(configFile)
        assertEquals(13.0, config.fontSize)
        assertEquals(0, config.paddingX)
        assertEquals(0, config.paddingY)
    }

    @Test
    fun `returns defaults when file does not exist`() {
        val config = GhosttyConfigReader.read(tempDir.resolve("nonexistent"))
        assertEquals(13.0, config.fontSize)
        assertEquals(0, config.paddingX)
        assertEquals(0, config.paddingY)
    }

    @Test
    fun `handles decimal font size`() {
        val configFile = tempDir.resolve("config")
        Files.writeString(configFile, "font-size = 13.5")

        val config = GhosttyConfigReader.read(configFile)
        assertEquals(13.5, config.fontSize)
    }

    @Test
    fun `handles padding with comma-separated values`() {
        val configFile = tempDir.resolve("config")
        Files.writeString(configFile, """
            window-padding-x = 8,12
            window-padding-y = 4,6
        """.trimIndent())

        val config = GhosttyConfigReader.read(configFile)
        assertEquals(8, config.paddingX)
        assertEquals(4, config.paddingY)
    }

    @Test
    fun `ignores malformed lines`() {
        val configFile = tempDir.resolve("config")
        Files.writeString(configFile, """
            font-size = 14
            this is not valid
            window-padding-x = abc
            window-padding-y = 6
        """.trimIndent())

        val config = GhosttyConfigReader.read(configFile)
        assertEquals(14.0, config.fontSize)
        assertEquals(0, config.paddingX)
        assertEquals(6, config.paddingY)
    }
}
