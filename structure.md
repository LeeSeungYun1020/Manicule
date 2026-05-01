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
| `feature:home`       | UI     | 홈 화면(검색창, 잔디 미리보기, 읽는 중 책, 오늘 통계)                |
| `feature:search`     | UI     | 도서 검색, 최근 검색어                                    |
| `feature:scanner`    | UI     | 바코드 스캔 화면                                        |
| `feature:bookdetail` | UI     | 책 상세, 상태/별점/메모, 독서 기록                            |
| `feature:library`    | UI     | 내 서재(상태 탭, 정렬)                                   |
| `feature:stats`      | UI     | 통계(이번 달/올해, 52주 잔디)                              |
| `feature:settings`   | UI     | 테마, 알림 설정                                        |
| `core:designsystem`  | UI     | ManiculeTheme, Color, Typography, 공통 Button/Dialog   |
| `core:ui`            | UI     | BookCard, ContributionGrid 등 feature 간 공유 컴포넌트   |
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

### 3.3 `feature:home`

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
        ├── InProgressSection.kt            # "내 서재 →" 포함
        ├── InProgressEmptyState.kt
        ├── ContributionPreview.kt          # 최근 8주
        └── TodaySummaryCard.kt
```

### 3.4 `feature:search`

```
feature/search/
└── src/main/java/com/example/note/feature/search/
    ├── SearchRoute.kt
    ├── SearchScreen.kt
    ├── SearchViewModel.kt
    ├── SearchUiState.kt                    # Idle / Loading / Results / Empty / Error
    ├── navigation/
    │   └── SearchNavigation.kt
    └── components/
        ├── SearchTextField.kt
        ├── RecentQueriesList.kt
        ├── BookSearchResultItem.kt
        └── EmptyResultPrompt.kt            # "바코드가 있나요?" 안내
```

### 3.5 `feature:scanner`

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
        ├── ViewfinderOverlay.kt
        └── PermissionDeniedView.kt
```

### 3.6 `feature:bookdetail`

```
feature/bookdetail/
└── src/main/java/com/example/note/feature/bookdetail/
    ├── BookDetailRoute.kt
    ├── BookDetailScreen.kt
    ├── BookDetailViewModel.kt
    ├── BookDetailUiState.kt                # Book + Records + Status + Rating + Memo
    ├── BookDetailUiEvent.kt                # ChangeStatus, AddRecord, EditRecord, ...
    ├── navigation/
    │   └── BookDetailNavigation.kt         # 인자: isbn:String
    └── components/
        ├── BookHeader.kt                   # 표지·제목·저자 (즉시 표시)
        ├── BookPublishInfoSection.kt       # 페이지 수·가격·분류 (즉시 표시)
        ├── BookDescriptionSection.kt       # 책 소개 (introductionUrl fetch)
        ├── BookTocSection.kt               # 목차 (tableOfContentsUrl fetch)
        ├── StatusSelector.kt               # 읽고싶음/읽는 중/완독
        ├── RatingMemoEditor.kt
        ├── ReadingRecordList.kt            # 날짜별 기록
        ├── AddRecordSheet.kt               # 날짜+페이지 입력
        └── FinishConfirmDialog.kt          # "혹시 책을 다 읽으셨나요?"
```

### 3.7 `feature:library`

```
feature/library/
└── src/main/java/com/example/note/feature/library/
    ├── LibraryRoute.kt
    ├── LibraryScreen.kt
    ├── LibraryViewModel.kt
    ├── LibraryUiState.kt                   # selectedTab, sort, books
    ├── navigation/
    │   └── LibraryNavigation.kt
    └── components/
        ├── StatusTabRow.kt                 # 전체/읽고싶음/읽는 중/완독
        ├── SortMenu.kt                     # 수정/추가/별점
        ├── LibraryBookCard.kt              # 진도 표시 포함
        └── DeleteConfirmDialog.kt          # "기록한 내용이 모두 삭제되어요"
```

### 3.8 `feature:stats`

```
feature/stats/
└── src/main/java/com/example/note/feature/stats/
    ├── StatsRoute.kt
    ├── StatsScreen.kt
    ├── StatsViewModel.kt
    ├── StatsUiState.kt                     # period, summary, contribution, selectedDay
    ├── navigation/
    │   └── StatsNavigation.kt              # 인자: focus:String? (잔디 위치 스크롤용)
    └── components/
        ├── PeriodSelector.kt               # 이번 달 / 올해
        ├── SummaryCards.kt                 # 완독 권수, 페이지 수
        ├── ContributionGrid52w.kt          # 52 × 7, 가로 스크롤
        └── SelectedDayRecords.kt
```

