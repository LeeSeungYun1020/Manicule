# 독서 기록 앱 — 모듈 구조

> 본 문서는 [plan.md](plan.md) 의 기능 요구사항을 기반으로
> [Android App Architecture (Guide to app architecture)](https://developer.android.com/topic/architecture) 준수하여 구조 설계를 진행

---

## 1. 아키텍처 원칙

Android 공식 권장 아키텍처(3-layer)를 따른다.

```
┌────────────────────────────────────────────────┐
│                   UI Layer                     │  Composables · ViewModel · UiState
│         (feature 모듈, app 모듈, core:ui)        │
└──────────────────────┬─────────────────────────┘
                       │ 의존
┌──────────────────────▼─────────────────────────┐
│                Domain Layer                    │  UseCase · 도메인 모델
│                  (core:domain)                 │
└──────────────────────┬─────────────────────────┘
                       │ 의존
┌──────────────────────▼─────────────────────────┐
│                  Data Layer                    │  Repository · DataSource
│   (core:data, core:database, core:network,     │
│    core:datastore, core:scanner,               │
│    core:notifications)                         │
└────────────────────────────────────────────────┘
```

핵심 규칙:

- **단방향 의존**: UI → Domain → Data. 역방향 금지.
- **Feature 간 의존 금지**: feature 모듈끼리 직접 의존하지 않는다. 공통이 필요하면 `core:*`로 추출.
- **Repository는 SSOT(Single Source of Truth)**: 외부에는 도메인 모델만 노출, DTO/Entity는 Data Layer 내부에 격리.
- **단방향 데이터 흐름(UDF)**: ViewModel은 `StateFlow<UiState>` 노출, UI는 이벤트만 송신.
- **Offline-first**: Room을 SSOT로, 네트워크는 도서 검색 시에만 호출.
- **반응형 레이아웃**: 폰·태블릿·폴더블 + 회전 모두 대응. WindowSizeClass 기반 분기, ViewModel은 `SavedStateHandle`로 회전 시 상태 보존.
- **빌드 베이스라인**: minSdk 24 + `coreLibraryDesugaring` 활성화(java.time 등), Android Auto Backup(`allowBackup=true`)으로 로컬 데이터 자동 백업.

---

## 2. 모듈 구성 개요

```
manicule/
├── app/                            # 진입점, NavHost, DI 그래프 조립
│
├── feature/                        # 화면 단위 모듈
│   ├── home/
│   ├── search/
│   ├── scanner/
│   ├── bookdetail/
│   ├── library/
│   ├── stats/
│   └── settings/
│
└── core/                           # 공용 모듈
    ├── designsystem/               # 디자인 토큰, 테마, 공통 컴포넌트
    ├── ui/                         # 다중 feature가 공유하는 UI 요소(잔디, 책 카드 등)
    ├── common/                     # Dispatcher, Result, 확장함수
    ├── model/                      # 도메인 모델
    ├── domain/                     # UseCase
    ├── data/                       # Repository 구현
    ├── database/                   # Room (DB Entity, DAO)
    ├── datastore/                  # Preferences DataStore (테마, 알림 설정)
    ├── network/                    # Retrofit (국립중앙도서관 API)
    ├── scanner/                    # CameraX + ML Kit 바코드 인식
    └── notifications/              # 독서 리마인더 알림 스케줄링
```

### 2.1 모듈 책임 요약

| 모듈                   | 레이어    | 책임                                               |
|----------------------|--------|--------------------------------------------------|
| `app`                | -      | NavHost, MainActivity, Application 클래스, Hilt 그래프 |
| `feature:home`       | UI     | 홈 화면(검색창, 독서 달력 미리보기, 읽는 중 책, 오늘 통계)               |
| `feature:search`     | UI     | 도서 검색, 최근 검색어                                    |
| `feature:scanner`    | UI     | 바코드 스캔 화면                                        |
| `feature:bookdetail` | UI     | 책 상세, 상태/별점/메모, 독서 기록                            |
| `feature:library`    | UI     | 내 서재(상태 탭, 정렬)                                   |
| `feature:stats`      | UI     | 통계(오늘/4주/1년/직접선택, 기간별 달력)                       |
| `feature:settings`   | UI     | 테마, 알림 설정                                        |
| `core:designsystem`  | UI     | ManiculeTheme, Color, Typography, 공통 Button/Dialog   |
| `core:ui`            | UI     | BookCard, ReadingCalendarGrid 등 feature 간 공유 컴포넌트  |
| `core:common`        | -      | Dispatcher 정의, Result 래퍼, 날짜 유틸                  |
| `core:model`         | Domain | Book, ReadingStatus, ReadingRecord 등             |
| `core:domain`        | Domain | UseCase (AddReadingRecord, GetStreak 등)          |
| `core:data`          | Data   | Repository 구현, DTO/Entity ↔ Model 매퍼             |
| `core:database`      | Data   | Room Database, DAO, Entity                       |
| `core:datastore`     | Data   | UserPreferences (테마, 알림)                         |
| `core:network`       | Data   | 국립중앙도서관 ISBN API 클라이언트                           |
| `core:scanner`       | Data   | CameraX + ML Kit 기반의 바코드 분석기 및 원천 데이터 제공         |
| `core:notifications` | Data   | WorkManager / AlarmManager 기반 리마인더               |

### 2.2 모듈 의존 그래프

```
                         ┌─────┐
                         │ app │
                         └──┬──┘
        ┌──────────────────┼──────────────────────┐
        ▼                  ▼                      ▼
  feature:home   feature:search   feature:scanner   ...
        │                  │                      │
        └──────┬───────────┴──────────┬───────────┘
                    ▼                        ▼
              core:domain              core:designsystem
           /       |       \                 ▲
          ▼        ▼        ▼                │
     core:data  core:scanner  core:notifications
    /    │    \                           core:ui
network database datastore             core:common
       \   │   /
        core:model
```

---

## 3. 모듈별 파일 구조

> 패키지 루트는 `com.leeseungyun1020.manicule`. 실제 패키지는 프로젝트 정책에 맞춰 변경.

### 3.1 `app`

```
app/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    └── java/com/example/note/
        ├── ManiculeApplication.kt              # @HiltAndroidApp
        ├── MainActivity.kt                  # 단일 Activity + Compose
        └── navigation/
            ├── ManiculeNavHost.kt               # 최상위 NavHost
            ├── TopLevelDestination.kt       # 홈/서재/통계/설정 4개 탭
            └── ManiculeAppState.kt              # rememberManiculeAppState
```

### 3.2 Feature 모듈 공통 구조

각 feature 모듈은 다음 패턴을 따른다.

```
feature/<name>/
├── build.gradle.kts
└── src/main/java/com/example/note/feature/<name>/
    ├── <Name>Route.kt                      # ViewModel 주입, 상태 수집
    ├── <Name>Screen.kt                     # @Composable 순수 UI
    ├── <Name>ViewModel.kt                  # @HiltViewModel, StateFlow
    ├── <Name>UiState.kt                    # sealed interface or data class
    ├── <Name>UiEvent.kt                    # 사용자 이벤트(선택)
    ├── navigation/
    │   └── <Name>Navigation.kt             # NavGraphBuilder.<name>Screen()
    └── components/                         # 해당 화면 전용 컴포저블
```

### 3.3 `feature:home` (홈, 1a~1c)

```
feature/home/
└── src/main/java/com/example/note/feature/home/
    ├── HomeRoute.kt
    ├── HomeScreen.kt
    ├── HomeViewModel.kt
    ├── HomeUiState.kt                      # InProgressBooks, Streak, TodayStat
    ├── navigation/
    │   └── HomeNavigation.kt               # homeScreen(onNavigateToSearch, onNavigateToScanner, ...)
    └── components/
        ├── HomeSearchBar.kt
        ├── ScanBarcodeButton.kt
        ├── InProgressSection.kt            # "더보기" 포함
        ├── EmptyInProgress.kt              # '읽는 중' 책 없을 때 '읽고 싶음' 책 유무에 따른 안내 및 액션 카드 포함
        └── ReadingSummaryCard.kt           # 독서 요약 카드 (잔디 7일, 연속 기록, 오늘 페이지)
```

### 3.4 `feature:search` (검색, 2a~3b)

```
feature/search/
└── src/main/java/com/example/note/feature/search/
    ├── SearchRoute.kt
    ├── SearchScreen.kt
    ├── SearchViewModel.kt
    ├── SearchUiState.kt                    # query, RecentQueries, filteredQueries(입력 중 로컬 필터), PagingData<Book>, inputPhase(Idle/Typing/Submitted)
    ├── SearchUiEvent.kt                    # 검색어 삭제, 삭제 Undo 등
    ├── navigation/
    │   └── SearchNavigation.kt
    └── components/
        ├── SearchTopBar.kt                 # Material 3 SearchBar 패턴
        ├── RecentQueryList.kt              # 최근 검색어 리스트, 개별 삭제 스낵바 Undo 포함. 입력 중에는 최근 검색어를 입력값으로 로컬 필터링해 노출
        ├── EmptyRecentQuery.kt             # 최근 검색어 없음(첫 사용/전체 삭제) 검색 유도 안내 카드
        ├── SearchResultList.kt             # 총 검색 결과 건수 표시 포함
        └── EmptySearchResult.kt            # "스캔 화면으로 이동" 버튼 포함
```

### 3.5 `feature:scanner` (바코드 스캔, 4a)

```
feature/scanner/
└── src/main/java/com/example/note/feature/scanner/
    ├── ScannerRoute.kt
    ├── ScannerScreen.kt
    ├── ScannerViewModel.kt
    ├── ScannerUiState.kt                   # Initializing / Scanning / Recognized / Failed
    ├── navigation/
    │   └── ScannerNavigation.kt
    └── components/
        ├── CameraPreview.kt                # Preview UseCase 직접 생성, bindToLifecycle, 회전 시 targetRotation 갱신
        ├── ViewfinderOverlay.kt            # 스캔 영역 가이드 UI 및 안내 문구
        ├── ScannerErrorState.kt            # "검색" 버튼 포함 (결과 없음/에러)
        └── PermissionDeniedState.kt        # 카메라 권한 거부 안내
```

### 3.6 `feature:bookdetail` (책 상세, 5a~5f)

```
feature/bookdetail/
└── src/main/java/com/example/note/feature/bookdetail/
    ├── BookDetailRoute.kt
    ├── BookDetailScreen.kt
    ├── BookDetailViewModel.kt
    ├── BookDetailUiState.kt                # Book + Records + Status + Rating + Memo
    ├── BookDetailUiEvent.kt                # 상태 변경, 별점/메모 변경, 기록 추가/수정/삭제, 기록 삭제 실행취소 등
    ├── navigation/
    │   └── BookDetailNavigation.kt         # 인자: isbn:String
    └── components/
        ├── BookDetailTabs.kt               # "책 정보", "내 기록" 탭
        ├── BookInfoTabContent.kt           # 표지, 정보, 소개, 목차 영역
        ├── MyRecordTabContent.kt           # 상태, 리뷰, 기록 영역
        ├── BookHeader.kt                   # 표지·제목·저자 등 (책 정보 탭 내부)
        ├── BookPublishInfoSection.kt       # 페이지 수·가격·분류
        ├── BookDescriptionSection.kt       # 책 소개 (introductionUrl fetch)
        ├── BookTocSection.kt               # 목차 (tableOfContentsUrl fetch)
        ├── StatusSelector.kt               # 읽고 싶음 / 읽는 중 / 다 읽음
        ├── RatingMemoEditor.kt             # 별점·메모 인라인 편집 (별 탭 시 즉시 저장, 메모 포커스 아웃 시 자동 저장, 빈 상태는 점선 UI). 별도 시트/다이얼로그 없음
        ├── ReadingRecordList.kt            # 진행률 프로그레스 바, 날짜별 기록
        ├── EmptyReadingRecord.kt           # 독서 기록 빈 상태 안내 컴포넌트
        ├── AddRecordBottomSheet.kt         # 날짜(오늘/어제/직접선택)/시간(지금/직접선택) 세그먼트, 쪽수 입력 바텀시트. '직접 선택' 시 Android 표준 DatePicker/TimePicker 다이얼로그 호출
        └── FinishConfirmDialog.kt          # 기록 후 남은 페이지 10%/40쪽 이하 시 "혹시 책을 다 읽으셨나요?" 확인, "네" 시 다 읽음 전환
```

### 3.7 `feature:library` (서재, 6a~6f)

```
feature/library/
└── src/main/java/com/example/note/feature/library/
    ├── LibraryRoute.kt
    ├── LibraryScreen.kt
    ├── LibraryViewModel.kt
    ├── LibraryUiState.kt                   # selectedTab, sort, books
    ├── LibraryUiEvent.kt                   # 정렬 변경, 상태 필터 변경, 책 삭제, 상태 변경, 실행취소 등
    ├── navigation/
    │   └── LibraryNavigation.kt
    └── components/
        ├── StatusTabRow.kt                 # 읽고 싶음 / 읽는 중 / 다 읽음
        ├── SortBottomSheet.kt              # 기준(추가/수정/별점) 및 방향 선택 (적용 버튼으로 확정)
        ├── LibraryBookCard.kt              # 상태별 표시 (기본, 진도율, 다 읽은 날짜)
        └── EmptyLibrary.kt                 # 책이 없는 경우 빈 상태 표시 (검색, 스캔 버튼 포함)
```

### 3.8 `feature:stats` (통계, 7a~7e)

```
feature/stats/
└── src/main/java/com/example/note/feature/stats/
    ├── StatsRoute.kt
    ├── StatsScreen.kt
    ├── StatsViewModel.kt
    ├── StatsUiState.kt                     # period(오늘/4주/1년/직접선택), summary, calendar, selectedDay
    ├── navigation/
    │   └── StatsNavigation.kt              # 인자: focus:String? (잔디 위치 스크롤용)
    └── components/
        ├── PeriodSelector.kt               # 오늘 / 4주 / 1년 / 직접 선택
        ├── PeriodSelectionBottomSheet.kt   # 직접 선택 탭의 시작일/종료일 설정용 바텀 시트
        ├── SummaryCards.kt                 # 다 읽은 권수, 페이지 수
        ├── ReadingChart.kt                 # 책(막대) + 페이지(꺾은선) 복합 차트 (좌축=권수, 우축=페이지 눈금 + 격자선, 가로 스크롤 시 좌우 축 고정·가운데만 스크롤, 우측 정렬)
        ├── SelectedDayRecords.kt           # 오늘 탭 하단에 표시되는 해당 일 독서 기록 목록
        └── SelectedDayRecordsBottomSheet.kt # 4주/1년/직접선택 탭 달력에서 특정 날짜 클릭 시 올라오는 독서 기록 바텀 시트
```

### 3.9 `feature:settings` (설정, 8a)

```
feature/settings/
└── src/main/java/com/example/note/feature/settings/
    ├── SettingsRoute.kt
    ├── SettingsScreen.kt
    ├── SettingsViewModel.kt
    ├── SettingsUiState.kt                  # theme, reminder
    ├── navigation/
    │   └── SettingsNavigation.kt
    └── components/
        ├── ThemeSegmentedControl.kt        # 시스템/라이트/다크 테마 선택 (세그먼트 컨트롤)
        ├── ReminderToggle.kt               # 리마인더 on/off 토글 스위치 및 시간 설정 행
        ├── ReminderTimePicker.kt           # 리마인더 시간 변경 (Android 표준 TimePicker 다이얼로그)
        └── SupportSection.kt               # 오픈소스 라이선스 및 버전 정보 표기 영역
```

---

## 4. Core 모듈 파일 구조

### 4.1 `core:designsystem`

```
core/designsystem/
└── src/main/java/com/example/note/core/designsystem/
    ├── theme/
    │   ├── Color.kt                        # Material 3 고정 브랜드 컬러 (라이트/다크)
    │   ├── ExtendedColor.kt                # 확장 색상 토큰
    │   ├── Type.kt                         # Noto Sans KR (Downloadable Fonts) + 시스템 폰트 fallback
    │   ├── Dimension.kt                    # 치수 (Spacing, Size, Border)
    │   ├── Shape.kt
    │   ├── Motion.kt                       # (추후 도입) 애니메이션 시간/에이징 곡선 토큰
    │   └── ManiculeTheme.kt                # MaterialTheme 래퍼, 다크/라이트
    ├── component/
    │   ├── ManiculeButton.kt
    │   ├── ManiculeTextField.kt
    │   ├── ManiculeDialog.kt                   # 공통 다이얼로그 (이름·메시지·확인/취소)
    │   ├── ManiculeTopAppBar.kt
    │   ├── ManiculeEmptyState.kt               # 빈 상태(Empty State) 공통 화면
    │   ├── ManiculeBottomSheet.kt              # 공통 바텀시트 레이아웃 (둥근 모서리, 닫기 버튼)
    │   ├── ManiculeSegmentedButton.kt          # 테두리 있는 둥근 탭 UI (테마, 통계기간 등 공통)
    │   ├── ManiculeLoading.kt
    │   ├── ManiculeSearchBar.kt                # 공통 검색 바
    │   ├── ManiculeListItem.kt                 # 공통 리스트 아이템
    │   ├── ManiculeSectionHeader.kt            # 섹션 헤더
    │   ├── ManiculeSnackbarHost.kt             # 스낵바 호스트 및 Undo 지원
    │   ├── ManiculeChip.kt                     # 최근 검색어 등 칩
    │   ├── ManiculeTabRow.kt                   # 공통 탭 행
    │   ├── ManiculeRatingBar.kt                # 별점 컴포넌트
    │   ├── ManiculeExpandableText.kt           # "더보기" 포함된 확장 텍스트
    │   └── ManiculeStatTile.kt                 # 통계 및 수치 표시 타일
    ├── icon/
    │   └── ManiculeIcons.kt
    └── res/                                # 색상·문자열 등 디자인 토큰
```

### 4.2 `core:ui`

> feature 간 재사용되는 UI(예: 책 카드, 잔디). designsystem 보다 한 단계 위.

```
core/ui/
└── src/main/java/com/example/note/core/ui/
    ├── book/
    │   ├── BookCover.kt                    # Coil 3.x AsyncImage 래퍼, 표지 fallback 처리
    │   ├── BookCoverOverlay.kt             # 스캔 가이드나 상태 등 표지 위에 올라가는 오버레이
    │   ├── BookListItem.kt
    │   └── BookProgressBar.kt              # 132 / 320쪽 표시
    ├── calendar/
    │   ├── ReadingCalendarCell.kt
    │   ├── ReadingCalendarGrid.kt          # 독서 달력 공통 그리드
    │   └── ReadingCalendarLegend.kt        # 달력 색상 범례
    └── preview/                             # @Preview 용 Sample 데이터
        └── BookPreviewParameterProvider.kt
```

### 4.3 `core:common`

```
core/common/
└── src/main/java/com/example/note/core/common/
    ├── di/
    │   └── DispatchersModule.kt            # @IoDispatcher, @DefaultDispatcher
    ├── result/
    │   └── Result.kt                       # sealed Loading/Success/Error + asResult()
    ├── time/
    │   ├── Clock.kt                        # 테스트용 Clock 추상화
    │   └── DateExt.kt                      # LocalDate 확장(주 시작일 등)
    └── ext/
        └── FlowExt.kt
```

### 4.4 `core:model`

```
core/model/
└── src/main/java/com/example/note/core/model/
    ├── Book.kt                             # isbn(EA_ISBN), title, author, publisher, pubDate(PUBLISH_PREDATE), coverUrl(TITLE_URL), totalPages(PAGE), price(PRE_PRICE), category(SUBJECT), tableOfContentsUrl(BOOK_TB_CNT_URL), introductionUrl(BOOK_INTRODUCTION_URL), summaryUrl(BOOK_SUMMARY_URL)
    ├── ReadingStatus.kt                    # WANT / READING / FINISHED
    ├── BookEntry.kt                        # Book + Status + rating + memo + finishedAt
    ├── ReadingRecord.kt                    # id, isbn, date, time, startPage, endPage
    ├── DailyReading.kt                     # 통계용 (date, pages)
    ├── ReadingCalendarDay.kt               # 독서 달력 한 칸 (date, intensity)
    ├── ReadingStreak.kt
    ├── PeriodSummary.kt                    # 다 읽은 권수, 페이지 수
    ├── UserPreferences.kt                  # ThemeMode, ReminderConfig
    └── SearchQuery.kt
```

### 4.5 `core:domain`

```
core/domain/
└── src/main/java/com/example/note/core/domain/
    ├── di/
    │   └── DomainModule.kt
    ├── book/
    │   └── GetBookDetailUseCase.kt          # ISBN → Book 조회 (DB 우선, 없으면 네트워크 fetch)
    ├── search/
    │   ├── SearchBooksUseCase.kt           # Flow<PagingData<Book>> 반환 (Paging 3 통합)
    │   ├── GetRecentQueriesUseCase.kt
    │   └── SaveRecentQueryUseCase.kt
    ├── scanner/
    │   └── GetBookByScanUseCase.kt          # 스캔된 ISBN으로 도서 정보 조회 및 유효성 검증
    ├── library/
    │   ├── GetLibraryBooksUseCase.kt        # status, sort 인자
    │   ├── ObserveBookEntryUseCase.kt       # ISBN → BookEntry(상태/별점/메모) 관찰, 미등록 시 null
    │   ├── ChangeReadingStatusUseCase.kt    # 다 읽음 시 finishedAt 저장 규칙 포함
    │   ├── DeleteBookEntryUseCase.kt
    │   └── UpdateRatingMemoUseCase.kt
    ├── record/
    │   ├── AddReadingRecordUseCase.kt       # 읽고싶음→읽는 중 자동 전환(첫 기록 생성 시에만), 남은 페이지 10% 또는 40쪽 이하 신호 반환
    │   ├── EditReadingRecordUseCase.kt
    │   ├── DeleteReadingRecordUseCase.kt
    │   └── ObserveBookRecordsUseCase.kt
    ├── stats/
    │   ├── GetTodaySummaryUseCase.kt
    │   ├── GetPeriodSummaryUseCase.kt       # 지정 기간 (오늘/4주/1년/직접선택)
    │   ├── GetReadingCalendarUseCase.kt     # 365일 독서 달력
    │   └── GetReadingStreakUseCase.kt
    └── settings/
        ├── GetUserPreferencesUseCase.kt
        ├── SetThemeUseCase.kt
        └── SetReminderUseCase.kt           # 알림 스케줄링 트리거
```

### 4.6 `core:data`

```
core/data/
└── src/main/java/com/example/note/core/data/
    ├── di/
    │   └── DataModule.kt                   # Repository 바인딩
    ├── repository/
    │   ├── BookRepository.kt               # interface
    │   ├── BookRepositoryImpl.kt           # network + database 결합
    │   ├── ReadingRecordRepository.kt
    │   ├── ReadingRecordRepositoryImpl.kt
    │   ├── LibraryRepository.kt            # BookEntry CRUD, 정렬·필터
    │   ├── LibraryRepositoryImpl.kt
    │   ├── StatsRepository.kt
    │   ├── StatsRepositoryImpl.kt
    │   ├── SearchHistoryRepository.kt
    │   ├── SearchHistoryRepositoryImpl.kt
    │   ├── UserPreferencesRepository.kt
    │   └── UserPreferencesRepositoryImpl.kt
    └── mapper/
        ├── BookMapper.kt                   # Dto/Entity ↔ Book
        ├── BookEntryMapper.kt
        └── ReadingRecordMapper.kt
```

### 4.7 `core:database`

```
core/database/
└── src/main/java/com/example/note/core/database/
    ├── di/
    │   └── DatabaseModule.kt
    ├── ManiculeDatabase.kt                     # @Database, version, migrations
    ├── entity/
    │   ├── BookEntity.kt                   # @Entity (PK = isbn)
    │   ├── BookEntryEntity.kt              # status, rating, memo, addedAt, updatedAt, finishedAt
    │   ├── ReadingRecordEntity.kt          # id, isbn, date, time, startPage, endPage
    │   └── RecentQueryEntity.kt
    ├── dao/
    │   ├── BookDao.kt
    │   ├── BookEntryDao.kt                 # status별 Flow, 정렬 쿼리
    │   ├── ReadingRecordDao.kt             # 날짜 범위 조회, 잔디 집계
    │   └── RecentQueryDao.kt
    ├── converter/
    │   └── Converters.kt                   # LocalDate, ReadingStatus
    └── migration/
        └── Migrations.kt
```

### 4.8 `core:datastore`

```
core/datastore/
    └── src/main/java/com/example/note/core/datastore/
        ├── UserPreferencesLocalDataSource.kt   # Theme, Reminder(on/off, time) 등 DataStore 읽기/쓰기
        └── UserPreferencesDataStore.kt         # DataStore 인스턴스 (실제 구현에 맞춤)
    └── PreferencesKeys.kt                  # THEME_MODE, REMINDER_ENABLED, REMINDER_TIME
```

### 4.9 `core:network`

```
core/network/
└── src/main/java/com/example/note/core/network/
    ├── di/
    │   └── NetworkModule.kt                # Retrofit, OkHttp, Json
    ├── nlk/                                # 국립중앙도서관 ISBN API
    │   ├── NlkApi.kt                       # Retrofit interface
    │   ├── NlkAuthInterceptor.kt           # 발급키 주입
    │   └── dto/
    │       ├── NlkSearchResponseDto.kt
    │       └── NlkBookDto.kt
    └── BuildConfigKeys.kt                  # 키 이름 상수
```

### 4.10 `core:scanner`

```
core/scanner/
└── src/main/java/com/example/note/core/scanner/
    ├── di/
    │   └── ScannerModule.kt
    ├── BarcodeScanner.kt                   # interface — Flow<ScanResult>
    ├── MlKitBarcodeScanner.kt              # BarcodeScanner 구현체, ImageAnalysis UseCase 제공
    ├── IsbnValidator.kt                    # ISBN-10/13 체크섬
    └── ScanResult.kt                       # Recognized(isbn) / Failed / Idle
```

### 4.11 `core:notifications`

```
core/notifications/
└── src/main/java/com/example/note/core/notifications/
    ├── di/
    │   └── NotificationsModule.kt
    ├── ReminderScheduler.kt                # interface
    ├── WorkManagerReminderScheduler.kt     # WorkManager 기반 일일 알림
    ├── ReminderWorker.kt
    └── NotificationChannels.kt
```

---

## 5. 화면 ↔ 모듈 매핑

| 화면     | 진입 모듈                | 의존하는 UseCase (core:domain)                                                                                                                                    |
|--------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 홈      | `feature:home`       | GetLibraryBooksUseCase(읽는 중), GetTodaySummaryUseCase, GetReadingStreakUseCase, GetReadingCalendarUseCase                                                      |
| 검색     | `feature:search`     | SearchBooksUseCase, GetRecentQueriesUseCase, SaveRecentQueryUseCase                                                                                           |
| 바코드 스캔 | `feature:scanner`    | GetBookByScanUseCase (BarcodeScanner 인터페이스는 core:scanner, core:domain이 의존)                                                                                    |
| 책 상세   | `feature:bookdetail` | GetBookDetailUseCase, ObserveBookEntryUseCase, ChangeReadingStatusUseCase, UpdateRatingMemoUseCase, AddReadingRecordUseCase, EditReadingRecordUseCase, DeleteReadingRecordUseCase, ObserveBookRecordsUseCase |
| 서재     | `feature:library`    | GetLibraryBooksUseCase, DeleteBookEntryUseCase                                                                                                                |
| 통계     | `feature:stats`      | GetPeriodSummaryUseCase, GetReadingCalendarUseCase                                                                                                            |
| 설정     | `feature:settings`   | GetUserPreferencesUseCase, SetThemeUseCase, SetReminderUseCase                                                                                                |

---

## 6. Gradle 구성 가이드

`build-logic` (Convention Plugins) 도입

```
build-logic/
└── convention/
    └── src/main/java/.../
        ├── AndroidApplicationConventionPlugin.kt
        ├── AndroidLibraryConventionPlugin.kt
        ├── AndroidFeatureConventionPlugin.kt        # feature 모듈 공통 설정 + Compose
        ├── AndroidLibraryComposeConventionPlugin.kt
        ├── AndroidHiltConventionPlugin.kt
        ├── AndroidRoomConventionPlugin.kt
        ├── AndroidLintConventionPlugin.kt           # ktlint(컨벤션) + detekt(비스타일 정적 분석) + Android Lint
        ├── AndroidApplicationFirebaseConventionPlugin.kt # google-services + Crashlytics gradle plugin
        └── JvmLibraryConventionPlugin.kt            # core:model (안드로이드 비의존 순수 Kotlin)
```

플러그인 적용 예:

```kotlin
// feature/home/build.gradle.kts
plugins {
	alias(libs.plugins.manicule.android.feature)
	alias(libs.plugins.manicule.android.library.compose)
}

dependencies {
	implementation(projects.core.designsystem)
	implementation(projects.core.ui)
	implementation(projects.core.domain)
	implementation(projects.core.model)
	implementation(projects.core.common)
}
```

```kotlin
// core/domain/build.gradle.kts
plugins {
	alias(libs.plugins.manicule.android.library)
	alias(libs.plugins.manicule.android.hilt)
}

dependencies {
	implementation(projects.core.model)
	implementation(projects.core.data)
	implementation(projects.core.scanner)
	implementation(projects.core.notifications)
}
```

---

## 7. 테스트 전략

| 모듈                   | 주요 테스트                                                             |
|----------------------|--------------------------------------------------------------------|
| `core:common`        | Result 래퍼 동작, DateExt 주 계산, FlowExt 변환 단위 테스트 (JVM)                  |
| `core:domain`        | UseCase 단위 테스트 (FakeRepository, 코루틴 Test)                          |
| `core:data`          | Repository 단위 테스트 (FakeDao, FakeApi), 매퍼 테스트                       |
| `core:database`      | Room in-memory DAO 테스트 (instrumented)                              |
| `core:datastore`     | TestDataStore 기반 UserPreferences 읽기/쓰기 테스트                         |
| `core:network`       | MockWebServer 기반 NlkApi 테스트                                        |
| `core:scanner`       | ISBN 유효성 검증 알고리즘 및 스캔 결과 가공 테스트                                    |
| `core:notifications` | WorkManager 기반 알림 예약 및 스케줄링 검증                                     |
| `core:designsystem`  | 공통 컴포넌트(Button, Dialog 등) Compose UI 테스트(`createComposeRule`) |
| `core:ui`            | BookCard, ContributionGrid Compose UI 테스트                         |
| `feature:*`          | ViewModel StateFlow 검증, Compose UI 테스트(`createAndroidComposeRule`) |
| `app`                | Navigation 통합 테스트                                                  |

---

## 8. 모듈화로 얻는 이점

- **빌드 속도**: feature 모듈 변경 시 영향 범위 최소화 → 증분 빌드 단축.
- **관심사 분리**: 화면 추가가 다른 화면에 영향을 주지 않음.
- **재사용**: `core:ui` 의 `ReadingCalendarGrid` 를 홈(8주 미리보기)·통계(기간 가변형)에서 동일 구현으로 사용.
- **테스트 용이성**: `core:model`, `core:common`은 순수 JVM 모듈. `core:domain` UseCase, `core:data` Repository는 Fake 구현체로 Android 환경 없이 단위 테스트 가능.
- **Offline-first 단순화**: Repository SSOT 원칙으로 UI는 항상 Room의 Flow만 구독.
