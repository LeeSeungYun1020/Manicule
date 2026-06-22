참고 파일
- plan/plan.md 기획
- plan/structure.md 모듈과 파일 구조
- plan/order.md 작업 순서, PR로 리뷰 가능한 수준으로 단계를 더 쪼개어서 구현 필요
- plan/current.md 작업한 내용, 다음 작업 내용. 작업 시 plan/current.md 파일 업데이트하여 다음 세션에서 활용
- history/ 커밋별 의사결정 기록 (작성 규칙: history/README.md)

규칙
- 응답은 한국어.
- 코드, 주석, git commit message를 포함한 모든 파일은 간결하고 이해하기 쉽게 작성. 필요하지 않은, 부차적인 내용과 미사여구 삽입하지 않음.
- 작업 후: `./gradlew ktlintFormat` → `check` 통과 → commit → 의사결정 있었다면 history 추가 → current.md 업데이트
- commit message 작성 시: 이전 포맷 확인.
- PR 코드 리뷰 작성 시: 불필요한 전체 요약 코멘트 작성 금지. 반드시 분리된 개별 코멘트로 작성.

Gemini 모델 필수 지침

- 파일 수정 도구(`write_to_file`, `replace_file_content`, `multi_replace_file_content`) 호출 전 변경 계획과 실제 코드(markdown)를 채팅 텍스트로 반드시 출력
- 각 파일마다 수정 계획과 코드 변경 사항 출력(다른 내용을 +- 형태로 알기 쉽게 표시) 후에 사용자 피드백(승인, 수정 지시)을 받아야 파일 수정 도구를 호출 가능
- 사용자의 수정 인가는 설명 받은 내용과 변경 사항에 한정됨
- 절대로 파일 수정 도구를 허가 없이 임의 호출하지 않음
