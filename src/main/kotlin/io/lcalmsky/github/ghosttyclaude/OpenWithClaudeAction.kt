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
        val autoFocus = GhosttyClaudeSettings.getInstance().state.autoFocusGhostty

        executor.submit {
            try {
                when {
                    !manager.sessionExists(sessionName) -> {
                        // 첫 실행: Claude Code만 시작 (컨텍스트 전달 안 함)
                        // 창 위치/크기는 Ghostty CLI args로 전달됨
                        manager.launchNewSession(sessionName, projectPath)
                    }
                    !manager.isSessionAttached(sessionName) -> {
                        // re-attach: 창 위치/크기는 Ghostty CLI args로 전달됨
                        manager.reattachSession(sessionName)
                        executor.schedule({
                            manager.sendKeys(sessionName, fileRef)
                        }, 500, TimeUnit.MILLISECONDS)
                    }
                    else -> {
                        manager.sendKeys(sessionName, fileRef)
                        if (autoFocus) {
                            Thread.sleep(200)
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
