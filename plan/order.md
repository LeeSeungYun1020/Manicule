# 독서 기록 앱 — 구현 순서

> [plan.md](plan.md)의 기능 요구사항과 [structure.md](structure.md)의 모듈 구조를 기반으로 작성한 구현 전략.
> 의미 충돌 위험이 큰 공용 계약만 먼저 확정하고, 이후에는 기능별로 Data → Domain → UI → 테스트를 완성한다.

---

## 1. 구현 원칙

| 원칙 | 의미 |
|---|---|
| **Contract First** | 공용 데이터 모델, 기존 쿼리의 의미, Navigation route와 공통 Composable 시그니처만 선행 계약에서 확정한다. |
| **Vertical Slicing** | 각 기능 레인은 Data → Domain → UI → 테스트까지 포함해 사용자 관점에서 실행 가능한 결과를 만든다. |
| **Offline-first** | Room을 SSOT로 두고 네트워크는 보조 수단으로 처리하며 UI는 Room `Flow`를 구독한다. |
| **Lane-level Start** | 레인은 자신의 `depends_on`이 충족되는 즉시 시작한다. 다른 독립 레인의 완료를 기다리지 않는다. |
| **Executable PR** | 큰 기능을 여러 PR로 나눌 때도 레이어별로 자르지 않고 실행 가능한 하위 사용자 흐름 단위로 나눈다. |
| **Test with Slice** | 단위·통합·UI 테스트를 별도 후속 단계로 미루지 않고 해당 레인에 포함한다. |

1단계 Foundation, 2단계 Core Engine과 디자인 시스템 기반은 완료되었다. 최근 검색어 조회·저장·전체 삭제·개별 삭제 Data/Domain 경로도 완료되어 검색 레인의 입력으로 사용한다.

---

## 2. 병렬 실행 규칙

### 2.1 시작과 병합

1. 각 레인은 아래 표의 `depends_on`만 확인한다. 조건이 충족되면 다른 레인 상태와 무관하게 착수한다.
2. `ManiculeIcons.kt`, `core:designsystem`의 `strings.xml`, Gradle 설정, [current.md](current.md)처럼 여러 레인이 단순 항목을 추가하는 파일은 공동 편집할 수 있다.
3. 기존 DAO에 서로 다른 신규 메서드를 추가하는 작업도 허용한다. 최종 머지자는 최신 `main` 기준으로 추가 항목과 import·정렬을 정리한다.
4. 같은 기능을 두 레인에서 중복 구현하거나 기존 공용 메서드의 의미와 공용 API 시그니처를 동시에 변경하지 않는다.
5. 공용 API 변경은 선행 계약 PR 또는 아래에 지정된 소유 레인 한 곳에서만 수행한다. 다른 레인은 해당 변경이 머지된 뒤 rebase한다.
6. `@Inject constructor`를 우선해 DI 모듈 공동 편집을 줄이되, 필요한 단순 바인딩 추가는 병렬 작업을 막는 이유로 삼지 않는다.

### 2.2 PR 분할 기준

작업 레인은 병렬 배정과 의존성 관리 단위이며 PR 단위가 아니다. 기능 규모에 따라 한 레인에서 여러 PR을 순차적으로 만든다.

| 기준 | 적용 |
|---|---|
| 리뷰 초점 | PR 하나에서 검토할 핵심 사용자 행동 또는 화면 상태를 1개로 제한한다. |
| 실행 가능성 | 매 PR은 빌드 가능하고 완료된 하위 흐름을 실제 화면 또는 명확한 테스트 진입점에서 확인할 수 있어야 한다. |
| 수직 연결 | 해당 행동에 필요한 Data/Domain/UI를 함께 연결한다. DAO 전체, UseCase 전체, UI 전체처럼 레이어만 일괄 구현하지 않는다. |
| 기반 PR 예외 | 공용 계약이나 Scanner·WorkManager처럼 독립적으로 검증 가능한 플랫폼 기반은 별도 PR로 허용한다. |
| 미구현 범위 | 후속 흐름은 명시적인 stub이나 비활성 경로로 남겨 현재 PR의 완료 범위를 분명히 한다. |
| 테스트 | 변경한 행동과 실패 경로의 테스트를 같은 PR에 포함한다. |

좋은 분할은 `최근 검색어 표시`, `검색 실행과 Paging 결과`, `기록 추가와 자동 상태 전환`처럼 하나의 행동을 끝까지 확인할 수 있다. 기능 전체를 한 PR에 넣거나 DAO·UseCase·화면을 각각 따로 완성하는 방식은 피한다.

