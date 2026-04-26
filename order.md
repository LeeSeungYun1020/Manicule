# 독서 기록 앱 — 구현 순서

> [plan.md](plan.md) 의 기능 요구사항과 [structure.md](structure.md) 의 모듈 구조를 기반으로
> 작성한 구현 전략. 레이어 간은 바텀업, 핵심 피처는 버티컬 슬라이싱 방식으로 구성한다.

---

## 1. 구현 원칙

| 원칙 | 의미 | 적용 단계 |
|---|---|---|
| **Standard First** | 모든 모듈이 공유하는 표준(모델·디자인 시스템)을 먼저 완전히 정의하여 일관성 확보 | 1단계 |
| **Vertical Slicing** | 피처 단위로 Data → Domain → UI 레이어를 한 번에 관통하여 동작하는 화면을 빠르게 확보 | 3·4단계 |
| **Offline-first** | Room을 SSOT로 두고 네트워크는 보조 수단으로 처리, UI는 항상 Room Flow 구독 | 2단계 |
| **Test-Driven** | 각 레이어·슬라이스 구현 시 단위 테스트를 동반 작성하여 회귀 방지 (structure.md 7절 따름) | 전 단계 |

---

## 2. 구현 단계

### 1단계 — 기반 시스템 (Foundation)

> **Standard First 적용.** 모든 모듈이 의존하는 표준을 먼저 확정.

| 순서 | 대상 | 내용 |
|---|---|---|
| 1 | `build-logic` | Convention Plugin (AndroidApplication, AndroidLibrary, AndroidFeature, Hilt, Room, JvmLibrary 등) |
| 2 | `core:model` | 도메인 모델 전체 (Book, BookEntry, ReadingRecord, ContributionDay 등) |
| 3 | `core:common` | Dispatcher, Result 래퍼, DateExt, FlowExt |
| 4 | `core:designsystem` | NoteTheme(Color/Type/Shape) + 공통 컴포넌트(NoteButton, NoteDialog 등) 전체 |
| 5 | `app` 스켈레톤 | MainActivity, NoteNavHost stub, TopLevelDestination 4개 하단 탭 |

표준 모듈은 변경 빈도가 높으면 전체에 파급되므로 후속 단계 전에 안정화한다.
`app` 스켈레톤은 이후 단계에서 동작 확인용 실행 컨텍스트로 사용한다.

---

### 2단계 — 데이터 엔진 & 공통 인프라 (Core Engine)

> **Offline-first 적용.** Room SSOT 기반으로 Repository를 완성하여 UI가 항상 Flow를 구독하도록 한다.

| 순서 | 대상 | 내용 |
|---|---|---|
| 1 | `core:database` | Room Entity, DAO, NoteDatabase, Migration |
| 2 | `core:network` | NLK ISBN API (Retrofit), DTO, NlkAuthInterceptor |
| 3 | `core:datastore` | UserPreferences DataStore (테마, 알림 설정) |
| 4 | `core:data` | Repository 인터페이스 + 구현체, Entity/DTO ↔ Model 매퍼 |
| 5 | `core:domain` | UseCase 베이스 구조 (이후 슬라이스에서 UseCase 추가) |
| 6 | `core:ui` | BookCover, BookListItem, BookProgressBar, ContributionGrid 공유 컴포넌트 |

`core:database`·`core:network`·`core:datastore`는 상호 의존이 없어 병렬 구현 가능.
Repository 단위 테스트(FakeDao, FakeApi)와 Room in-memory 테스트를 동반 작성한다.

---

### 3단계 — 핵심 피처 버티컬 슬라이싱 (Vertical Slices)

> **Vertical Slicing 적용.** 각 슬라이스를 Data 보강 → Domain UseCase → UI feature 순으로 한 번에 관통한다.

사용자 핵심 흐름(검색 → 등록 → 조회 → 통계) 순서로 진행한다.

#### Slice 1 — `feature:search`
도서 검색, 최근 검색어. 모든 흐름의 진입점.

- DAO·Repository에 검색 쿼리/최근 검색어 메서드 추가
- `SearchBooksUseCase`, `GetRecentQueriesUseCase`, `SaveRecentQueryUseCase`
- SearchScreen, ViewModel, UiState
- **검증 기준**: 키워드 검색 시 결과 목록 표시, 최근 검색어 저장·재실행

#### Slice 2 — `feature:bookdetail`
책 상세, 상태/별점/메모, 독서 기록. 가장 복잡한 핵심 비즈니스 로직.

- BookEntry CRUD, ReadingRecord CRUD DAO·Repository 보강
- `GetBookDetailUseCase`, `ObserveBookEntryUseCase`, `ChangeReadingStatusUseCase`, `UpdateRatingMemoUseCase`, `AddReadingRecordUseCase`(40쪽 신호 포함), `EditReadingRecordUseCase`, `DeleteReadingRecordUseCase`, `ObserveBookRecordsUseCase`
- BookHeader, BookPublishInfoSection, BookDescriptionSection(URL fetch), BookTocSection(URL fetch), StatusSelector, RatingMemoEditor, ReadingRecordList, AddRecordSheet, FinishConfirmDialog
- **검증 기준**: 상태 변경, 기록 추가/수정/삭제, 40쪽 이하 완독 다이얼로그, 상태 전환 규칙

