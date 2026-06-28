# 참고 파일

- plan/plan.md 기획
- plan/structure.md 모듈과 파일 구조
- plan/order.md 작업 순서, PR로 리뷰 가능한 수준으로 단계를 더 쪼개어서 구현 필요
- plan/current.md 작업한 내용, 다음 작업 내용. 작업 시 plan/current.md 파일 업데이트하여 다음 세션에서 활용
- history/ 커밋별 의사결정 기록 (작성 규칙: history/README.md)

# 규칙

- 작업 전: 브랜치 전환. ai/{현재 작업} 형태.
- 모든 작업이 하나의 commit으로 들어가는게 아니라 작업 단위로 나누어 commit 진행. 해당 commit은 빌드 가능해야 함.
- 사용자 의사결정이 필요한 내용을 반드시 확인 후 코드 작성.
- android-cli 스킬 활용. android 가이드 확인 후 구현 진행.
- kotlin Documentation(https://kotlinlang.org/docs/coding-conventions.html) 확인.
- 코드, 주석, git commit message를 포함한 모든 파일은 간결하고 이해하기 쉽게 작성. 필요하지 않은, 부차적인 내용과 미사여구 삽입하지 않음.
- 작업 후: `./gradlew ktlintFormat` → `check` 통과 → commit → 사용자 의사결정 있었다면 history 추가 → current.md 업데이트
- commit message 작성 시: 이전 포맷 확인.
- PR 생성 시: 이전 PR 포맷 확인. 문서화 관련 내용은 포함하지 않음. LeeSeungYun1020 계정으로 생성(gh auth status 확인). Assignees는 LeeSeungYun1020. Reviewers는 lsy-auto.
- PR 코드 리뷰 작성 시: 불필요한 전체 요약 코멘트 작성 금지. 반드시 분리된 개별 코멘트로 작성. lsy-auto 계정으로 업로드.
- PR 리뷰 반영 시: 해당 코멘트에 수정 내용 또는 수정하지 않는 이유 간결히 추가. LeeSeungYun1020 계정으로 답글.
- PR 승인: 리뷰 해결된 경우 lsy-auto 계정으로 resolve 처리, 모든 리뷰 resolve 시 approve. lsy-auto 계정으로 승인.

# Gemini 모델 필수 지침

- 파일 수정 도구(`write_to_file`, `replace_file_content`, `multi_replace_file_content`) 호출 전 변경 계획과 실제 코드(markdown)를 채팅 텍스트로 반드시 출력
- 각 파일마다 수정 계획과 코드 변경 사항 출력(다른 내용을 +- 형태로 알기 쉽게 표시) 후에 사용자 피드백(승인, 수정 지시)을 받아야 파일 수정 도구를 호출 가능
- 사용자의 수정 인가는 설명 받은 내용과 변경 사항에 한정됨
- 절대로 파일 수정 도구를 허가 없이 임의 호출하지 않음