---

## 3. 선행 계약

두 계약 레인은 서로 독립적이다. 각 버티컬 레인은 필요한 계약만 머지되면 시작한다.

### C1 — ReadingRecord와 공유 쿼리 계약

`depends_on`: 없음

| 계층 | 범위 |
|---|---|
| Model | `ReadingRecord`를 `id`, `isbn`, `date`, `time`, `startPage`, `endPage`로 확정하고 페이지 범위를 검증한다. `ContributionDay`는 `ReadingCalendarDay`로 명칭을 통일한다. |
| Database | Entity와 `LocalTime` Converter를 갱신한다. 하루 여러 기록을 허용하고 기록을 `date DESC, time DESC`로 조회한다. 현재 페이지는 `MAX(endPage)`로 계산한다. |
| Data/Domain | Mapper를 갱신하고 Repository API를 `getMaxEndPage` 의미로 통일한다. `GetContributionUseCase`를 `GetReadingCalendarUseCase`로 바꾼다. |
| UI contract | `ReadingCalendarGrid`와 Preview provider가 `ReadingCalendarDay`를 사용하도록 타입을 맞춘다. |
| 검증 | 모델·Mapper·Repository·DAO 테스트와 `BookEntryDao`의 `currentPage` 쿼리 테스트를 갱신한다. |

앱은 미출시 상태이며 보존할 개발 DB 데이터가 없다. Room `version = 1`을 유지하고 변경된 스키마 JSON만 재생성한다.

### C2 — 모듈·Navigation·공통 UI 계약

`depends_on`: 없음

| 영역 | 범위 |
|---|---|
| Module | `core:scanner`, `core:notifications`, 7개 `feature:*` 모듈을 개설하고 Gradle 의존성을 연결한다. |
| Navigation | `SearchRoute`, `ScannerRoute`, `BookDetailRoute(isbn)`, 상위 탭 route 등 타입과 `NavGraphBuilder` 확장 함수 시그니처를 확정하고 stub destination을 연결한다. |
| Common UI | 여러 레인이 공유하는 `ManiculeTopAppBar`, `ManiculeEmptyState`, `ManiculeLoading`, `BookCoverSize`, `BookProgressBar` 시그니처를 확정한다. |
| Preview | 공용 컴포넌트의 크기·상태별 Preview 계약과 `BookPreviewParameterProvider`를 준비한다. |
| 검증 | 모듈 의존 그래프, type-safe route 컴파일, 공용 컴포넌트 Preview와 기본 UI 테스트를 확인한다. |

공통 UI의 세부 요구는 [UI/UX 가이드](prototype/ui-ux-guidelines.md)의 확정 편차와 컴포넌트 커버리지 표를 따른다.

권장 PR 순서:

1. 모듈 스캐폴딩과 type-safe route stub
2. 여러 기능이 공유하는 Composable 시그니처·크기·Preview 계약

---

## 4. 버티컬 레인

### V1 — 검색

`depends_on`: C2

| 계층 | 범위 |
|---|---|
| Data | 완료된 최근 검색어 Repository 경로와 기존 도서 검색 Paging 데이터 경로를 사용한다. 공개 검색 계약은 `Flow<PagingData<Book>>`로 유지한다. |
| Domain | 완료된 최근 검색어 조회·저장·전체 삭제·개별 삭제 UseCase와 `SearchBooksUseCase`를 화면 흐름에 연결한다. |
| UI | `ManiculeSearchBar`, `ManiculeSnackbarHost`, `ManiculeSectionHeader`, 검색 화면/ViewModel을 구현한다. 350ms 디바운스, 입력 중 최근 검색어 로컬 필터, 빈 상태, Paging 목록, 스캔 이동을 포함한다. |
| 검증 | Paging append/오류/빈 결과, 최근 검색어 개별 삭제와 Undo, 전체 삭제, 입력 필터, ViewModel 및 Compose UI 테스트를 작성한다. |

권장 PR 순서:

1. 검색 화면 진입과 최근 검색어·빈 상태 표시
2. 입력 디바운스·로컬 필터·검색 실행
3. Paging 결과·append 상태·빈 결과의 스캔 이동
4. 최근 검색어 개별/전체 삭제와 Undo

### V2 — 스캔

`depends_on`: C2

