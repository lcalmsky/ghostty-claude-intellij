package io.lcalmsky.github.ghosttyclaude

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.io.File
import java.security.MessageDigest

class ClaudeSessionManager {

    fun getSessionName(projectPath: String): String {
        val dirName = File(projectPath).name
        val hash = MessageDigest.getInstance("MD5")
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
        val result = runCommandOutput(
            tmux, "list-sessions", "-F", "#{session_name}:#{session_attached}"
        )
        return result.lines().any { it == "$sessionName:1" }
    }

    fun launchNewSession(sessionName: String, projectPath: String) {
        val ghosttyPath = findGhosttyPath()
            ?: throw IllegalStateException("Ghostty not found")
        val tmuxPath = findTmuxPath()
            ?: throw IllegalStateException("tmux not found. Install with: brew install tmux")

        // 경로에서 double quote만 이스케이프 (single-quote 안에서 사용)
        val escapedPath = projectPath.replace("\"", "\\\"")
        val shellCommand = "cd \"$escapedPath\" && claude"

        ProcessBuilder(
            ghosttyPath,
            "-e", tmuxPath, "new-session", "-s", sessionName,
            "zsh -lic '$shellCommand'"
        ).start()
    }

    fun reattachSession(sessionName: String) {
        val ghosttyPath = findGhosttyPath()
            ?: throw IllegalStateException("Ghostty not found")
        val tmuxPath = findTmuxPath()
            ?: throw IllegalStateException("tmux not found")

        ProcessBuilder(
            ghosttyPath,
            "-e", tmuxPath, "attach-session", "-t", sessionName
        ).start()
    }

    fun sendKeys(sessionName: String, text: String) {
        val tmux = findTmuxPath() ?: return
        ProcessBuilder(tmux, "send-keys", "-t", sessionName, "-l", text)
            .start()
            .waitFor()
    }

    fun activateGhostty() {
        // AppleScript로 Ghostty 활성화 - open -a 보다 포커스 유지가 안정적
        val script = """tell application "Ghostty" to activate"""
        ProcessBuilder("/usr/bin/osascript", "-e", script)
            .start()
            .waitFor()
    }

    fun notifyError(project: Project?, message: String) {
        if (project == null) return
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GhosttyClaude")
            .createNotification(message, NotificationType.ERROR)
            .notify(project)
    }

    fun findGhosttyPath(): String? {
        return findBinary("ghostty", "/Applications/Ghostty.app/Contents/MacOS/ghostty")
    }

    fun findTmuxPath(): String? {
        return findBinary("tmux", "/opt/homebrew/bin/tmux", "/usr/local/bin/tmux")
    }

    private fun findBinary(name: String, vararg fallbackPaths: String): String? {
        // 1. Check PATH
        val whichResult = runCommandOutput("/usr/bin/which", name).trim()
        if (whichResult.isNotEmpty() && File(whichResult).exists()) {
            return whichResult
        }
        // 2. Check known locations
        for (path in fallbackPaths) {
            if (File(path).exists()) {
                return path
            }
        }
        return null
    }

    private fun runCommand(vararg command: String): Int {
        return try {
            ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()
                .waitFor()
        } catch (e: Exception) {
            -1
        }
    }

    private fun runCommandOutput(vararg command: String): String {
        return try {
            val process = ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        fun shellEscape(value: String): String {
            return "'" + value.replace("'", "'\\''") + "'"
        }
    }
}
