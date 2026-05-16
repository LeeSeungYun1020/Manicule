# Manicule — 구현 진행 현황

> [order.md](order.md) 의 6단계 구현 전략에 따라 진행한 작업 로그.
> 새 세션에서 이어서 작업할 때 이 문서를 먼저 읽고 "다음 작업" 섹션부터 진행한다.

---

## 진행 단계 요약

| 단계 | 이름 | 상태 |
|---|---|---|
| 1 | 기반 시스템 (Foundation) | ✅ 완료 |
| 2 | 데이터 엔진 & 공통 인프라 (Core Engine) | ⏳ 다음 |
| 3 | 핵심 피처 버티컬 슬라이싱 (Vertical Slices) | ⏳ |
| 4 | 시스템 연동 슬라이싱 (System Integration Slices) | ⏳ |
| 5 | App 조립 (App Assembly) | ⏳ |
| 6 | 검증 (Verification) | ⏳ |

---

## ✅ 완료한 작업

### 1단계 — 기반 시스템 (Foundation)

#### 1-1. 프로젝트 루트 구성
- `settings.gradle.kts` — pluginManagement / dependencyResolutionManagement / TYPESAFE_PROJECT_ACCESSORS,
  현재는 `:app`, `:core:common`, `:core:designsystem`, `:core:model` 만 활성. 후속 단계에서 주석 해제
- `build.gradle.kts` — plugin alias 만 선언 (apply false)
- `gradle.properties` — JVM args, AndroidX, parallel/caching/configuration-cache 활성화
- `gradle/libs.versions.toml` — Kotlin 2.3.21, AGP 9.0.0, Compose BoM 2025.02.00, Room 2.8.4, Retrofit 2.11.0,
  Coil 3.0.0-rc01, ML Kit Barcode, CameraX 1.6.0, Material3 1.4.0, kotlinx.datetime, Hilt 2.59.2, KSP, Paging 3, Desugar,
  Firebase BoM 34.13.0 (firebase-analytics / firebase-crashlytics 라이브러리 버전 통일) 등
  (NowInAndroid 패턴 참고). Convention plugin alias 도 함께 선언
- `.gitignore` — Gradle/IDE/Keystore/Native 빌드 산출물 제외 (build-logic stub 만 예외 처리)

#### 1-2. build-logic Convention Plugin
- `build-logic/settings.gradle.kts` — 별도 빌드, 루트의 libs catalog 공유
- `build-logic/convention/build.gradle.kts` — kotlin-dsl, plugin 등록 (ktlint/detekt/Firebase classpath 포함)
- 등록된 플러그인 (10개) — structure.md 6장의 9종 + Application Compose 분리:
  - `manicule.android.application` (`AndroidApplicationConventionPlugin`) — `manicule.android.lint` 자동 적용
  - `manicule.android.application.compose` (`AndroidApplicationComposeConventionPlugin`)
  - `manicule.android.library` (`AndroidLibraryConventionPlugin`) — `manicule.android.lint` 자동 적용
  - `manicule.android.library.compose` (`AndroidLibraryComposeConventionPlugin`)
  - `manicule.android.feature` (`AndroidFeatureConventionPlugin`) — 후속 feature 모듈에서 사용
  - `manicule.android.hilt` (`AndroidHiltConventionPlugin`)
  - `manicule.android.room` (`AndroidRoomConventionPlugin`) — 후속 단계 `core:database` 에서 사용
  - `manicule.android.lint` (`AndroidLintConventionPlugin`) — ktlint + detekt + Android Lint, 모든 모듈 공통 적용
  - `manicule.android.application.firebase` (`AndroidApplicationFirebaseConventionPlugin`) — google-services + Crashlytics
    플러그인 + Firebase BoM(34.13.0) 기반 `firebase-analytics` / `firebase-crashlytics` 의존성 자동 추가.
    Firebase 콘솔 연결 후 `app` 에서 명시적 적용 (현재는 미적용 상태로 유지)
  - `manicule.jvm.library` (`JvmLibraryConventionPlugin`) — `core:model` 전용, `manicule.android.lint` 자동 적용
- 공통 헬퍼:
  - `AndroidCommon.kt` — JDK 17, coreLibraryDesugaring, opt-in 모음
  - `AndroidCompose.kt` — Compose BoM, ui/foundation/material3 등 implementation 일괄 추가
