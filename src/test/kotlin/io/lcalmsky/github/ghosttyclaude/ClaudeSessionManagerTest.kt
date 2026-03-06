package io.lcalmsky.github.ghosttyclaude

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ClaudeSessionManagerTest {

    private val manager = ClaudeSessionManager()

    @Test
    fun `getSessionName returns consistent name for same path`() {
        val name1 = manager.getSessionName("/Users/test/project")
        val name2 = manager.getSessionName("/Users/test/project")
        assertEquals(name1, name2)
    }

    @Test
    fun `getSessionName returns different name for different paths`() {
        val name1 = manager.getSessionName("/Users/test/project-a")
        val name2 = manager.getSessionName("/Users/test/project-b")
        assertNotEquals(name1, name2)
    }

    @Test
    fun `getSessionName includes directory name`() {
        val name = manager.getSessionName("/Users/test/my-project")
        assertTrue(name.startsWith("claude-my-project-"))
    }

    @Test
    fun `getSessionName differentiates same dir name in different locations`() {
        val name1 = manager.getSessionName("/Users/test/workspace/project")
        val name2 = manager.getSessionName("/Users/test/worktrees/project")
        assertNotEquals(name1, name2)
    }

    @Test
    fun `shellEscape handles simple string`() {
        assertEquals("'hello'", ClaudeSessionManager.shellEscape("hello"))
    }

    @Test
    fun `shellEscape handles string with spaces`() {
        assertEquals("'hello world'", ClaudeSessionManager.shellEscape("hello world"))
    }

    @Test
    fun `shellEscape handles string with single quotes`() {
        assertEquals("'it'\\''s a test'", ClaudeSessionManager.shellEscape("it's a test"))
    }

    @Test
    fun `shellEscape handles path with special characters`() {
        val escaped = ClaudeSessionManager.shellEscape("/Users/test/my project (1)")
        assertEquals("'/Users/test/my project (1)'", escaped)
    }

    @Test
    fun `multiline tmux commands are joined with &&`() {
        val input = """
            tmux set mouse on
            tmux set history-limit 50000
            tmux set-window-option mode-keys vi
        """.trimIndent()

        val result = input
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .joinToString(" && ")

        assertEquals(
            "tmux set mouse on && tmux set history-limit 50000 && tmux set-window-option mode-keys vi",
            result
        )
    }

    @Test
    fun `multiline tmux commands ignore comments and empty lines`() {
        val input = """
            tmux set mouse on
            # comment line
            tmux set history-limit 50000

            tmux set escape-time 0
        """.trimIndent()

        val result = input
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .joinToString(" && ")

        assertEquals(
            "tmux set mouse on && tmux set history-limit 50000 && tmux set escape-time 0",
            result
        )
    }

    @Test
    fun `empty or comment-only tmux commands produce empty string`() {
        val input = """
            # comment
            # another comment
        """.trimIndent()

        val result = input
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .joinToString(" && ")

        assertEquals("", result)
    }
}
