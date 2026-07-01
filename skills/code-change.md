# 코드 수정

- 단계를 제약 사항에 맞추어 실행.

## 실행 단계

1. 계획 또는 지시한 내용에 따라 코드 구현.
2. android, kotlin 가이드 준수 여부 확인.
3. 빌드 가능 여부 확인.
4. 검증 단계 실행.
5. 수정한 파일에 한정해 git add 진행.
6. `git log -n 5 --oneline`로 이전 commit 포맷 확인, commit message 결정.
7. commit 진행.
8. 사용자 의사결정으로 기존 계획(`plan` 디렉토리)에 변경이 있었다면 기록 단계 실행

## 기록 단계

1. 기록 작성 규칙 `history/README.md` 확인.
2. 기록.
3. commit 진행.

## 검증 단계

1. `./gradlew ktlintFormat`
2. `./gradlew check`

## 제약 사항

- android 가이드와 kotlin 가이드를 준수해야 함.
	- android-cli 스킬 활용. android 가이드 확인 후 계획에 반영
	- kotlin Documentation(https://kotlinlang.org/docs/coding-conventions.html) 준수
- 코드, 주석, git commit message를 포함한 모든 텍스트는 간결하고 이해하기 쉽게 작성. 필요하지 않은 부차적인 내용과 미사여구 삭제.
- 파일 수정 도구(`write_to_file`, `replace_file_content`, `multi_replace_file_content`, `Write`, `Edit` 등) 호출 전 변경 계획과 실제 코드(markdown)를 채팅 텍스트로 반드시 출력
- 각 파일마다 수정 계획과 코드 변경 사항 출력(다른 내용을 +- 형태로 알기 쉽게 표시) 후에 사용자 피드백(승인, 수정 지시)을 받아야 파일 수정 도구를 호출 가능
- 사용자의 수정 인가는 설명 받은 내용과 변경 사항에 한정되며, 다른 파일에 추가 수정이 필요한 경우 중단하고 사용자에 내용 출력.