### 3.9 `feature:settings`

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
        ├── ThemeRadioGroup.kt              # 다크/라이트/시스템
        ├── ReminderToggle.kt
        └── ReminderTimePicker.kt
```

---

## 4. Core 모듈 파일 구조

### 4.1 `core:designsystem`

```
core/designsystem/
└── src/main/java/com/example/note/core/designsystem/
    ├── theme/
    │   ├── Color.kt                        # Material 3 고정 브랜드 컬러 (라이트/다크)
    │   ├── Type.kt                         # Noto Sans KR (Downloadable Fonts) + 시스템 폰트 fallback
    │   ├── Shape.kt
    │   └── ManiculeTheme.kt                # MaterialTheme 래퍼, 다크/라이트
    ├── component/
    │   ├── ManiculeButton.kt
    │   ├── ManiculeTextField.kt
    │   ├── ManiculeDialog.kt                   # 공통 다이얼로그 (이름·메시지·확인/취소)
    │   ├── ManiculeTopAppBar.kt
    │   ├── ManiculeEmptyState.kt
    │   └── ManiculeLoading.kt
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
    │   ├── BookListItem.kt
    │   └── BookProgressBar.kt              # 132 / 320쪽 표시
    ├── contribution/
    │   ├── ContributionCell.kt
    │   └── ContributionGrid.kt             # 잔디 공통 그리드
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
    ├── ReadingRecord.kt                    # id, isbn, date, cumulativePage
    ├── DailyReading.kt                     # 통계용 (date, pages)
    ├── ContributionDay.kt                  # 잔디 한 칸 (date, intensity)
    ├── ReadingStreak.kt
    ├── PeriodSummary.kt                    # 완독 권수, 페이지 수
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
    │   ├── ChangeReadingStatusUseCase.kt    # 완독 시 finishedAt 저장 규칙 포함
    │   ├── DeleteBookEntryUseCase.kt
    │   └── UpdateRatingMemoUseCase.kt
    ├── record/
    │   ├── AddReadingRecordUseCase.kt       # 읽고싶음→읽는 중 자동 전환, 40쪽 이하 신호 반환
    │   ├── EditReadingRecordUseCase.kt
    │   ├── DeleteReadingRecordUseCase.kt
    │   └── ObserveBookRecordsUseCase.kt
    ├── stats/
    │   ├── GetTodaySummaryUseCase.kt
    │   ├── GetPeriodSummaryUseCase.kt       # 이번 달 / 올해
    │   ├── GetContributionUseCase.kt        # 365일 잔디
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
    │   ├── ReadingRecordEntity.kt          # id, isbn, date, cumulativePage
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
    ├── di/
    │   └── DataStoreModule.kt
    ├── UserPreferencesDataStore.kt         # Preferences DataStore 래퍼
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
| 홈      | `feature:home`       | GetLibraryBooksUseCase(읽는 중), GetTodaySummaryUseCase, GetReadingStreakUseCase, GetContributionUseCase                                                         |
| 검색     | `feature:search`     | SearchBooksUseCase, GetRecentQueriesUseCase, SaveRecentQueryUseCase                                                                                           |
| 바코드 스캔 | `feature:scanner`    | GetBookByScanUseCase (BarcodeScanner 인터페이스는 core:scanner, core:domain이 의존)                                                                                    |
| 책 상세   | `feature:bookdetail` | GetBookDetailUseCase, ObserveBookEntryUseCase, ChangeReadingStatusUseCase, UpdateRatingMemoUseCase, AddReadingRecordUseCase, EditReadingRecordUseCase, DeleteReadingRecordUseCase, ObserveBookRecordsUseCase |
| 서재     | `feature:library`    | GetLibraryBooksUseCase, DeleteBookEntryUseCase                                                                                                                |
| 통계     | `feature:stats`      | GetPeriodSummaryUseCase, GetContributionUseCase                                                                                                               |
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
        ├── AndroidLintConventionPlugin.kt           # ktlint + detekt 적용 (모든 모듈 공통)
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
- **재사용**: `core:ui` 의 `ContributionGrid` 를 홈(8주 미리보기)·통계(52주)에서 동일 구현으로 사용.
- **테스트 용이성**: `core:model`, `core:common`은 순수 JVM 모듈. `core:domain` UseCase, `core:data` Repository는 Fake 구현체로 Android 환경 없이 단위 테스트 가능.
- **Offline-first 단순화**: Repository SSOT 원칙으로 UI는 항상 Room의 Flow만 구독.