#### Slice 3 — `feature:library`
서재 목록, 필터, 정렬, 삭제.

- BookEntryDao 정렬 쿼리 보강
- `GetLibraryBooksUseCase`, `DeleteBookEntryUseCase`
- StatusTabRow, SortMenu, LibraryBookCard, DeleteConfirmDialog
- **검증 기준**: 상태별 탭 필터, 3종 정렬, 진도 표시, 삭제 다이얼로그

#### Slice 4 — `feature:home` + `feature:stats`
집계·통계 화면. 누적된 reading record 데이터를 활용.

- 잔디·통계 집계용 ReadingRecordDao 쿼리 추가
- `GetTodaySummaryUseCase`, `GetReadingStreakUseCase`, `GetContributionUseCase`, `GetPeriodSummaryUseCase`
- HomeScreen 컴포넌트 전체, StatsScreen 컴포넌트 전체
- **검증 기준**: 홈 읽는 중 목록·잔디 미리보기·오늘 통계, 통계 52주 잔디·이번 달/올해 요약

---

### 4단계 — 시스템 연동 슬라이싱 (System Integration Slices)

> 핵심 흐름 완성 후 부가 기능을 슬라이스 단위로 추가.

#### Slice 5 — 바코드 스캔
- `core:scanner` (CameraX + ML Kit, BarcodeScanner 인터페이스, IsbnValidator)
- `feature:scanner` (CameraPreview, ViewfinderOverlay, PermissionDeniedView)
- `GetBookByScanUseCase`
- 홈·검색 화면에서 스캔 진입점 연결
- **검증 기준**: 카메라 권한 흐름, ISBN 인식 → 책 상세 진입, 인식 실패 다이얼로그

#### Slice 6 — 알림 + 설정
- `core:notifications` (ReminderScheduler, WorkManagerReminderScheduler, ReminderWorker)
- `feature:settings` (ThemeRadioGroup, ReminderToggle, ReminderTimePicker)
- `GetUserPreferencesUseCase`, `SetThemeUseCase`, `SetReminderUseCase`
- **검증 기준**: 테마 즉시 적용, 알림 on/off 및 시간 설정 → 매일 알림 발송

---

### 5단계 — App 조립 (App Assembly)

> 분리된 App 단계. 모든 feature를 NoteNavHost로 최종 연결.

- 모든 destination을 NoteNavHost에 등록 (`homeScreen`, `searchScreen`, `scannerScreen`, `bookDetailScreen`, `libraryScreen`, `statsScreen`, `settingsScreen`)
- TopLevelDestination 4개 하단 탭 ↔ 시작 destination 매핑
- 화면 간 navigation 인자 전달 검증 (예: bookdetail의 `isbn:String`)
- Hilt 의존 그래프 전체 빌드 확인
- 화면 간 onNavigate 콜백 모두 연결

---

### 6단계 — 검증 (Verification)

> 단계별 + 최종 통합 방식.

#### 단계별 검증
3·4단계 각 슬라이스 완료 시점에 인라인으로 검증 기준을 확인한다 (위 슬라이스별 "검증 기준" 항목).

#### 최종 통합 검증
| 항목 | 내용 |
|---|---|
| E2E 시나리오 | "도서 검색 → 책 상세 진입 → 상태 변경 → 독서 기록 추가 → 서재 조회 → 통계 확인" 전 흐름 |
| 시각 검증 | 다크/라이트/시스템 테마 동작, `core:designsystem` 준수, 컴포넌트 스냅샷 테스트 |
| Offline-first 검증 | 네트워크 단절 시 캐시된 책·서재·기록 정상 표시, 검색만 네트워크 의존 |
| Navigation 통합 테스트 | `app` 모듈에서 모든 화면 진입·이탈 흐름 자동화 검증 |
| 알림 검증 | 설정한 시간에 리마인더 알림 발송, on/off 토글 동작 |
| 바코드 스캔 검증 | ISBN 인식 성공/실패 흐름, 카메라 권한 거부 흐름 |

---

## 3. 모듈 등장 점검

structure.md에 정의된 모든 모듈이 어느 단계에서 구현되는지 정리.

| 모듈 | 단계 |
|---|---|
| `build-logic` | 1단계 |
| `core:model` | 1단계 |
| `core:common` | 1단계 |
| `core:designsystem` | 1단계 |
| `app` 스켈레톤 | 1단계 |
| `core:database` | 2단계 |
| `core:network` | 2단계 |
| `core:datastore` | 2단계 |
| `core:data` | 2단계 |
| `core:domain` 베이스 | 2단계 |
| `core:ui` | 2단계 |
| `feature:search` | 3단계 Slice 1 |
| `feature:bookdetail` | 3단계 Slice 2 |
| `feature:library` | 3단계 Slice 3 |
| `feature:home`, `feature:stats` | 3단계 Slice 4 |
| `core:scanner`, `feature:scanner` | 4단계 Slice 5 |
| `core:notifications`, `feature:settings` | 4단계 Slice 6 |
| `app` 최종 조립 | 5단계 |
