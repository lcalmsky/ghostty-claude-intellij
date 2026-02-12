package io.lcalmsky.github.ghosttyclaude

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OpenWithClaudeAction : AnAction() {

    private val executor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "ghostty-claude-launcher").apply { isDaemon = true }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)

        val manager = ClaudeSessionManager()

        val ghosttyPath = manager.findGhosttyPath()
        if (ghosttyPath == null) {
            manager.notifyError(project, "Ghostty not found. Install Ghostty or add it to PATH.")
            return
        }

        val sessionName = manager.getSessionName(projectPath)
        val fileRef = if (editor != null && virtualFile != null) buildFileRef(editor, virtualFile) else null
        val autoFocus = GhosttyClaudeSettings.getInstance().state.autoFocusGhostty

        executor.submit {
            try {
                when {
                    !manager.sessionExists(sessionName) -> {
                        manager.launchNewSession(sessionName, projectPath)
                    }
                    !manager.isSessionAttached(sessionName) -> {
                        manager.reattachSession(sessionName)
                        if (fileRef != null) {
                            executor.schedule({
                                manager.sendKeys(sessionName, fileRef)
                            }, 500, TimeUnit.MILLISECONDS)
                        }
                    }
                    else -> {
                        if (fileRef != null) {
                            manager.sendKeys(sessionName, fileRef)
                            if (autoFocus) {
                                Thread.sleep(200)
                                manager.activateGhostty()
                            }
                        } else {
                            manager.activateGhostty()
                        }
                    }
                }
            } catch (ex: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    manager.notifyError(project, "Failed to launch Claude: ${ex.message}")
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    companion object {
        fun buildFileRef(editor: Editor, file: VirtualFile): String {
            val filePath = file.path
            val selectionModel = editor.selectionModel

            return if (selectionModel.hasSelection()) {
                val startLine = editor.document.getLineNumber(selectionModel.selectionStart) + 1
                val endLine = editor.document.getLineNumber(selectionModel.selectionEnd) + 1
                "@$filePath:$startLine-$endLine "
            } else {
                val line = editor.caretModel.logicalPosition.line + 1
                "@$filePath:$line "
            }
        }
    }
}
