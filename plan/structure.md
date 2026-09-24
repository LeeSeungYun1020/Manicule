# 독서 기록 앱 — 모듈 구조

> 모듈 책임·의존 방향·공용 경계와 현재 주요 파일의 기준. [Android App Architecture](https://developer.android.com/topic/architecture) 준수.

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
│    core:datastore, core:scanner)               │
└────────────────────────────────────────────────┘
```

`core:notifications`는 Android 시스템 진입점과 WorkManager 구현을 소유하는 플랫폼 모듈이다.
`app`에서 조립하며 `core:domain`이 정의한 알림 계약을 구현한다.

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
| `core:ui`            | UI     | BookCover, ReadingCalendarGrid 등 feature 간 공유 컴포넌트  |
| `core:common`        | -      | Dispatcher 정의, Result 래퍼, 날짜 유틸                  |
| `core:model`         | Domain | Book, ReadingStatus, ReadingRecord 등             |
| `core:domain`        | Domain | UseCase (AddReadingRecord, GetStreak 등)          |
| `core:data`          | Data   | Repository 구현, DTO/Entity ↔ Model 매퍼             |
| `core:database`      | Data   | Room Database, DAO, Entity                       |
| `core:datastore`     | Data   | UserPreferences (테마, 알림)                         |
| `core:network`       | Data   | 국립중앙도서관 ISBN API 클라이언트                           |
| `core:scanner`       | Data   | CameraX + ML Kit 기반의 바코드 분석기 및 원천 데이터 제공         |
| `core:notifications` | Platform | WorkManager 기반 리마인더 예약·발송, 알림 채널 (`core:domain` 계약 구현) |

### 2.2 모듈 의존 그래프

```
app
├── feature:* ───────────────┬──> core:domain ──┬──> core:data ──┬──> core:network
│                            │                   │                 ├──> core:database
│                            │                   │                 └──> core:datastore
│                            │                   └──> core:scanner
│                            ├──> core:designsystem
│                            └──> core:ui ───────────> core:designsystem
└── core:notifications ─────────> core:domain

core:data, core:domain, core:ui ──> core:model / core:common
```

---

## 3. Feature 경계와 주요 파일

아래는 책임 경계를 찾기 위한 주요 파일이며 전체 파일 목록이나 새 파일명 지시가 아니다. 현재 위치는 `rg --files <모듈>`로 확인한다. 패키지 루트는 `com.leeseungyun1020.manicule`; `core:ui`만 `src/main/java`, 나머지는 `src/main/kotlin`을 사용한다.

### 3.1 `app`

```
app/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    └── kotlin/com/leeseungyun1020/manicule/
        ├── ManiculeApplication.kt              # @HiltAndroidApp
        ├── MainActivity.kt                  # 단일 Activity, 루트 ManiculeTheme
        └── navigation/
            ├── ManiculeNavHost.kt               # 최상위 NavHost (NavController 소유 및 실제 stack mutation 조립)
            ├── TopLevelDestination.kt       # feature route를 사용하는 홈/서재/통계/설정 4개 탭
            ├── ManiculeApp.kt                # Scaffold·하단 탭 조립
            └── ManiculeAppState.kt          # rememberManiculeAppState
