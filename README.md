# Ghostty Claude

IntelliJ plugin to open [Ghostty](https://ghostty.org/) terminal with [Claude Code](https://claude.ai/claude-code), sending current file and selection context via keyboard shortcut.

## Features

- **One shortcut** (`Cmd+Opt+K` / `Ctrl+Alt+K`) to launch Claude Code in Ghostty
- **Smart session management** — first press opens a new Ghostty window, subsequent presses send context to the existing session
- **Selection awareness** — sends cursor line or selected line range as `@file:line` reference
- **Worktree isolation** — each IntelliJ project window gets its own independent Ghostty + Claude Code session
- **Session recovery** — if the Ghostty window is closed, the next shortcut press re-attaches to the existing tmux session

## How It Works

```
1st press:  IntelliJ → Ghostty window opens → Claude Code starts → @file:line sent
2nd press:  IntelliJ → existing Claude Code receives @file:line (no new window)
```

The plugin uses [tmux](https://github.com/tmux/tmux) sessions to reliably deliver file context to the correct Claude Code instance, even across multiple projects.

## Requirements

- [Ghostty](https://ghostty.org/) terminal
- [Claude Code](https://claude.ai/claude-code) CLI
- [tmux](https://github.com/tmux/tmux) — `brew install tmux`
- IntelliJ IDEA 2024.3+

## Installation

### From Disk

1. Download the latest release zip from [Releases](https://github.com/lcalmsky/ghostty-claude-intellij/releases)
2. IntelliJ IDEA → Settings → Plugins → ⚙️ → Install Plugin from Disk
3. Select the downloaded zip file
4. Restart IntelliJ IDEA

### Build from Source

```bash
git clone https://github.com/lcalmsky/ghostty-claude-intellij.git
cd ghostty-claude-intellij
./gradlew buildPlugin
```

The plugin zip will be at `build/distributions/ghostty-claude-intellij-<version>.zip`.

## Usage

1. Open any file in the IntelliJ editor
2. Press `Cmd+Opt+K` (macOS) or `Ctrl+Alt+K` (Windows/Linux)
3. Ghostty opens with Claude Code — the current file reference appears in Claude Code's input
4. Type your question after the file reference and press Enter

### With Selection

1. Select a block of code in the editor
2. Press `Cmd+Opt+K`
3. Claude Code receives `@/path/to/file.kt:10-25` (the selected line range)

### Multiple Projects (Worktrees)

Each IntelliJ project window maintains its own Ghostty + Claude Code session. Opening the shortcut from different projects will create separate Ghostty windows that don't interfere with each other.

## License

MIT License — see [LICENSE](LICENSE) for details.
