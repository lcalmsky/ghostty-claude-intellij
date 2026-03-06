package io.lcalmsky.github.ghosttyclaude

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TmuxConfigGeneratorTest {

    @Test
    fun `copy-on-select true generates pbcopy binding`() {
        val config = GhosttyConfigReader.GhosttyConfig(copyOnSelect = "true")
        val commands = TmuxConfigGenerator.generate(config)
        assertTrue(commands.any { it.contains("pbcopy") })
    }

    @Test
    fun `copy-on-select clipboard generates pbcopy binding`() {
        val config = GhosttyConfigReader.GhosttyConfig(copyOnSelect = "clipboard")
        val commands = TmuxConfigGenerator.generate(config)
        assertTrue(commands.any { it.contains("pbcopy") })
    }

    @Test
    fun `copy-on-select false does not generate pbcopy binding`() {
        val config = GhosttyConfigReader.GhosttyConfig(copyOnSelect = "false")
        val commands = TmuxConfigGenerator.generate(config)
        assertFalse(commands.any { it.contains("pbcopy") })
    }

    @Test
    fun `clipboard-write allow-always sets clipboard on`() {
        val config = GhosttyConfigReader.GhosttyConfig(clipboardWrite = "allow-always")
        val commands = TmuxConfigGenerator.generate(config)
        assertTrue(commands.contains("tmux set set-clipboard on"))
    }

    @Test
    fun `clipboard-write deny sets clipboard off`() {
        val config = GhosttyConfigReader.GhosttyConfig(clipboardWrite = "deny")
        val commands = TmuxConfigGenerator.generate(config)
        assertTrue(commands.contains("tmux set set-clipboard off"))
    }

    @Test
    fun `clipboard-write ask does not set clipboard`() {
        val config = GhosttyConfigReader.GhosttyConfig(clipboardWrite = "ask")
        val commands = TmuxConfigGenerator.generate(config)
        assertFalse(commands.any { it.contains("set-clipboard") })
    }

    @Test
    fun `scrollback-limit generates history-limit`() {
        val config = GhosttyConfigReader.GhosttyConfig(scrollbackLimit = 10000)
        val commands = TmuxConfigGenerator.generate(config)
        assertTrue(commands.contains("tmux set history-limit 10000"))
    }

    @Test
    fun `null scrollback-limit omits history-limit`() {
        val config = GhosttyConfigReader.GhosttyConfig(scrollbackLimit = null)
        val commands = TmuxConfigGenerator.generate(config)
        assertFalse(commands.any { it.contains("history-limit") })
    }

    @Test
    fun `always includes mouse on`() {
        val config = GhosttyConfigReader.GhosttyConfig()
        val commands = TmuxConfigGenerator.generate(config)
        assertTrue(commands.contains("tmux set mouse on"))
    }

    @Test
    fun `toShellString wraps commands with error suppression`() {
        val config = GhosttyConfigReader.GhosttyConfig(
            copyOnSelect = "false",
            clipboardWrite = "ask",
            scrollbackLimit = null
        )
        val result = TmuxConfigGenerator.toShellString(config)
        assertTrue(result.contains("2>/dev/null || true"))
        assertFalse(result.contains("pbcopy"))
    }

    @Test
    fun `toShellString joins multiple commands with and-and`() {
        val config = GhosttyConfigReader.GhosttyConfig(
            copyOnSelect = "true",
            clipboardWrite = "allow-always",
            scrollbackLimit = 5000
        )
        val result = TmuxConfigGenerator.toShellString(config)
        assertEquals(5, result.split(" && ").size)
    }
}
