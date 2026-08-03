# Manicule — 진행 현황

> 참고: [plan.md](plan.md) 기획 · [structure.md](structure.md) 모듈/파일 구조 · [order.md](order.md) 선행 계약/버티컬 레인 · [history/](../history/README.md) 커밋별 의사결정
> 새 세션은 **"다음 실행 가능 작업"**부터 진행.

## 완료

- Foundation / Core Engine과 디자인 시스템 기반
- 디자인 시스템 토큰, 테마 배선, 공통 컴포넌트 토큰화
- `ReadingCalendar` UI 명칭과 그리드 터치·날짜별 툴팁
- 최근 검색어 조회·저장·전체 삭제·개별 삭제 Repository와 UseCase
- 프로토타입의 중앙 정렬 앱바·홈 아바타 제외·서재 정렬 캡션 위치·카메라 권한 CTA 확정 편차 반영

## 선행 계약

| 레인 | 범위 | `depends_on` | 상태 |
|---|---|---|---|
| C1 | ReadingRecord 시간·시작/끝 페이지 모델, Entity/Converter, `currentPage` 쿼리, Mapper/Repository 명칭 | 없음 | ⏳ 실행 가능 |
| C2 | 모듈 스캐폴딩, Navigation route 타입, 공통 Composable·크기·Preview 계약 | 없음 | ⏳ 실행 가능 |

## 버티컬 레인

| 레인 | 기능 | `depends_on` | 상태 | 다음 범위 |
|---|---|---|---|---|
| V1 | 검색 | C2 | ⏳ 대기 | 기존 검색 Data/Domain → 공통 검색 컴포넌트 → 화면/ViewModel → Paging·Undo 테스트 |
| V2 | 스캔 | C2 | ⏳ 대기 | Scanner core → 조회 UseCase → 권한·인식 UI → 성공/실패/회전 테스트 |
| V3 | 설정 | C2 | ⏳ 대기 | Notifications → 알림 UseCase → 설정 UI → 스케줄·테마 테스트 |
| V4 | 책 상세 | C1, C2 | ⏳ 대기 | 기록·상태 Repository/UseCase → 상세/내 기록 UI → CRUD·자동 상태 전환 테스트 |
| V5 | 서재 | C1, C2; 필요 시 V4 공용 상태 API | ⏳ 대기 | 정렬·상태 쿼리/UseCase → 서재 UI → 정렬·삭제·Undo 테스트 |
| V6 | 홈 | C1, C2; V4 기록 API; V5 서재 조회 계약 | ⏳ 대기 | 집계 계약 → 홈 UseCase/UI → 첫 사용자·읽는 중 없음·요약 이동 테스트 |
| V7 | 통계 | C1, C2; V4 기록 API | ⏳ 대기 | 기간 집계 → 통계 UseCase/UI → 기간·날짜·스크롤 테스트 |

## 다음 실행 가능 작업

1. C1과 C2를 독립 PR로 동시에 진행한다.
2. C2가 머지되면 V1·V2·V3를 즉시 시작한다. C1 완료를 기다리지 않는다.
3. C1과 C2가 모두 머지되면 V4를 시작하고, 각 레인은 추가 `depends_on`이 충족되는 즉시 시작한다.

세부 작업과 공용 API 소유 규칙은 [order.md](order.md)를 따른다. UI 착수 전에는 [ui-ux-guidelines.md](prototype/ui-ux-guidelines.md)의 확정 편차, 색상 기준과 컴포넌트 커버리지를 확인한다.

## 병렬 작업 운영

- 작업 레인은 PR과 1:1이 아니다. 한 레인에서 사용자 행동별로 여러 리뷰 가능한 PR을 순차 생성한다.
- 각 PR은 빌드 가능해야 하며, 변경한 행동과 실패 경로의 테스트를 포함한다. 독립 검증 가능한 공용 계약·플랫폼 기반만 별도 기반 PR로 허용한다.
- 문자열·아이콘·Gradle·이 진행 문서의 단순 추가가 겹치는 것은 허용한다.
- 동일 기능 중복 구현과 기존 공용 API 의미의 동시 변경은 금지한다.
- 여러 작업자가 같은 파일에 항목을 추가해 생긴 단순 병합 충돌은 최종 머지자가 최신 `main` 기준으로 정리한다.
- 공용 API 변경을 기다리는 레인은 해당 소유 PR 머지 후 rebase한다.

### 모듈 추가 절차

1. `settings.gradle.kts`에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts`(convention plugin alias).
3. Android library면 빈 `AndroidManifest.xml` 추가.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>` 사용.
5. `app/build.gradle.kts`의 `implementation(projects.…)` 주석 해제.

### 커밋 / history

의사결정 있는 커밋은 [history/](../history/README.md)에 `<short-hash>.md`를 추가한다. 순수 tooling/format 커밋에는 만들지 않는다.
