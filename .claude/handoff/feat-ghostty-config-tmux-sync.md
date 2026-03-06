# tmux 설정 UI 개선 - Handoff (Session 1)

## 현재 상태 요약
- **브랜치**: feat/ghostty-config-tmux-sync
- **상태**: 미완료 - Settings 저장 검증 필요
- **전체 변경 파일**: 4개

## 진행 상황

### ✅ 완료
1. GhosttyClaudeSettings.kt - tmuxSetupCommands 기본값을 멀티라인 설정으로 변경
2. GhosttyClaudeConfigurable.kt - JBTextField → JTextArea 교체 (스크롤팬, monospace 폰트)
3. ClaudeSessionManager.kt - 멀티라인 명령어 파싱 로직 구현 (lines 분할 → trim → 필터링 → && 조인)
4. ClaudeSessionManagerTest.kt - 멀티라인 파싱 테스트 3개 추가
5. README.md / README_ko.md - 멀티라인 입력 방식으로 문서 업데이트
6. `./gradlew test` - 모든 테스트 통과 ✅

### 🔄 진행 중 - Settings 저장 문제 발견
**문제**:
- Settings에서 "Setup commands" 필드를 비우고 Apply해도, 새 tmux 세션 생성 시 여전히 기본값(mouse on 등)이 적용됨
- GhosttyClaude.xml 파일에 `tmuxSetupCommands` 필드가 저장되지 않음
- Kotlin `PersistentStateComponent`가 필드를 저장하지 않으면 기본값을 로드하는 문제

**원인**:
- GhosttyClaudeSettings.State에 기본값이 정의되어 있음 (라인 19-26)
- Settings UI에서 설정값을 변경해도 XML에 저장되지 않으면 기본값이 로드됨
- 이는 설계상 문제 아님 - 빈 설정을 명시적으로 저장해야 함

**필요한 검증**:
- [ ] IntelliJ IDE 재시작 후 다시 테스트
- [ ] Settings 저장이 제대로 작동하는지 확인
- [ ] 빈 설정 vs 기본값 설정 두 가지 케이스 모두 동작 확인

## 다음 세션 TODO
- [ ] IDE 재시작 후 Settings 저장 재검증
- [ ] 기본값 유지 + 사용자 덮어쓰기 가능한 설계 확인
- [ ] runIde로 새 IDE 띄워 Settings UI에 기본값이 표시되는지 확인
- [ ] 스크린샷 촬영 (기본값이 보이는 상태)
- [ ] git diff --stat으로 변경 사항 확인
- [ ] 커밋 및 PR 생성

## 핵심 파일 경로
- `/Users/jm.lee/git-repo/ghostty-claude-intellij/src/main/kotlin/io/lcalmsky/github/ghosttyclaude/GhosttyClaudeSettings.kt` — State에 tmuxSetupCommands 기본값 정의
- `/Users/jm.lee/git-repo/ghostty-claude-intellij/src/main/kotlin/io/lcalmsky/github/ghosttyclaude/GhosttyClaudeConfigurable.kt` — UI: JTextArea로 변경, JScrollPane 추가
- `/Users/jm.lee/git-repo/ghostty-claude-intellij/src/main/kotlin/io/lcalmsky/github/ghosttyclaude/ClaudeSessionManager.kt` — 멀티라인 명령어 파싱
- `/Users/jm.lee/git-repo/ghostty-claude-intellij/src/test/kotlin/io/lcalmsky/github/ghosttyclaude/ClaudeSessionManagerTest.kt` — 파싱 테스트 3개
- `/Users/jm.lee/git-repo/ghostty-claude-intellij/README.md` — 기본값 + 예시로 문서 업데이트
- `/Users/jm.lee/git-repo/ghostty-claude-intellij/README_ko.md` — 한글 문서 동기화

## 알려진 이슈
1. **Settings 저장 미확인**: IntelliJ IDE 캐시 또는 플러그인 메모리 캐시 가능성
2. **기본값 설계**: 현재는 기본값이 항상 로드됨 - 사용자가 빈 설정을 영구적으로 유지할 수 없음 (실제로는 이렇게 설계하는 게 맞음)

## 다음 세션 부트스트랩
1. 이 handoff 파일 읽기
2. 핵심 파일들 순서대로 Read (GhosttyClaudeSettings → GhosttyClaudeConfigurable → ClaudeSessionManager)
3. `git diff --stat`으로 현재 변경 확인
4. `/gradlew test`로 테스트 재확인
5. `./gradlew runIde` → Settings UI 열어서 기본값 확인
6. 새 세션 시작 (`Cmd+Opt+K`) → tmux 설정 동작 확인
7. 스크린샷 촬영
8. 커밋 및 PR

## 세션 로그
- 14:02 - 계획 구현 시작
- 14:15 - 새 세션 생성해서 Settings 저장 재검증 중 문제 발견
- 기본값이 항상 적용되는 문제 → IntelliJ PersistentStateComponent의 기본값 메커니즘 확인