- 정적 분석 설정 파일:
  - `.editorconfig` (루트) — ktlint 의 코드 스타일 정의 (max line 140, trailing comma, `@Composable` 함수명 예외)
  - `config/detekt/detekt.yml` (루트) — detekt: `style` / `naming` 비활성화(컨벤션은 ktlint), `complexity`·`empty-blocks`·`potential-bugs` 등만 오버라이드
  - `gradle/libs.versions.toml` — `ktlint`(ktlint-gradle 플러그인)와 `ktlintCli`(실행 CLI, 예: 1.8.0) 버전 분리; `detekt-formatting` 미사용

#### 1-3. core:model (JVM 모듈)
패키지: `com.leeseungyun1020.manicule.core.model`
- `Book` — NLK ISBN API 매핑 (EA_ISBN/TITLE/AUTHOR/PUBLISHER/PUBLISH_PREDATE/TITLE_URL/PAGE/PRE_PRICE/SUBJECT
  /BOOK_TB_CNT_URL/BOOK_INTRODUCTION_URL/BOOK_SUMMARY_URL)
- `ReadingStatus` (WANT/READING/FINISHED)
- `BookEntry` — Book + status + rating(1..5) + memo + addedAt/updatedAt + finishedAt + currentPage
- `ReadingRecord` — id, isbn, date, cumulativePage (누적 페이지 모델)
- `DailyReading`, `ContributionDay` (intensity 0..4 양자화 규칙: 0/1-19/20-49/50-99/100+)
- `ReadingStreak`, `PeriodSummary` (with `StatsPeriod`)
- `UserPreferences` (`ThemeMode`, `ReminderConfig`)
- `SearchQuery`, `TodaySummary`
- 단위 테스트: `ContributionDayTest`

#### 1-4. core:common (Android Library + Hilt)
패키지: `com.leeseungyun1020.manicule.core.common`
- `di/Dispatchers.kt` — `@Dispatcher(IO|Default)` Qualifier + enum
- `di/DispatchersModule.kt` — `@Provides` 로 IO/Default Dispatcher 바인딩
- `di/ApplicationScope.kt` — `@ApplicationScope` Qualifier + `CoroutineScopesModule`
- `result/Result.kt` — `sealed Loading/Success/Error` + `Flow<T>.asResult()` 확장
- `time/Clock.kt`, `time/SystemClock` — 테스트 용 시간 추상화 (`@Inject`)
- `time/ClockModule.kt` — Clock 바인딩
- `time/DateExt.kt` — `WeekStart(Sunday)`, `startOfWeek/Month/Year`, `endOfMonth/Year`,
  `daysUntilInclusive`, `dateRangeInclusive`
- `ext/FlowExt.kt` — `defaultIfEmpty`
- 단위 테스트: `ResultTest`, `DateExtTest`

#### 1-5. core:designsystem (Android Library + Compose)
패키지: `com.leeseungyun1020.manicule.core.designsystem`
- `theme/Color.kt` — Manicule 브랜드 컬러(Brown/Beige/Ink + Paper),
  Light/Dark ColorScheme, `GrassLight/GrassDark` 5단계 잔디 색상
- `theme/Type.kt` — Noto Sans KR Downloadable Fonts (`provider="com.google.android.gms.fonts"`),
  Typography 한국어 line-height 조정
- `theme/Shape.kt` — RoundedCornerShape 4/8/12/16/24
- `theme/ManiculeTheme.kt` — MaterialTheme 래퍼, `LocalGrassColors` CompositionLocal
- 공통 컴포넌트 (component/):
  - `ManiculeButton`, `ManiculeOutlinedButton`, `ManiculeTextButton`
  - `ManiculeTextField` (OutlinedTextField 래퍼)
  - `ManiculeDialog` (책 삭제·완독 확인·스캔 실패 다이얼로그가 모두 사용)
  - `ManiculeTopAppBar`
  - `ManiculeEmptyState`, `ManiculeLoading`
- `icon/ManiculeIcons.kt` — 공용 아이콘 alias + 하단 탭 아이콘 (Home/Library/Stats/Settings)
- `res/values/font_certs.xml` — Google Fonts provider certificates

#### 1-6. app 스켈레톤
패키지: `com.leeseungyun1020.manicule`
- `ManiculeApplication` — `@HiltAndroidApp`
- `MainActivity` — `enableEdgeToEdge()`, splashScreen, ManiculeTheme + Surface,
  WindowSizeClass 계산, `rememberManiculeAppState`, `ManiculeApp` 호출