| 계층 | 범위 |
|---|---|
| Data | `core:scanner`에 CameraX ImageAnalysis, ML Kit 바코드 인식, ISBN-10/13 검증과 `ScanResult`를 구현한다. |
| Domain | 스캔한 ISBN으로 책을 조회하는 UseCase를 구현하고 기존 Book Repository에 연결한다. |
| UI | 권한 요청·거부, 카메라 인식, 성공 시 책 상세 이동, 실패 시 검색 이동 화면과 ViewModel을 구현한다. |
| 검증 | ISBN 체크섬, 성공/실패, 권한 상태, 중복 인식 억제, 회전 시 `targetRotation` 갱신 테스트를 작성한다. |

권장 PR 순서:

1. Scanner core와 ISBN 검증(독립 플랫폼 기반 PR)
2. 카메라 권한과 Preview·회전 처리
3. ISBN 인식·도서 조회·책 상세 이동과 실패 시 검색 이동

### V3 — 설정

`depends_on`: C2

| 계층 | 범위 |
|---|---|
| Data | 기존 UserPreferences Repository를 사용해 리마인더 설정과 발송 시점 콘텐츠 조회를 지원한다. |
| Domain | `ReminderScheduler` 계약, 리마인더 on/off·시간 변경 UseCase, 최근 읽는 중 책 제목 또는 기본 메시지를 만드는 `GetReminderContentUseCase`를 구현한다. |
| Platform | `core:notifications`에 도메인 Scheduler 구현, 알림 채널과 Worker를 구현한다. Worker는 `GetReminderContentUseCase`를 호출하며 `core:data` Repository를 직접 주입하지 않는다. |
| UI | 테마 선택, 알림 토글·시간 선택, 라이선스·앱 버전 화면과 ViewModel을 구현한다. |
| 검증 | 스케줄 등록·취소·재설정, 읽는 중 책 제목/fallback 메시지, 테마 즉시 적용과 설정 UI 테스트를 작성한다. |

권장 PR 순서:

1. Domain 알림 계약과 WorkManager Scheduler·Worker(독립 플랫폼 기반 PR)
2. 리마인더 토글·시간 설정 UI와 예약/취소 연결
3. 컨텍스트 메시지와 테마·라이선스·버전 흐름

### V4 — 책 상세

`depends_on`: C1, C2

이 레인이 독서 기록 CRUD 및 상태 변경에 필요한 기존 Repository/API 의미 확장의 소유자다.

| 계층 | 범위 |
|---|---|
| Data | ReadingRecord CRUD·책별 관찰과 BookEntry 상태·별점·메모 저장 Repository를 완성한다. 소개·목차 URL 조회도 연결한다. |
| Domain | 기록 추가·수정·삭제·관찰, 상태 변경, 리뷰 저장 UseCase를 구현한다. 첫 기록의 `WANT → READING` 전환과 남은 페이지 10% 또는 40쪽 이하 신호를 처리한다. |
| UI | 책 정보/내 기록 탭, 상태·별점·메모 인라인 편집, 기록 시트, 진행률, 삭제 Undo, 완독 확인 다이얼로그를 구현한다. |
| 검증 | CRUD, 기본 탭, 자동 상태 전환, 완독 날짜, 리뷰 저장, Undo, 빈 기록과 재독 흐름 테스트를 작성한다. |

권장 PR 순서:

1. 책 정보 조회와 기본 탭 결정
2. 독서 상태 변경
3. 별점·메모 인라인 저장
4. 독서 기록 목록과 기록 추가
5. 기록 수정·삭제와 Undo
6. 첫 기록 자동 전환과 완독 확인

### V5 — 서재

`depends_on`: C1, C2; V4의 공용 상태 변경 API가 필요하면 해당 API 머지

이 레인이 서재 정렬 공개 계약의 소유자다.

| 계층 | 범위 |
|---|---|
| Data | 상태별 목록과 추가일·수정일·별점 정렬 쿼리 및 Repository를 구현한다. 별점 동률은 수정일 최신순, 별점 없음은 0점으로 처리한다. |
| Domain | `GetLibraryBooksUseCase(status, sort)`, 삭제와 상태 변경 흐름을 연결한다. |
| UI | 상태 탭, 정렬 시트와 현재 정렬 캡션, 진행률/완독일 오버레이, 롱프레스 삭제·상태 변경과 Undo를 구현한다. |
| 검증 | 상태 필터, 모든 정렬 방향·동률, 삭제·상태 변경·Undo, 빈 서재 UI 테스트를 작성한다. |