```

### 3.2 Feature 모듈 공통 구조

각 feature의 `navigation/<Name>Navigation.kt`는 public route 타입, destination 및 필수 콜백 계약을 소유한다. 화면이 구현된 feature는 필요에 따라 Route, Screen, ViewModel, UiState와 전용 컴포넌트로 나눈다.

#### Navigation 아키텍처 및 책임 경계

- **Feature는 NavController 비의존**: feature Composable은 `NavController`를 직접 받지 않고 콜백만 호출한다. production 콜백에 기본 빈 람다(`{}`)를 두지 않는다 (Preview/테스트만 허용, 미지원 이동은 UI availability로 모델링).
- **Destination-local 이동**: 현재 화면 종료 후 기존 history로 복귀(뒤로가기, 닫기 등). feature는 필수 콜백을 소유하고, `app`의 최소 연결(`popBackStack()` 등)은 해당 destination을 노출하는 feature PR에서 완료한다. 단, pop 실패·deep link fallback 정책은 app이 소유한다.
- **Cross-destination 이동**: 다른 destination으로 이동(검색·책 상세 등). source는 의도/인자 콜백, target은 route 타입을 소유하며, 준비 즉시 `app/`의 `ManiculeNavHost`에서 점진 연결한다.
- **App 계층의 백스택 소유**: `app`의 `ManiculeNavHost`가 `NavController`, 실제 백스택 조작, `popUpTo`, `launchSingleTop`, 상태 복원 정책을 소유한다.

### 3.3 Feature별 주요 파일

경로는 각 `feature/<name>/src/main/kotlin/com/leeseungyun1020/manicule/feature/<name>/`를 기준으로 한다. 미구현 화면의 예정 파일은 나열하지 않는다.

| 모듈 | 현재 주요 파일 | 책임 |
|---|---|---|
| `feature:home` | `navigation/HomeNavigation.kt`, `HomeScreen.kt`, `HomeViewModel.kt`, `HomeSearchTopBar.kt` | 홈 상태 조합과 검색·스캔·서재·통계 이동 의도 |
| `feature:search` | `navigation/SearchNavigation.kt`, `SearchRoute.kt`, `SearchViewModel.kt`, `components/SearchResultList.kt` | 최근 검색어, 입력 필터, Paging 결과 |
| `feature:scanner` | `navigation/ScannerNavigation.kt`, `ScannerViewModel.kt`, `CameraPreview.kt` | 권한·카메라 수명주기와 도서 조회 결과 |
| `feature:bookdetail` | `navigation/BookDetailNavigation.kt`, `BookDetailRoute.kt`, `BookDetailViewModel.kt`, `components/AddRecordBottomSheet.kt` | ISBN 진입, 독서 상태·리뷰·기록 편집 |
| `feature:library` | `navigation/LibraryNavigation.kt`, `LibraryRoute.kt`, `LibraryViewModel.kt`, `components/SortBottomSheet.kt` | 상태 탭, 정렬, 책 변경·삭제 |
| `feature:stats` | `navigation/StatsNavigation.kt` | `StatsRoute(focus)` 진입 계약 |
| `feature:settings` | `navigation/SettingsNavigation.kt`, `SettingsRoute.kt`, `SettingsViewModel.kt`, `components/ReminderSection.kt` | 테마·리마인더 설정 |

서재 새 진입은 `LibraryRoute()`의 `READING`을 기본으로 하며, 홈 '고르기'는 `LibraryRoute(LibraryTab.WANT)`로 진입한다. `initialTab`은 새 백스택 항목의 초기값이다. 기존 화면을 복원할 때는 저장된 사용자 선택을 유지하므로, '고르기' 연결 시 기존 항목을 `restoreState`로 복원하지 않는다.

---

## 4. Core 모듈 주요 파일

각 절의 파일명은 해당 모듈 소스 패키지를 기준으로 한다. 내부 구현 파일과 전체 컴포넌트 목록은 `rg --files core/<name>`로 확인한다.

### 4.1 `core:designsystem`

`theme/ManiculeTheme.kt`가 앱 테마를, `theme/Color.kt`·`Dimension.kt`·`Type.kt`가 공용 토큰을 소유한다. 공용 화면 컴포넌트는 `component/`, 아이콘은 `icon/ManiculeIcons.kt`에 둔다.

### 4.2 `core:ui`

소스 위치는 `src/main/java/com/leeseungyun1020/manicule/core/ui/`다. `book/BookCover.kt`·`BookListItem.kt`·`BookProgressBar.kt`와 `calendar/ReadingCalendarGrid.kt`가 feature 간 공유 UI를 소유한다. Preview 데이터는 `preview/`에 둔다.

### 4.3 `core:common`

`di/DispatchersModule.kt`와 `time/Clock.kt`가 실행·시간 추상화를 제공한다. 공통 `Result`와 날짜·Flow 확장은 각각 `result/`, `time/`, `ext/`에 둔다.

### 4.4 `core:model`

`Book.kt`, `BookEntry.kt`, `ReadingRecord.kt`, `ReadingCalendarDay.kt`, `UserPreferences.kt` 등 도메인 모델을 소유한다.

### 4.5 `core:domain`

`book/`, `search/`, `scanner/`, `library/`, `record/`, `stats/`, `settings/`에 기능별 UseCase를 둔다. 공용 상태 변경은 `library/ChangeReadingStatusUseCase.kt`, 스캔 후보 조회는 `scanner/GetBookByScanUseCase.kt`, 리마인더 계약은 `settings/ReminderScheduler.kt`가 소유한다.

### 4.6 `core:data`

`repository/BookRepository.kt`·`LibraryRepository.kt`·`StatsRepository.kt` 등의 공개 계약과 구현, `datasource/`의 로컬·원격 연결, `mapper/`의 DTO/Entity 변환을 소유한다. Repository 밖으로 DTO·Entity를 노출하지 않는다.

### 4.7 `core:database`

`ManiculeDatabase.kt`, `dao/BookEntryDao.kt`·`ReadingRecordDao.kt`·`ReadingRecordStatsDao.kt`, `entity/`와 `converter/Converters.kt`가 Room 저장소를 구성한다. 실제 쿼리와 스키마는 이 파일들과 생성된 schema JSON을 확인한다.

### 4.8 `core:datastore`

`UserPreferencesDataStore.kt`와 `PreferencesKeys.kt`가 테마·리마인더 설정을 저장한다. `di/DataStoreModule.kt`가 인스턴스를 제공한다.

### 4.9 `core:network`

`nlk/NlkApi.kt`가 국립중앙도서관 API 계약을, `nlk/NlkAuthInterceptor.kt`와 `nlk/dto/`가 인증·응답 변환을 소유한다. Retrofit 구성은 `di/NetworkModule.kt`에 둔다.

### 4.10 `core:scanner`

`BarcodeReader.kt`·`BarcodeReaderFactory.kt`가 계약, `DemandDrivenBarcodeReader.kt`·`MlKitBarcodeReaderFactory.kt`가 CameraX–ML Kit 구현을 소유한다.

`BarcodeReader`는 `ImageAnalysis`를 소유한다. 첫 `getBarcodes()` 대기자가 생기면 analyzer를 연결하고, 마지막 대기자가 반환·실패·취소되면 `clearAnalyzer()`로 중단한다. 동시 호출은 같은 프레임을 각 predicate로 평가하고 과거 결과는 replay하지 않는다. 최초 일치 프레임의 non-null `Barcode.rawValue`를 원문 그대로 전달하며 ISBN 체크섬·접두사·정규화나 바코드 포맷 제한을 적용하지 않는다. 도서 조회 성공 여부는 Domain 흐름이 결정한다.

### 4.11 `core:notifications`

`WorkManagerReminderScheduler.kt`는 도메인 `ReminderScheduler`를 구현한다. `ReminderWorker.kt`는 발송 시점에 `GetReminderContentUseCase`를 호출하고, `ReminderNotificationPublisher.kt`가 메시지를 게시한다. `TimeZoneChangedReceiver.kt`는 시간대 변경 시 재예약한다.

의존 방향은 `app → core:notifications → core:domain`이다. `core:notifications`는 `core:data` Repository를 직접 주입하지 않는다.

---

## 5. Gradle 구성

Convention plugin은 `build-logic/convention/src/main/kotlin/`에 둔다. `AndroidFeatureConventionPlugin.kt`, `AndroidApplicationComposeConventionPlugin.kt`, `AndroidLibraryComposeConventionPlugin.kt`, `AndroidHiltConventionPlugin.kt`, `AndroidRoomConventionPlugin.kt`, `AndroidLintConventionPlugin.kt`, `JvmLibraryConventionPlugin.kt`가 주요 진입점이다.

모듈별 적용 plugin과 현재 의존성은 각 `build.gradle.kts`에서 확인한다. 의존 방향의 설계 기준은 이 문서의 모듈 의존 그래프다.

## 6. 테스트 전략

| 모듈                   | 주요 테스트                                                             |
|----------------------|--------------------------------------------------------------------|
| `core:common`        | Result 래퍼 동작, DateExt 주 계산, FlowExt 변환 단위 테스트 (JVM)                  |
| `core:domain`        | UseCase 단위 테스트 (FakeRepository, 코루틴 Test)                          |
| `core:data`          | Repository 단위 테스트 (FakeDao, FakeApi), 매퍼 테스트                       |
| `core:database`      | Room in-memory DAO 테스트 (instrumented)                              |
| `core:datastore`     | TestDataStore 기반 UserPreferences 읽기/쓰기 테스트                         |
| `core:network`       | MockWebServer 기반 NlkApi 테스트                                        |
| `core:scanner`       | rawValue·predicate, 동시 호출 공유, 호출 수 기반 시작·중단·재시작, 오류·close 테스트       |
| `core:notifications` | WorkManager 기반 알림 예약 및 스케줄링 검증                                     |
| `core:designsystem`  | 공통 컴포넌트(Button, Dialog 등) Compose UI 테스트(`createComposeRule`) |
| `core:ui`            | BookCover, BookListItem, BookProgressBar, ReadingCalendarGrid Compose UI 테스트 |
| `feature:*`          | ViewModel StateFlow 검증, Compose UI 테스트(`createAndroidComposeRule`) |
| `app`                | Navigation 통합 테스트                                                  |