- `navigation/TopLevelDestination` — HOME/LIBRARY/STATS/SETTINGS 4개 탭 정의
- `navigation/ManiculeAppState` — NavController, currentTopLevelDestination,
  `navigateToTopLevelDestination` (singleTop + restoreState + popUpTo)
- `navigation/ManiculeNavHost` — stub destination 등록 (후속 단계에서 feature graph 로 교체)
- `navigation/ManiculeApp` — Scaffold + NavigationBar (BottomBar)
- `AndroidManifest.xml` — INTERNET / CAMERA / POST_NOTIFICATIONS, allowBackup=true, splash 테마
- `res/values/strings.xml`, `res/values/themes.xml`

---

## ⏳ 다음 작업

### 2단계 — 데이터 엔진 & 공통 인프라 (Core Engine)

작업 순서 (병렬 가능 항목은 동시에 진행):

1. **`core:database`** (Room) — 병렬 가능
   - `settings.gradle.kts` 의 `:core:database` 주석 해제, `manicule.android.library` + `manicule.android.hilt`
     + `manicule.android.room` 적용
   - `entity/`: `BookEntity`, `BookEntryEntity`, `ReadingRecordEntity`, `RecentQueryEntity`
   - `dao/`: `BookDao`, `BookEntryDao` (status별 Flow + 정렬 쿼리), `ReadingRecordDao`
     (날짜 범위 / 잔디 집계), `RecentQueryDao`
   - `converter/Converters.kt` — LocalDate, ReadingStatus
   - `migration/Migrations.kt`
   - `ManiculeDatabase` — `@Database`, version 1, 스키마 export
   - `di/DatabaseModule.kt`
   - **테스트**: Room in-memory DAO 테스트 (instrumented)

2. **`core:network`** (Retrofit + NLK API) — 병렬 가능
   - `nlk/NlkApi.kt` (Retrofit interface), `NlkAuthInterceptor`,
     `dto/NlkSearchResponseDto.kt`, `dto/NlkBookDto.kt`
   - `BuildConfigKeys.kt`, `di/NetworkModule.kt`
   - 모듈 `build.gradle.kts` 에서 OkHttp / Retrofit BoM 을 직접 선언:
     `implementation(platform(libs.okhttp.bom))`, `implementation(platform(libs.retrofit.bom))`
   - **테스트**: MockWebServer 기반 NlkApi 테스트

3. **`core:datastore`** (Preferences DataStore) — 병렬 가능
   - `UserPreferencesDataStore.kt`, `PreferencesKeys.kt`, `di/DataStoreModule.kt`
   - **테스트**: TestDataStore 기반 읽기/쓰기

4. **`core:data`** (Repository 통합)
   - 인터페이스: `BookRepository`, `ReadingRecordRepository`, `LibraryRepository`,
     `StatsRepository`, `SearchHistoryRepository`, `UserPreferencesRepository`
   - 구현체 + DTO/Entity ↔ Model 매퍼 (`mapper/`)
   - `di/DataModule.kt` (Repository 바인딩)
   - **테스트**: FakeDao/FakeApi 기반 Repository 테스트, 매퍼 테스트

5. **`core:domain`** 베이스 구조 (UseCase 는 슬라이스에서 추가)
   - `di/DomainModule.kt`
   - `book/`, `search/`, `library/`, `record/`, `stats/`, `settings/`, `scanner/` 패키지 골격

6. **`core:ui`**
   - `book/BookCover.kt` (Coil 3.x AsyncImage 래퍼, fallback 처리)
   - `book/BookListItem.kt`, `book/BookProgressBar.kt` (132/320쪽)
   - `contribution/ContributionCell.kt`, `contribution/ContributionGrid.kt` (홈 8주·통계 52주 공유)
   - `preview/BookPreviewParameterProvider.kt`
   - 모듈 `build.gradle.kts` 에서 Coil BoM 을 직접 선언:
     `implementation(platform(libs.coil.bom))`

### 2단계 완료 후 — 3단계 Slice 1 (`feature:search`)

- DAO·Repository 검색 메서드 / NLK API PagingSource
- `SearchBooksUseCase` (`Flow<PagingData<Book>>`), `GetRecentQueriesUseCase`, `SaveRecentQueryUseCase`
- SearchScreen + ViewModel (디바운스 350ms, `collectAsLazyPagingItems`)
- 검증 기준: 키워드 검색·무한 스크롤·최근 검색어 저장/재실행

---

## 작업 시 참고 사항

### 모듈 추가 절차 (반복 패턴)

