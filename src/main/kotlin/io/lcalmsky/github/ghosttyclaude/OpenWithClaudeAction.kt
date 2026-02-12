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
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val projectPath = project.basePath ?: return

        val manager = ClaudeSessionManager()

        val ghosttyPath = manager.findGhosttyPath()
        if (ghosttyPath == null) {
            manager.notifyError(project, "Ghostty not found. Install Ghostty or add it to PATH.")
            return
        }

        val sessionName = manager.getSessionName(projectPath)
        val fileRef = buildFileRef(editor, virtualFile)

        executor.submit {
            try {
                when {
                    !manager.sessionExists(sessionName) -> {
                        manager.launchNewSession(sessionName, projectPath)
                        executor.schedule({
                            manager.sendKeys(sessionName, fileRef)
                            manager.activateGhostty()
                        }, 3, TimeUnit.SECONDS)
                    }
                    !manager.isSessionAttached(sessionName) -> {
                        manager.reattachSession(sessionName)
                        executor.schedule({
                            manager.sendKeys(sessionName, fileRef)
                        }, 500, TimeUnit.MILLISECONDS)
                    }
                    else -> {
                        manager.sendKeys(sessionName, fileRef)
                        // IntelliJ Action 이벤트 처리 완료 후 포커스 전환되도록 딜레이
                        Thread.sleep(200)
                        manager.activateGhostty()
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
        e.presentation.isEnabledAndVisible =
            e.getData(CommonDataKeys.EDITOR) != null &&
            e.getData(CommonDataKeys.VIRTUAL_FILE) != null
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
