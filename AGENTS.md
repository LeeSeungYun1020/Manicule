# 참고 파일

- plan/plan.md 기획
- plan/structure.md 모듈과 파일 구조
- plan/order.md 작업 순서, PR로 리뷰 가능한 수준으로 단계를 더 쪼개어 구현 가능
- plan/current.md 레인별 진행 현황과 다음 범위. 해당 레인 상태가 바뀔 때만 갱신
- plan/prototype/ui-ux-guidelines.md UI/UX 개발 가이드라인. UI 구현 시 필수 확인
- history/ 커밋별 의사결정 기록 (작성 규칙: history/README.md)
- skills/skills.md 계획·구현·리뷰·반영·검수의 단계별 진입점. 요청한 단계의 절차를 준수하여 실행.

# 선택 읽기

- 계획 단계는 최신 `origin/main`의 `plan/current.md` 해당 레인, `plan/order.md`의 공통 규칙·해당 레인, 관련 요구사항·모듈 구조와 기존 코드를 확인한다. 의사결정을 확정한 `plan/tasks/<작업>.md`만 추가한다.
- 구현 단계는 작업 계획 파일과 연결된 요구사항·관련 코드를 우선 읽는다. 리뷰·검수는 PR 설명과 전체 계획 문서(`plan/plan.md`, `plan/structure.md`, `plan/order.md`, `plan/current.md`)의 관련 요구사항·경계 및 코드·테스트를 확인한다. 로컬 전달용 `plan/tasks/<작업>.md`는 요구하지 않는다. 같은 세션의 변경되지 않은 문서를 다시 읽지 않는다.
- 구조·공용 API 변경은 `plan/structure.md` §1·§2의 아키텍처·의존 그래프, §3.2의 feature 경계와 사용부까지 확인한다.
- UI 구현은 UI 가이드 §1~§6·§8의 공통 규칙·확정 편차, §7의 관련 컴포넌트와 `plan/prototype/prototype.html`의 해당 화면·변형을 확인한다.
- 문서·개발 도구 변경은 해당 지침과 참조 관계만 확인한다. 불변 조건·예외·의존성이 연결되면 범위를 넓히고, 전체 정합성 점검 시에는 관련 문서 전체를 읽는다.
- `rg -n '^#{1,4} ' <문서>`로 관련 절을 찾고 `rg --files <모듈>`로 실제 파일을 찾는다. 계획의 예정 구조와 현재 구현을 구분한다.