1. `settings.gradle.kts` 에서 해당 `include(":...")` 주석 해제
2. 모듈 폴더 생성 + `build.gradle.kts` 작성 (적절한 convention plugin alias 적용)
3. (Android 라이브러리인 경우) `src/main/AndroidManifest.xml` 빈 manifest 추가
4. 패키지 루트는 `com.leeseungyun1020.manicule.<group>.<module>` 로 통일
5. `app/build.gradle.kts` 에서 필요한 implementation(projects.…) 주석 해제

### 핵심 원칙 (order.md 1절)

- **Standard First** — 표준은 변경 빈도가 높으면 전체 파급. 후속 단계 전에 안정화
- **Vertical Slicing** — feature 단위로 Data → Domain → UI 한 번에 관통
- **Offline-first** — Room SSOT, UI 는 Flow 만 구독
- **Test-Driven** — 각 레이어/슬라이스 단위 테스트 동반 작성

### 패키지/네이밍 규칙

- 패키지 루트: `com.leeseungyun1020.manicule`
- 디자인 시스템 컴포넌트 prefix: `Manicule*` (예: `ManiculeButton`, `ManiculeDialog`)
- ViewModel 의 UiState 는 sealed interface 또는 data class — feature 모듈 패턴
- ReadingRecord 는 "누적" 페이지를 저장 (`cumulativePage`), 당일 페이지는 차분으로 계산

### 알려진 보류 / 향후 처리 사항

- BuildConfig 키 (`NLK_AUTH_KEY` 등) 는 `core:network` 작성 시 `local.properties` 로딩 방식 결정 후 추가
- 잔디 셀 크기·간격 등 디자인 토큰은 `core:ui` 의 ContributionGrid 작성 시 결정
- `app:run` 동작 확인은 2단계 완료 시점에 한 번 더 점검 (Hilt 그래프 누락 검증)

---

## 빠른 디렉터리 트리

```
Manicule/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── .editorconfig                # ktlint 코드 스타일
├── gradle/libs.versions.toml
├── config/
│   └── detekt/detekt.yml        # detekt 규칙 오버라이드
├── build-logic/
│   └── convention/
│       ├── build.gradle.kts
│       └── src/main/kotlin/
│           ├── AndroidApplicationConventionPlugin.kt
│           ├── AndroidApplicationComposeConventionPlugin.kt
│           ├── AndroidApplicationFirebaseConventionPlugin.kt
│           ├── AndroidLibraryConventionPlugin.kt
│           ├── AndroidLibraryComposeConventionPlugin.kt
│           ├── AndroidFeatureConventionPlugin.kt
│           ├── AndroidHiltConventionPlugin.kt
│           ├── AndroidRoomConventionPlugin.kt
│           ├── AndroidLintConventionPlugin.kt
│           ├── JvmLibraryConventionPlugin.kt
│           └── com/leeseungyun1020/manicule/buildlogic/
│               ├── AndroidCommon.kt
│               └── AndroidCompose.kt
├── app/                        # ✅ 1단계
│   ├── build.gradle.kts
│   └── src/main/{AndroidManifest.xml, kotlin/, res/}
└── core/
    ├── common/                 # ✅ 1단계
    ├── designsystem/           # ✅ 1단계
    └── model/                  # ✅ 1단계
```

## Lint / 정적 분석 사용법

Foundation 단계에서 ktlint + detekt + Android Lint 를 모든 모듈에 자동 적용했다.
**역할 분리**: 컨벤션·포맷은 ktlint(`.editorconfig`), detekt는 기본 규칙 중 스타일·네이밍 세트를 끄고 복잡도·잠재 버그 등에 집중한다.
모듈별 별도 설정 없이 다음 태스크를 사용할 수 있다.

| 태스크 | 설명 |
|---|---|
| `./gradlew ktlintCheck` | ktlint 검사 (전체 모듈) |
| `./gradlew ktlintFormat` | ktlint 자동 수정 |
| `./gradlew detekt` | detekt 정적 분석 (전체 모듈) |
| `./gradlew lint` | Android Lint (Android 모듈만) |
| `./gradlew check` | 위의 모든 검사 + 단위 테스트 통합 실행 |

규칙은 `.editorconfig` 와 `config/detekt/detekt.yml` 에서 조정한다.
ktlint 위반 발견 시 우선 `ktlintFormat` 으로 자동 수정 가능한 항목을 처리하고,
detekt 위반은 코드를 직접 고치거나 부득이한 경우 `@Suppress("DetektRule")` 로 억제한다.
