# Ghostty Claude IntelliJ Plugin

## 프로젝트 구조

- `src/main/kotlin/.../` - 플러그인 소스
- `src/test/kotlin/.../` - 테스트
- `src/main/resources/META-INF/plugin.xml` - 플러그인 메타데이터 (버전, 변경 내역)
- `README.md` / `README_ko.md` - 영문/한국어 README

## 문서 동기화 규칙

기능 추가/변경/삭제 시 아래 파일들을 반드시 함께 업데이트할 것:

1. `README.md` - 영문 README (Features, Settings 테이블, 예시)
2. `README_ko.md` - 한국어 README (README.md와 동일 구조 유지)
3. `src/main/resources/META-INF/plugin.xml` - change-notes 섹션

세 파일의 기능 설명이 서로 일치해야 한다.

## 빌드 & 테스트

```bash
./gradlew test        # 테스트 실행
./gradlew runIde      # 플러그인 포함 IntelliJ 실행 (수동 확인용)
```
