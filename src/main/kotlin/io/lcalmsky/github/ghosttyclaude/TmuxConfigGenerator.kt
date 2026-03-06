package io.lcalmsky.github.ghosttyclaude

object TmuxConfigGenerator {

    fun generate(config: GhosttyConfigReader.GhosttyConfig): List<String> {
        val commands = mutableListOf<String>()

        commands += "tmux set mouse on"

        if (config.copyOnSelect == "true" || config.copyOnSelect == "clipboard") {
            commands += """tmux bind -T copy-mode MouseDragEnd1Pane send-keys -X copy-pipe-and-cancel "pbcopy""""
            commands += """tmux bind -T copy-mode-vi MouseDragEnd1Pane send-keys -X copy-pipe-and-cancel "pbcopy""""
        }

        when (config.clipboardWrite) {
            "allow-always" -> commands += "tmux set set-clipboard on"
            "deny" -> commands += "tmux set set-clipboard off"
        }

        config.scrollbackLimit?.let {
            commands += "tmux set history-limit $it"
        }

        return commands
    }

    fun toShellString(config: GhosttyConfigReader.GhosttyConfig): String {
        val commands = generate(config)
        if (commands.isEmpty()) return ""
        return commands.joinToString(" && ") { "($it 2>/dev/null || true)" }
    }
}
