# Ghostty Claude

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-blue?logo=jetbrains)](https://plugins.jetbrains.com/)
[![macOS](https://img.shields.io/badge/platform-macOS-lightgrey?logo=apple)](https://www.apple.com/macos/)
[![Ghostty](https://img.shields.io/badge/terminal-Ghostty-blueviolet)](https://ghostty.org/)
[![Claude Code](https://img.shields.io/badge/AI-Claude%20Code-orange)](https://docs.anthropic.com/en/docs/claude-code)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

[한국어](README_ko.md)

IntelliJ plugin that launches [Claude Code](https://docs.anthropic.com/en/docs/claude-code) in [Ghostty](https://ghostty.org/) terminal and sends file context from the editor with a single keyboard shortcut.

![Demo](docs/demo.gif)

## Features

- **One shortcut** — `Cmd+Opt+K` (macOS) / `Ctrl+Alt+K` to launch or send context
- **Smart session management** — first press launches Ghostty + Claude Code, subsequent presses send file context to the existing session without opening a new window
- **Selection range** — sends `@file:line` for cursor position, `@file:start-end` for selected block
- **Worktree isolation** — each IntelliJ project window gets its own independent Ghostty + Claude Code session
- **Session recovery** — automatically re-attaches if the Ghostty window was closed but the tmux session is still alive
- **Tmux setup commands** — run custom tmux commands (mouse, clipboard, scrollback, etc.) when a session starts
- **Configurable** — toggle `--dangerously-skip-permissions`, `--verbose`, or add custom CLI arguments from Settings

## How It Works

```
1st Cmd+Opt+K  →  Ghostty opens  →  Claude Code starts in project directory
2nd Cmd+Opt+K  →  @file:line sent to existing Claude Code session  →  Ghostty activates
```

The plugin creates a [tmux](https://github.com/tmux/tmux) session per project to reliably route file context to the correct Claude Code instance.

## Requirements

> **Platform**: macOS only. Ghostty and tmux path resolution is macOS-specific (`/Applications/Ghostty.app`, `/opt/homebrew/bin/tmux`). Linux support is not yet available.

| Dependency | Install |
|------------|---------|
| macOS | Required |
| [Ghostty](https://ghostty.org/) | Download from ghostty.org |
| [Claude Code](https://docs.anthropic.com/en/docs/claude-code) | `npm install -g @anthropic-ai/claude-code` |
| [tmux](https://github.com/tmux/tmux) | `brew install tmux` |
| IntelliJ IDEA | 2024.3 or later |

## Installation

1. IntelliJ IDEA → Settings → Plugins → Marketplace
2. Search for **"Ghostty Claude"**
3. Click **Install** → Restart

## Usage

### Launch Claude Code

1. Press `Cmd+Opt+K` from anywhere in IntelliJ (editor, project tree, terminal, etc.)
2. A Ghostty window opens with Claude Code running in the project directory

### Send File Context

1. Place your cursor on a line (or select a block of code) in the editor
2. Press `Cmd+Opt+K`
3. `@/path/to/File.kt:42` appears in Claude Code's input — type your question and press Enter

> When pressed outside the editor (e.g. project tree, tool windows), the shortcut opens or focuses the Ghostty window without sending file context.

### Multiple Projects

Each IntelliJ project window gets its own Ghostty session. When using git worktrees with separate IntelliJ windows, each window creates and communicates with its own independent Claude Code instance.

## Settings

**Settings → Tools → Ghostty Claude**

![Settings](docs/settings.png)

| Option | Description |
|--------|-------------|
| Skip permission prompts | Launches Claude Code with `--dangerously-skip-permissions` |
| Verbose output | Launches Claude Code with `--verbose` |
| Additional arguments | Custom CLI arguments (e.g. `--model sonnet`) |
| Window position | Controls where the Ghostty window appears (Left/Right half, thirds, etc.) |
| Auto-focus Ghostty | Automatically switch to Ghostty window after sending context |
| Tmux setup commands | Multi-line tmux commands to run when a new tmux session starts (one per line, supports comments) |

> Note: Changing launch options only takes effect for new sessions. Close the existing Ghostty window and press the shortcut again to start a new session with updated options.

### Tmux Setup Commands Examples

The "Setup commands" field accepts multiple tmux commands, one per line. Lines starting with `#` are treated as comments and ignored. These commands run once when a new tmux session is created.

**Default configuration:**
```
tmux set mouse on
tmux set history-limit 50000
tmux set-window-option mode-keys vi
tmux set escape-time 0
```

The default enables tmux mouse support for scroll and selection. To use Ghostty's native copy/paste with `Cmd+C` instead, change the command to `tmux set mouse off`.

**Other examples:**

| Use case | Commands |
|----------|----------|
| Disable mouse (use Ghostty Cmd+C) | `tmux set mouse off` |
| Mouse + vi copy-mode | `tmux set mouse on`<br>`tmux set-window-option mode-keys vi` |
| With comments | `# Enable mouse for selection`<br>`tmux set mouse on` |
| Larger scrollback | `tmux set history-limit 100000` |

## Recommended Configuration

### Ghostty — Window Sizing

The plugin reads your Ghostty config (`~/.config/ghostty/config`) to calculate window size accurately. The following settings are used:

```
font-family = JetBrainsMono Nerd Font Mono
font-size = 14
window-padding-x = 8
window-padding-y = 4
```

If you change your font or font size, the window sizing adjusts automatically — no plugin reconfiguration needed.

### IntelliJ — Ghostty Terminal Compatibility

When launching IntelliJ from a Ghostty terminal via `idea .`, IntelliJ may show errors due to the `TERM=xterm-ghostty` environment variable. Add this alias to your `~/.zshrc`:

```bash
alias idea='TERM=xterm-256color idea'
```

This overrides the TERM variable only for IntelliJ while preserving Ghostty's terminal features for normal shell usage.

## License

MIT — see [LICENSE](LICENSE) for details.
