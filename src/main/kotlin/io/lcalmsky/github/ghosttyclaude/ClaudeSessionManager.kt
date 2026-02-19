package io.lcalmsky.github.ghosttyclaude

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.io.File
import java.security.MessageDigest

class ClaudeSessionManager {
    fun getSessionName(projectPath: String): String {
        val dirName = File(projectPath).name
        val hash =
            MessageDigest
                .getInstance("MD5")
                .digest(projectPath.toByteArray())
                .take(3)
                .joinToString("") { "%02x".format(it) }
        return "claude-$dirName-$hash"
    }

    fun sessionExists(sessionName: String): Boolean {
        val tmux = findTmuxPath() ?: return false
        return runCommand(tmux, "has-session", "-t", sessionName) == 0
    }

    fun isSessionAttached(sessionName: String): Boolean {
        val tmux = findTmuxPath() ?: return false
        val result =
            runCommandOutput(
                tmux,
                "list-sessions",
                "-F",
                "#{session_name}:#{session_attached}",
            )
        return result.lines().any { it == "$sessionName:1" }
    }

    fun launchNewSession(
        sessionName: String,
        projectPath: String,
    ) {
        val ghosttyPath =
            findGhosttyPath()
                ?: throw IllegalStateException("Ghostty not found")
        val tmuxPath =
            findTmuxPath()
                ?: throw IllegalStateException("tmux not found. Install with: brew install tmux")

        val claudeArgs = GhosttyClaudeSettings.getInstance().buildClaudeArgs()
        val argsStr = if (claudeArgs.isNotBlank()) " $claudeArgs" else ""

        val escapedPath = projectPath.replace("\"", "\\\"")
        val shellCommand = "cd \"$escapedPath\" && (tmux set -p allow-passthrough on 2>/dev/null || true) && printf \"\\ePtmux;\\e\\e]7;file://%s%s\\a\\e\\\\\\\\\" \"\$HOST\" \"\$PWD\" && claude$argsStr"

        val cmd = mutableListOf(ghosttyPath)
        cmd += windowPositionArgs()
        cmd +=
            listOf(
                "-e",
                tmuxPath,
                "new-session",
                "-s",
                sessionName,
                "zsh -lic '$shellCommand'",
            )

        ProcessBuilder(cmd).start()
    }

    fun reattachSession(sessionName: String) {
        val ghosttyPath =
            findGhosttyPath()
                ?: throw IllegalStateException("Ghostty not found")
        val tmuxPath =
            findTmuxPath()
                ?: throw IllegalStateException("tmux not found")

        val cmd = mutableListOf(ghosttyPath)
        cmd += windowPositionArgs()
        cmd += listOf("-e", tmuxPath, "attach-session", "-t", sessionName)

        ProcessBuilder(cmd).start()
    }

    private fun windowPositionArgs(): List<String> {
        val posName = GhosttyClaudeSettings.getInstance().state.windowPosition
        val position =
            try {
                WindowPosition.valueOf(posName)
            } catch (_: Exception) {
                WindowPosition.DEFAULT
            }
        return position.toGhosttyArgs()
    }

    fun sendKeys(
        sessionName: String,
        text: String,
    ) {
        val tmux = findTmuxPath() ?: return
        ProcessBuilder(tmux, "send-keys", "-t", sessionName, "-l", text)
            .start()
            .waitFor()
    }

    fun activateGhostty() {
        val script = """tell application "Ghostty" to activate"""
        ProcessBuilder("/usr/bin/osascript", "-e", script)
            .start()
            .waitFor()
    }

    fun notifyError(
        project: Project?,
        message: String,
    ) {
        if (project == null) return
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("GhosttyClaude")
            .createNotification(message, NotificationType.ERROR)
            .notify(project)
    }

    fun findGhosttyPath(): String? = findBinary("ghostty", "/Applications/Ghostty.app/Contents/MacOS/ghostty")

    fun findTmuxPath(): String? = findBinary("tmux", "/opt/homebrew/bin/tmux", "/usr/local/bin/tmux")

    private fun findBinary(
        name: String,
        vararg fallbackPaths: String,
    ): String? {
        val whichResult = runCommandOutput("/usr/bin/which", name).trim()
        if (whichResult.isNotEmpty() && File(whichResult).exists()) {
            return whichResult
        }
        for (path in fallbackPaths) {
            if (File(path).exists()) {
                return path
            }
        }
        return null
    }

    private fun runCommand(vararg command: String): Int =
        try {
            ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()
                .waitFor()
        } catch (e: Exception) {
            -1
        }

    private fun runCommandOutput(vararg command: String): String =
        try {
            val process =
                ProcessBuilder(*command)
                    .redirectErrorStream(true)
                    .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
        }

    companion object {
        fun shellEscape(value: String): String = "'" + value.replace("'", "'\\''") + "'"
    }
}
