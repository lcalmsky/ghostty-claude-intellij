package io.lcalmsky.github.ghosttyclaude

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "GhosttyClaudeSettings", storages = [Storage("GhosttyClaude.xml")])
class GhosttyClaudeSettings : PersistentStateComponent<GhosttyClaudeSettings.State> {

    data class State(
        var skipPermissions: Boolean = false,
        var verbose: Boolean = false,
        var autoFocusGhostty: Boolean = true,
        var additionalArgs: String = "",
        var windowPosition: String = "DEFAULT",
    )

    fun buildClaudeArgs(): String {
        val args = mutableListOf<String>()
        if (myState.skipPermissions) args += "--dangerously-skip-permissions"
        if (myState.verbose) args += "--verbose"
        if (myState.additionalArgs.isNotBlank()) args += myState.additionalArgs.trim()
        return args.joinToString(" ")
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): GhosttyClaudeSettings =
            ApplicationManager.getApplication().getService(GhosttyClaudeSettings::class.java)
    }
}
