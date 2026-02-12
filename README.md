# Ghostty Claude

IntelliJ plugin that launches [Claude Code](https://docs.anthropic.com/en/docs/claude-code) in [Ghostty](https://ghostty.org/) terminal and sends file context from the editor with a single keyboard shortcut.

<!-- TODO: demo GIF here -->
<!-- ![Demo](docs/demo.gif) -->

## Features

- **One shortcut** — `Cmd+Opt+K` (macOS) / `Ctrl+Alt+K` to launch or send context
- **Smart session management** — first press launches Ghostty + Claude Code, subsequent presses send file context to the existing session without opening a new window
- **Selection range** — sends `@file:line` for cursor position, `@file:start-end` for selected block
- **Worktree isolation** — each IntelliJ project window gets its own independent Ghostty + Claude Code session
- **Session recovery** — automatically re-attaches if the Ghostty window was closed but the tmux session is still alive
- **Configurable** — toggle `--dangerously-skip-permissions`, `--verbose`, or add custom CLI arguments from Settings

## How It Works

```
1st Cmd+Opt+K  →  Ghostty opens  →  Claude Code starts in project directory
2nd Cmd+Opt+K  →  @file:line sent to existing Claude Code session  →  Ghostty activates
```

The plugin creates a [tmux](https://github.com/tmux/tmux) session per project to reliably route file context to the correct Claude Code instance.

## Requirements

| Dependency | Install |
|------------|---------|
| [Ghostty](https://ghostty.org/) | Download from ghostty.org |
| [Claude Code](https://docs.anthropic.com/en/docs/claude-code) | `npm install -g @anthropic-ai/claude-code` |
| [tmux](https://github.com/tmux/tmux) | `brew install tmux` |
| IntelliJ IDEA | 2024.3 or later |

## Installation

### From JetBrains Marketplace

<!-- TODO: uncomment after marketplace approval -->
<!-- 1. IntelliJ IDEA → Settings → Plugins → Marketplace -->
<!-- 2. Search for "Ghostty Claude" -->
<!-- 3. Click Install → Restart -->

### From Disk

1. Download the latest zip from [Releases](https://github.com/lcalmsky/ghostty-claude-intellij/releases)
2. IntelliJ IDEA → Settings → Plugins → ⚙ → **Install Plugin from Disk**
3. Select the zip file and restart

### Build from Source

```bash
git clone https://github.com/lcalmsky/ghostty-claude-intellij.git
cd ghostty-claude-intellij
./gradlew buildPlugin
```

Output: `build/distributions/ghostty-claude-intellij-<version>.zip`

## Usage

### Launch Claude Code

1. Open any file in the editor
2. Press `Cmd+Opt+K`
3. A Ghostty window opens with Claude Code running in the project directory

### Send File Context

1. Place your cursor on a line (or select a block of code)
2. Press `Cmd+Opt+K`
3. `@/path/to/File.kt:42` appears in Claude Code's input — type your question and press Enter

### Multiple Projects

Each IntelliJ project window gets its own Ghostty session. When using git worktrees with separate IntelliJ windows, each window creates and communicates with its own independent Claude Code instance.

## Settings

**Settings → Tools → Ghostty Claude**

<!-- TODO: settings screenshot here -->
<!-- ![Settings](docs/settings.png) -->

| Option | Description |
|--------|-------------|
| Skip permission prompts | Launches Claude Code with `--dangerously-skip-permissions` |
| Verbose output | Launches Claude Code with `--verbose` |
| Additional arguments | Custom CLI arguments (e.g. `--model sonnet`) |
| Auto-focus Ghostty | Automatically switch to Ghostty window after sending context |

> Note: Changing launch options only takes effect for new sessions. Close the existing Ghostty window and press the shortcut again to start a new session with updated options.

## License

MIT — see [LICENSE](LICENSE) for details.
