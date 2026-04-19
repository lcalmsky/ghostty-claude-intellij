package io.lcalmsky.github.ghosttyclaude

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GhosttyClaudeSettingsDefaultsTest {

    @Test
    fun `default tmuxSetupCommands enables mouse on`() {
        val default = GhosttyClaudeSettings.State().tmuxSetupCommands
        assertTrue(
            default.lineSequence().any { it.trim() == "tmux set mouse on" },
            "expected default to contain 'tmux set mouse on', was:\n$default"
        )
    }

    @Test
    fun `default tmuxSetupCommands does not disable mouse`() {
        val default = GhosttyClaudeSettings.State().tmuxSetupCommands
        assertFalse(
            default.contains("tmux set mouse off"),
            "default should not disable mouse, was:\n$default"
        )
    }

    @Test
    fun `default tmuxSetupCommands joins into expected shell sequence`() {
        val joined = GhosttyClaudeSettings.State().tmuxSetupCommands
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .joinToString(" && ")

        assertEquals(
            "tmux set mouse on && tmux set history-limit 50000 && tmux set-window-option mode-keys vi && tmux set escape-time 0",
            joined
        )
    }
}