권장 PR 순서:

1. 상태별 목록과 빈 서재
2. 정렬 계약·시트·현재 정렬 표시
3. 진행률/완독일 오버레이와 롱프레스 변경·삭제·Undo

### V6 — 홈

`depends_on`: C1, C2; V4의 기록 관찰 API와 V5의 서재 조회 계약

이 레인이 홈에 필요한 최근 7일·오늘·읽는 중 요약 계약을 소유한다.

| 계층 | 범위 |
|---|---|
| Data | 최근 7일 기록, 오늘 읽은 페이지, 읽는 중 목록과 첫 사용자 판별에 필요한 최소 집계 쿼리/Repository를 추가한다. |
| Domain | 홈 요약, 읽는 중 책, 첫 사용자/읽는 중 없음 분기 UseCase를 구현한다. |
| UI | 검색·스캔 진입, 독서 요약 카드, 읽는 중 목록, 첫 사용자 온보딩과 읽는 중 없음 상태를 구현한다. |
| 검증 | 첫 사용자, 읽는 중 없음의 대기 도서 유무, 계속 사용자, 더보기·요약 카드 이동 테스트를 작성한다. |

권장 PR 순서:

1. 첫 사용자 온보딩과 검색·스캔 진입
2. 읽는 중 목록·없음 분기와 서재 이동
3. 최근 7일 독서 요약과 통계 이동

### V7 — 통계

`depends_on`: C1, C2; V4의 기록 관찰 API

이 레인이 기간별 통계 집계 공개 계약의 소유자다.

| 계층 | 범위 |
|---|---|
| Data | 날짜 범위별 읽은 페이지·책 수·연속 기록·날짜별/책별 기록 집계 쿼리와 Stats Repository를 구현한다. |
| Domain | 오늘·4주·1년·직접 선택 기간, 독서 달력, 기간 요약, 날짜 상세 UseCase를 구현한다. |
| UI | 기간 전환, 직접 선택 시트, 독서 달력, 날짜 기록 시트, 오늘 목록과 복합 차트를 구현한다. 긴 기간은 축을 고정하고 그래프만 가로 스크롤한다. |
| 검증 | 기간 경계·윤년·빈 기간·날짜 선택, 집계 정확성, 최신 위치 초기 스크롤과 축 고정 UI 테스트를 작성한다. |

권장 PR 순서:

1. 기간 선택과 요약 집계
2. 독서 달력과 날짜별 기록 시트
3. 오늘 읽은 책 목록
4. 복합 차트와 최신 위치·축 고정 스크롤

---

## 5. 앱 조립과 통합 검증

조립 작업은 필요한 V 레인이 머지된 destination부터 점진적으로 진행한다.

- stub destination을 실제 `NavGraphBuilder.<name>Screen()`으로 교체하고 화면 간 콜백·인자를 연결한다.
- TopLevelDestination 4개와 시작 destination을 확인한다.
- 검색 → 책 상세 → 기록 추가 → 서재 → 통계 E2E 흐름을 검증한다.
- 다크/라이트/시스템 테마, 오프라인 캐시, 알림, 스캔 권한, 폰·태블릿·폴더블과 회전을 통합 검증한다.

---

## 6. 기능 ↔ 레인 매핑

| 기능 | 계약 | 버티컬 레인 | 완료 조건 |
|---|---|---|---|
| 검색 | C2 | V1 | Data/Domain/UI/Paging·Undo 테스트 |
| 스캔 | C2 | V2 | Scanner core/조회/UI/권한·회전 테스트 |
| 설정·알림 | C2 | V3 | Notifications/UseCase/UI/스케줄·테마 테스트 |
| 책 상세·독서 기록 | C1, C2 | V4 | Repository/UseCase/UI/CRUD·상태 테스트 |
| 서재 | C1, C2 | V5 | 정렬·상태 Data/Domain/UI/Undo 테스트 |
| 홈 | C1, C2 | V6 | 집계/UseCase/UI/상태·이동 테스트 |
| 통계 | C1, C2 | V7 | 기간 집계/UseCase/UI/기간·스크롤 테스트 |

모든 사용자 기능은 하나의 버티컬 레인에 매핑되며, 공용 계약의 의미 변경은 C1·C2 또는 표에 지정된 소유 레인에서만 수행한다.
