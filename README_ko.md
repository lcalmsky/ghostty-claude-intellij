# Ghostty Claude

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-blue?logo=jetbrains)](https://plugins.jetbrains.com/)
[![macOS](https://img.shields.io/badge/platform-macOS-lightgrey?logo=apple)](https://www.apple.com/macos/)
[![Ghostty](https://img.shields.io/badge/terminal-Ghostty-blueviolet)](https://ghostty.org/)
[![Claude Code](https://img.shields.io/badge/AI-Claude%20Code-orange)](https://docs.anthropic.com/en/docs/claude-code)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

[English](README.md)

IntelliJ에서 단축키 하나로 [Ghostty](https://ghostty.org/) 터미널에 [Claude Code](https://docs.anthropic.com/en/docs/claude-code)를 실행하고, 에디터의 파일 컨텍스트를 전달하는 플러그인입니다.

![Demo](docs/demo.gif)

## 주요 기능

- **단축키 하나** — `Cmd+Opt+K` (macOS) / `Ctrl+Alt+K`로 실행 또는 컨텍스트 전달
- **세션 관리** — 첫 입력 시 Ghostty + Claude Code 실행, 이후 입력 시 기존 세션에 파일 컨텍스트 전달
- **선택 범위 전달** — 커서 위치는 `@file:line`, 선택 블록은 `@file:start-end` 형태로 전달
- **프로젝트별 독립 세션** — IntelliJ 프로젝트 창마다 독립된 Ghostty + Claude Code 세션 생성
- **세션 복구** — Ghostty 창이 닫혀도 tmux 세션이 살아있으면 자동으로 재연결
- **Tmux 설정 명령어** — 세션 시작 시 실행할 tmux 명령어를 자유롭게 설정 (마우스, 클립보드, 스크롤백 등)
- **설정 가능** — `--dangerously-skip-permissions`, `--verbose`, 커스텀 CLI 인수 등 Settings에서 변경 가능

## 동작 방식

```
첫 번째 Cmd+Opt+K  →  Ghostty 열림  →  프로젝트 디렉토리에서 Claude Code 시작
두 번째 Cmd+Opt+K  →  기존 Claude Code 세션에 @file:line 전달  →  Ghostty 활성화
```

플러그인은 프로젝트마다 [tmux](https://github.com/tmux/tmux) 세션을 생성하여 파일 컨텍스트를 올바른 Claude Code 인스턴스로 전달합니다.

## 요구 사항

> **플랫폼**: macOS 전용. Ghostty와 tmux 경로 처리가 macOS 전용(`/Applications/Ghostty.app`, `/opt/homebrew/bin/tmux`)입니다. Linux는 아직 지원하지 않습니다.

| 의존성 | 설치 방법 |
|--------|----------|
| macOS | 필수 |
| [Ghostty](https://ghostty.org/) | ghostty.org에서 다운로드 |
| [Claude Code](https://docs.anthropic.com/en/docs/claude-code) | `npm install -g @anthropic-ai/claude-code` |
| [tmux](https://github.com/tmux/tmux) | `brew install tmux` |
| IntelliJ IDEA | 2024.3 이상 |

## 설치

1. IntelliJ IDEA → Settings → Plugins → Marketplace
2. **"Ghostty Claude"** 검색
3. **Install** 클릭 → 재시작

## 사용법

### Claude Code 실행

1. IntelliJ 어디서든 `Cmd+Opt+K` 입력 (에디터, 프로젝트 트리, 터미널 등)
2. Ghostty 창이 열리며 프로젝트 디렉토리에서 Claude Code 실행

### 파일 컨텍스트 전달

1. 에디터에서 커서를 놓거나 코드 블록을 선택
2. `Cmd+Opt+K` 입력
3. Claude Code 입력창에 `@/path/to/File.kt:42`가 나타남 — 질문을 입력하고 Enter

> 에디터 밖(프로젝트 트리, 도구 창 등)에서 입력하면 파일 컨텍스트 없이 Ghostty 창만 열거나 포커스합니다.

### 멀티 프로젝트

IntelliJ 프로젝트 창마다 독립된 Ghostty 세션이 생성됩니다. git worktree를 사용해 여러 IntelliJ 창을 열면, 각 창이 독립된 Claude Code 인스턴스와 통신합니다.

## 설정

**Settings → Tools → Ghostty Claude**

![Settings](docs/settings.png)

| 옵션 | 설명 |
|------|------|
| Skip permission prompts | `--dangerously-skip-permissions` 옵션으로 Claude Code 실행 |
| Verbose output | `--verbose` 옵션으로 Claude Code 실행 |
| Additional arguments | 커스텀 CLI 인수 (예: `--model sonnet`) |
| Window position | Ghostty 창 위치 설정 (좌/우 절반, 1/3 등) |
| Auto-focus Ghostty | 컨텍스트 전달 후 자동으로 Ghostty 창으로 전환 |
| Tmux setup commands | 새 tmux 세션 시작 시 실행할 tmux 명령어 (한 줄에 하나, 주석 지원) |

> 참고: 실행 옵션 변경은 새 세션에만 적용됩니다. 기존 Ghostty 창을 닫고 단축키를 다시 눌러야 변경된 옵션으로 시작됩니다.

### Tmux 설정 명령어 예시

"Setup commands" 필드에 여러 줄의 tmux 명령어를 입력합니다. 한 줄에 하나씩, 빈 줄은 무시되고 `#`으로 시작하는 줄은 주석으로 처리됩니다. 새 tmux 세션 생성 시 한 번 실행됩니다.

**기본 설정:**
```
tmux set mouse on
tmux set history-limit 50000
tmux set-window-option mode-keys vi
tmux set escape-time 0
```

기본값은 tmux 마우스 지원을 활성화하여 스크롤과 선택을 지원합니다. Ghostty의 기본 복사/붙여넣기(`Cmd+C`)를 사용하려면 해당 줄을 `tmux set mouse off`로 변경하세요.

**다른 예시들:**

| 용도 | 명령어 |
|------|--------|
| 마우스 비활성화 (Ghostty Cmd+C 사용) | `tmux set mouse off` |
| 마우스 + vi 복사 모드 | `tmux set mouse on`<br>`tmux set-window-option mode-keys vi` |
| 주석 포함 | `# 마우스로 선택 활성화`<br>`tmux set mouse on` |
| 스크롤백 용량 증가 | `tmux set history-limit 100000` |

## 권장 설정

### Ghostty — 창 크기

플러그인은 Ghostty 설정 파일(`~/.config/ghostty/config`)을 읽어 창 크기를 정확히 계산합니다. 다음 설정이 사용됩니다:

```
font-family = JetBrainsMono Nerd Font Mono
font-size = 14
window-padding-x = 8
window-padding-y = 4
```

폰트나 크기를 변경하면 창 크기가 자동으로 조정됩니다 — 플러그인 재설정 불필요.

### IntelliJ — Ghostty 터미널 호환성

Ghostty 터미널에서 `idea .`로 IntelliJ를 실행하면 `TERM=xterm-ghostty` 환경 변수로 인해 오류가 발생할 수 있습니다. `~/.zshrc`에 다음 alias를 추가하세요:

```bash
alias idea='TERM=xterm-256color idea'
```

IntelliJ에만 TERM 변수를 덮어쓰고, 일반 셸에서는 Ghostty 터미널 기능을 그대로 유지합니다.

## 라이선스

MIT — 자세한 내용은 [LICENSE](LICENSE)를 참조하세요.
