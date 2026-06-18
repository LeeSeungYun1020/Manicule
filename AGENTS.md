참고 파일
- plan/plan.md 기획
- plan/structure.md 모듈과 파일 구조
- plan/order.md 작업 순서, PR로 리뷰 가능한 수준으로 단계를 더 쪼개어서 구현 필요
- plan/current.md 작업한 내용, 다음 작업 내용. 작업 시 plan/current.md 파일 업데이트하여 다음 세션에서 활용
- history/ 커밋별 의사결정 기록 (작성 규칙: history/README.md)

규칙
- 응답은 한국어.
- 코드, 주석, git commit message를 포함한 모든 파일은 간결하고 이해하기 쉽게 작성
- 작업 후: `./gradlew ktlintFormat` → `check` 통과 → commit → 의사결정 있었다면 history 추가 → current.md 업데이트
- PR 코드 리뷰 작성 시: 불필요한 전체 요약 코멘트 작성 금지. 반드시 분리된 개별 코멘트로 작성.
