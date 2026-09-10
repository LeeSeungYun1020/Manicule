# UI/UX 개발 가이드라인

> UI 구현 시 반드시 본 문서와 [prototype.html](prototype.html) 을 함께 참조한다.

---

## 1. 프로토타입 참조 규칙

- UI 구현 전 [prototype.html](prototype.html) 에서 해당 화면의 섹션(1\~8)과 변형(a, b, c…)을 확인한다.
- prototype.html은 완벽한 디자인 문서가 아니다. 아래와 충돌 시 **가이드라인을 우선**하고, 구현 전 사용자와 재검토를 진행한다.
    - Material 3 컴포넌트 패턴
    - 프로젝트 테마 토큰 (`Color.kt`, `Type.kt`, `Shape.kt`)
    - WCAG AA 명암비
    - 터치 타겟 최소 크기
- 재검토 결과 및 결정 사항은 [history/](../../history/README.md) 에 기록한다.

### 확정된 프로토타입 편차

아래는 이미 재검토가 끝난 항목이다. **프로토타입이 아니라 이 표를 따른다.**

| 화면 | 프로토타입 | 확정 사항 | 근거 |
|---|---|---|---|
| 전체 | 앱바 제목 좌측 정렬 | **중앙 정렬**(`CenterAlignedTopAppBar`) + 스크롤 시 접힘 | M3 가이드라인 우선 |
| 1a 홈 | 우측 상단 원형 아바타 | **구현 제외** | `plan.md` 3.1·§4 에 없는 미기획 요소 |
| 6a 서재 | 앱바 안에 정렬 상태 텍스트 | 앱바는 **정렬 아이콘만**, 상태 텍스트는 **탭 아래·목록 위** | M3 중앙 정렬 앱바는 title + action 1개 구성 |
| 1a·7a·8a | 제목이 본문 스크롤 영역 안 | **앱바로 통일** | 화면 간 일관성 |
| 5c 내 기록 | 상태 기본값 '읽고 싶음' | **미등록·UNSET은 미선택**. 상태 탭 시 서재에 추가. WANT 선택 예시는 이미 등록된 책에만 적용 | 사용자 확정: 명시적 상태 선택으로 서재 등록 |

---

## 2. Jetpack Compose 개발 규칙

| 규칙             | 요약                                                                                                                             |
|----------------|--------------------------------------------------------------------------------------------------------------------------------|
| State Hoisting | 상태는 ViewModel 또는 최저 공통 부모에서 관리. 화면 상태는 `sealed interface`나 `data class` 기반의 `UiState` 모델로 묶어 관리. UI 컴포넌트는 stateless로 작성  |
| State 수집       | ViewModel의 `StateFlow` 수집 시 리소스 낭비를 막기 위해 `collectAsStateWithLifecycle()` 사용                                                 |
| Composable 컨벤션 | Unit 반환 함수는 PascalCase. 첫 번째 선택 파라미터로 `modifier: Modifier = Modifier` 수용. 루트 레이아웃에 적용                                          |
| 재구성 최적화        | 컬렉션 포함 data class에 `@Immutable` / `@Stable` 사용. 조건부 UI는 `derivedStateOf`, 화면 회전 시 유지될 상태는 `rememberSaveable` 활용              |
| Modifier 순서    | `clickable` → `padding` 순서로 터치 영역 확보. `background` → `padding` 순서로 배경 범위 결정                                                    |
| Side-Effect    | `LaunchedEffect`(suspend 트리거), `DisposableEffect`(리스너 등록/해제), `rememberUpdatedState`(장수명 이펙트 내 최신 콜백). 컴포지션 본문에서 네트워크/DB 호출 금지 |
| 테스트            | `createComposeRule()` 로 UI 테스트. `onNodeWithText`, `performClick`, `assertIsDisplayed` 패턴                                       |

상세: `android docs compose-state`, `android docs compose-side-effects` 또는 [Compose API Guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md) 참조

---

## 3. 모바일 UI/UX 일반 규칙

- **터치 타겟**: 최소 **48 × 48 dp**. 부족 시 `Modifier.minimumInteractiveComponentSize()` 활용. 인접 타겟 간 최소 8dp 간격
- **간격 체계**: 8dp 그리드 (4 / 8 / 16 / 24 / 32 dp). 화면 좌우 패딩 16dp
- **타이포그래피 계층**: M3 15종 텍스트 스타일 (Display / Headline / Title / Body / Label × L/M/S) 준수. 프로젝트는 Noto Sans KR + 한국어 line-height 튜닝 적용 (
  `Type.kt`)
- **색상 명암비**: 일반 텍스트 4.5:1 이상, 대형 텍스트(24sp+) 및 아이콘 3.0:1 이상 (WCAG AA)
- **상태별 UI**: 핵심 콘텐츠는 로딩 / 오류 / 빈 상태를 구분하고, 실패 시 사용자가 인지할 수 있는 오류 UI와 재시도·복구 동작을 제공한다. 보조 콘텐츠 실패는 핵심 흐름을 유지하도록 숨기거나 빈 상태와 동일하게 표현할 수 있지만, 상태 모델에서는 성공한 빈 결과와 오류를 구분하고 자동 재시도 / 수동 재시도 / 화면 재진입 시 재구독 중 하나 이상의 복구 경로를 둔다. `ManiculeEmptyState`는 성공적으로 조회된 빈 결과에 활용한다
- **내비게이션**: 하단 탭 3\~5개. 시스템 뒤로가기 지원. Type-safe Navigation 사용

---

## 4. Material 3 가이드라인

- **테마 래퍼**: 앱 최상위 루트(`MainActivity`)에서 `ManiculeTheme`으로 래핑하여 테마와 CompositionLocal을 제공(개별 Screen/컴포넌트는 중복 래핑 금지). 모든 `@Preview` 함수는 `ManiculePreviewTheme`으로 래핑하여 루트 `Surface`와 콘텐츠 색상 환경을 제공.
- **토큰 접근**: 컴포넌트 내부에서 `MaterialTheme.colorScheme` / `.typography` / `.shapes` 및 `MaterialTheme.spacing`/`size`/`border` 토큰을 통해 접근. 달력 레벨 색상은 `MaterialTheme.maniculeColors.calendarLevels` 사용. 하드코딩 색상(`Color(0xFF...)`) 금지
- **확장 색상 추가 기준**: `ManiculeExtendedColors` 에 필드를 추가하려면 **세 조건을 모두** 만족해야 한다. 하나라도 어긋나면 `colorScheme` 표준 역할이나 컴포넌트 내 `private const` 를 쓴다
    1. `colorScheme` 36개 역할 중 의미가 맞는 것이 없다
    2. 라이트/다크에서 값이 **다르게** 결정된다 (같으면 상수)
    3. 2개 이상의 컴포넌트가 공유한다
    - 예: 표지·카메라 오버레이 딤은 `colorScheme.scrim` + 알파, 차트 막대는 `primary`, 격자선은 `outlineVariant`, 축 레이블은 `onSurfaceVariant` 로 해결된다
- **상단 앱바**: `ManiculeTopAppBar`(중앙 정렬) + `TopAppBarDefaults.enterAlwaysScrollBehavior()` 로 스크롤 시 접힘/복귀. `Scaffold` 에 `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` 연결 필수
    - **탭이 있는 화면**(책 정보·서재)은 탭을 앱바 *안*에 넣지 않는다. `Scaffold(topBar = { Column { ManiculeTopAppBar(...); ManiculeTabRow(...) } })` 구조로 두면 앱바만 접히고 탭은 상단에 남는다
    - **앱바를 쓰지 않는 화면**: 검색(2a~3a, 상단이 `ManiculeSearchBar`), 바코드 스캔 카메라 화면(4a, 카메라 위 떠 있는 뒤로가기 버튼). 스캔 실패·권한 거부(4b·4c)는 앱바를 사용한다
- **컴포넌트 패턴**: M3 표준 컴포넌트 사용 (`TopAppBar`, `BottomSheet`, `SegmentedButton`, `SearchBar`, `Snackbar`, `Card` 등). 커스텀 구현 전 M3 컴포넌트로 대체 가능한지
  확인
- **Elevation**: M3는 그림자 대신 Tonal Elevation(surface 색상 위 틴팅)으로 깊이를 표현. `surfaceContainerLow` ~ `surfaceContainerHighest` 단계 활용
- **모션**: `AnimatedVisibility`, `animateContentSize`, `Crossfade` 로 상태 전환. 화면 전환은 Shared Element Transition 고려
- **적응형 레이아웃**: `WindowSizeClass` 기반 (Compact < 600dp / Medium / Expanded). 프로젝트 구조 참고: [structure.md](../structure.md)

> 상세: [Material 3 공식 가이드라인](https://m3.material.io) 또는 `android docs material3` 참조

---

## 5. 추가 고려사항

- **다크/라이트 모드**: `MaterialTheme.colorScheme` 토큰만 사용하면 자동 대응. Preview에서 양쪽 모드 확인 필수
- **문자열 리소스**: 모든 사용자 노출 텍스트는 `stringResource(R.string.*)` 사용
- **RTL 지원**: `padding(start, end)` 사용 (left/right 금지). 방향성 아이콘은 `Icons.AutoMirrored` 사용
- **LazyColumn 성능**: `items(key = { ... }, contentType = { ... })` 필수 지정
- **이미지**: Coil `AsyncImage` + `crossfade(true)` + placeholder/error fallback. `BookCover` 래퍼 활용
- **이미지 위 텍스트 시인성**: 책 표지 등 이미지 위에 텍스트를 겹쳐 렌더링할 경우, 이미지가 흰색이더라도 글씨가 명확히 보이도록 텍스트 배경에 반드시 반투명 딤(Dim) 처리나 그라디언트 오버레이를 적용한다.
- **상태 전용 색상 남용 금지**: 특정 상태(읽는 중, 완료 등)나 옵션, 메뉴 등을 구분하기 위해 임의의 커스텀 색상(예: 초록색, 파란색 등)을 부여하지 않는다. 상태 구분은 아이콘이나 텍스트 정보로만 표현한다.
- **Edge-to-Edge**: `enableEdgeToEdge()` 적용. `statusBarsPadding()`, `navigationBarsPadding()`, `imePadding()` 으로 인셋 처리
- **접근성**: 인터랙티브 요소에 `contentDescription` 제공. 순수 장식 이미지는 `null`. 카드 내 텍스트 그룹은 `semantics(mergeDescendants = true)`

---

## 6. Preview 규칙

- **필수**: 모든 `@Composable` UI 컴포넌트는 최소 1개 `@Preview` 함수를 가진다. 해당 파일 하단에 `private`으로 작성.
- **다중 상태**: 컴포넌트가 여러 상태에 대응하면, **가능한 모든 상태의 Preview 를 생성**한다.
    - 예: enabled/disabled, 데이터 있음/없음, 짧은 텍스트/긴 텍스트, 각 선택 상태
- **멀티프리뷰 어노테이션**: Light/Dark + Font Scale 1.5x 를 한 번에 확인하는 `ManiculePreview` 어노테이션 정의 및 사용.
  ```kotlin
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.FUNCTION)
  @Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
  @Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
  @Preview(name = "Font 1.5x", fontScale = 1.5f, showBackground = true)
  annotation class ManiculePreview
  ```
- **Preview 내 테마**: Preview 함수 본문에서 반드시 `ManiculePreviewTheme { ... }` 으로 래핑해 앱 루트와 동일한 `background`/`onBackground` Surface 환경을 제공.
- **PreviewParameterProvider**: 다양한 상태를 하나의 Preview 함수로 렌더링할 때 활용.

---

## 7. 공통 컴포넌트 규칙

### 공통 컴포넌트 목록

전체 목록이다. 각 컴포넌트의 **구현 여부는 코드베이스에서 직접 확인**한다(문서에 구현 상태를 중복 기록하지 않는다).
파일 위치는 [structure.md](../structure.md) §4.1·§4.2, 착수 순서는 [order.md](../order.md) 참조.

| 위치                  | 컴포넌트                                               | 용도            |
|---------------------|----------------------------------------------------|---------------|
| `core:designsystem` | `ManiculeBottomSheet`                              | 공통 바텀시트      |
|                     | `ManiculeButton` / `ManiculeIconButton`            | 버튼 (기본, Outlined, Text, Icon) |
|                     | `ManiculeCard`                                     | 공통 카드 (기본, Dashed) |
|                     | `ManiculeDialog`                                   | 확인/취소 다이얼로그   |
|                     | `ManiculeEmptyState`                               | 빈 상태 안내       |
|                     | `ManiculeLoading`                                  | 로딩 인디케이터 (크기는 호출부 `modifier` 지정) |
|                     | `ManiculeSegmentedButton`                          | 세그먼트 버튼      |
|                     | `ManiculeTextField`                                | 텍스트 입력        |
|                     | `ManiculeTopAppBar`                                | 상단 앱바         |
|                     | `ManiculeSearchEntry`                              | 검색 화면 진입      |
|                     | `ManiculeSearchBar`                                    | 공통 검색 바       |
|                     | `ManiculeSectionHeader`                                | 섹션 헤더         |
|                     | `ManiculeSnackbarHost`                                 | 스낵바 호스트 및 Undo 지원 |
|                     | `ManiculeTabRow`                                       | 공통 탭 행        |
|                     | `ManiculeStatTile`                                     | 통계 수치 표시 타일  |
| `core:ui`           | `BookCover`                                          | 책 표지           |
|                     | `BookListItem`                                     | 컴팩트 책 리스트 아이템 |
|                     | `BookProgressBar`                                  | 독서 진행률 바      |
|                     | `ReadingCalendarGrid` / `Cell` / `Legend`          | 독서 달력 및 범례    |

### 프로토타입 변형 ↔ 컴포넌트 커버리지

[prototype.html](prototype.html) 의 **29개 변형**이 모두 아래 컴포넌트로 분해되는지 확인한 표.
분해되지 않는 칸이 생기면 컴포넌트 목록이 미완이라는 뜻이다. 화면 구현 착수 시 해당 행을 먼저 확인한다.

**(f)** = feature 모듈 소유. 표기 없는 것은 `core:designsystem` / `core:ui` / M3 표준.

| 변형 | 화면 | 구성 컴포넌트 |
|---|---|---|
| **1a** 계속 사용자 | 홈 | `ManiculeSearchEntry` · `ManiculeIconButton`(스캔) · `ManiculeCard`(요약, onClick) · `ReadingCalendarGrid`(최근 7일) · `ManiculeStatTile`×2 · `ManiculeSectionHeader`(더보기) · `BookCarouselItem`(f) |
| **1b** 첫 사용자 · 빈 상태 | 홈 | `ManiculeSearchEntry` · `ManiculeDashedCard`(빈 요약) · `OnboardingGuide`(f) · `ManiculeButton`×2 |
| **1c** 읽는 중 없음 | 홈 | 1a 요약부 + `ManiculeEmptyState`(inline, `actions` 1~2개) |
| **2a** 검색어 없음 | 검색 | `ManiculeSearchBar`(requestInitialFocus) · `ManiculeEmptyState` — 앱바 없음 |
| **2b** 최근 검색어 리스트 | 검색 | `ManiculeSearchBar` · `ManiculeSectionHeader` · M3 `ListItem`(leading=History, trailing=Delete)×n · `HorizontalDivider` |
| **2c** 입력 중 로컬 필터 | 검색 | `ManiculeSearchBar` · M3 `ListItem`(leading=Search, 입력값 부분 강조는 호출부 `AnnotatedString`)×n |
| **3a** 결과 컴팩트 리스트 | 검색 결과 | `ManiculeSearchBar` · "검색 결과" 캡션(`labelMedium`·`onSurfaceVariant`, 배지 아님) · `BookListItem`×n · `ManiculeLoading`(작은 크기, 페이징) |
| **3b** 결과 없음 | 검색 결과 | `ManiculeSearchBar` · `ManiculeEmptyState`(`actions`=스캔) |
| **4a** 카메라 스캔 | 스캔 | `BarcodeScannerOverlay`(f, `colorScheme.scrim` + 알파) — 앱바 없음, 카메라 위 떠 있는 뒤로가기 |
| **4b** 인식 실패 | 스캔 | `ManiculeTopAppBar` · `ManiculeEmptyState`(`actions`=검색) |
| **4c** 권한 거부 | 스캔 | `ManiculeTopAppBar` · `ManiculeEmptyState`(`actions`=카메라 사용·검색, Filled+Outlined **2개**) |
| **5a** 책 정보 탭 | 책 정보 | `ManiculeTopAppBar` + `ManiculeTabRow`(앱바 밖) · `BookCover`(중형) · 정보 행 Text 나열 · `BookDetailExpandableText`(f)×2 |
| **5b** 내 기록 · 있음 | 책 정보 | `ManiculeTabRow` · `ManiculeSegmentedButton`(상태 3) · `BookDetailRatingBar`(f) · `ManiculeTextField`(`maxLines`) · `BookProgressBar` · `ManiculeSectionHeader`(추가) · M3 `ListItem`(기록 행)×n |
| **5c** 내 기록 · 없음 | 책 정보 | 5b + `ManiculeDashedCard`(리뷰 유도, `BookDetailRatingBar` 0점) + `ManiculeEmptyState`(기록 없음) |
| **5d** 기록 추가 시트 | 책 정보 | `ManiculeBottomSheet` · `ManiculeSegmentedButton`×2(날짜/시간) · `ManiculeTextField`(`keyboardType = Number`)×2 · `ManiculeButton` · M3 `DatePickerDialog`/`TimePickerDialog`('직접 선택' 시) |
| **5e** 삭제 스낵바 | 책 정보 | `ManiculeSnackbarHost` + `showUndoSnackbar` |
| **5f** 다 읽음 확인 다이얼로그 | 책 정보 | `ManiculeDialog`(`icon` = Celebration) |
| **6a** 읽고 싶음 탭 | 서재 | `ManiculeTopAppBar`(actions=정렬 `ManiculeIconButton`) + `ManiculeTabRow`(앱바 밖) · **정렬 상태 캡션**(`labelMedium`, 탭 아래·목록 위) · `LibraryBookCard`(f)×n |
| **6b** 읽는 중 탭 | 서재 | 6a의 `LibraryBookCard`(f)에 진도율 책갈피 포함 |
| **6c** 다 읽음 탭 | 서재 | 6a의 `LibraryBookCard`(f)에 다 읽은 날짜 포함 |
| **6d** 빈 상태 | 서재 | `ManiculeTabRow` · `ManiculeEmptyState`(`actions`=검색·스캔) |
| **6e** 정렬 바텀시트 | 서재 | `ManiculeBottomSheet` · M3 `ListItem`(selected)×3 · `ManiculeSegmentedButton`(방향) · `ManiculeButton`(적용) |
| **6f** 롱프레스 메뉴 | 서재 | `ManiculeBottomSheet` · M3 `ListItem`(삭제) · M3 `ListItem`×2(상태 변경) — 전 항목 `onSurface`, 강조색 없음 |
| **7a** 4주 탭 | 통계 | `ManiculeTopAppBar` · `ManiculeSegmentedButton`(기간 4) · `ManiculeCard` · `ReadingCalendarGrid` · `ReadingCalendarLegend` · `ManiculeStatTile`×3 · `ReadingChart`(f) |
| **7b** 1년 탭 | 통계 | 7a (가로 스크롤 + 좌우 축 고정) |
| **7c** 오늘 탭 | 통계 | 7a 요약부 + `BookListItem`(소형, trailing=쪽수)×n |
| **7d** 기간 설정 시트 | 통계 | `ManiculeBottomSheet` · M3 `ListItem`×2(시작/종료일) · M3 `DatePickerDialog` · `ManiculeButton` |
| **7e** 날짜 탭 시트 | 통계 | `ManiculeBottomSheet` · `BookListItem`(소형)×n |
| **8a** 설정 | 설정 | `ManiculeTopAppBar` · `ManiculeSectionHeader`×3 · `ManiculeSegmentedButton`(테마 3) · M3 `ListItem`(trailing=Switch) · M3 `ListItem`(시간·라이선스·버전) · M3 `TimePickerDialog` |

> 이 매트릭스는 화면 콘텐츠만 다룬다. 범위 밖 2건 — ① 하단 탭 `NavigationBar` 는 `app` 셸(`ManiculeNavHost`) 소관이라 행마다 반복 표기하지 않는다(상단 앱바는 화면마다 구성이 달라 표기한다). ② 오픈소스 라이선스 화면([plan.md](../plan.md) 3.6)은 서드파티 라이브러리가 그리는 독립 화면으로 `feature:settings` 소관이며 29 변형에 없다.

### 활용 규칙

1. **기존 확인 우선**: 새 컴포넌트 구현 전 위 목록에서 대체 가능한지 확인.
2. **M3 표준이 1순위**: 위 목록에 없더라도 M3 표준 컴포넌트로 해결되면 래퍼를 만들지 않는다. "2개 이상에서 쓰인다"는 사실만으로는 추출 근거가 되지 않는다.
    - 확정 예: 최근 검색어 행(2b·2c)·설정 행(8a)·바텀시트 선택 행(6e·6f·7d)은 **M3 `ListItem` 을 직접 사용**한다. 2c 의 입력값 부분 강조는 호출부에서 `AnnotatedString` 으로 처리
3. **공통 추출 기준**: 2개 이상 feature 모듈에서 사용되며 M3 표준으로 대체되지 않는 UI 요소는 공통 컴포넌트로 추출.
4. **배치 기준**:
    - `core:designsystem` — 도메인 무관 기본 UI 원자 (Button, Dialog 등)
    - `core:ui` — 도메인 모델(`Book`, `ReadingRecord` 등)에 의존하는 공유 위젯
5. **커스텀 스타일링 지양**: 공통 컴포넌트 사용 시 `Modifier` 파라미터로 외형 조정. 내부 스타일 직접 변경 금지.
6. **공유 파일 추가 규약**: `ManiculeIcons.kt` 와 `core:designsystem` 의 `strings.xml` 은 **파일 말미에 추가(append-only)**. 알파벳순 삽입은 병렬 작업 시 충돌을 만든다.

---

## 8. 구현 검수 체크리스트 (완료 보고 전 필수 확인)

AI는 UI 구현을 마치고 사용자에게 보고하기 전, 반드시 아래 카테고리별 항목들을 스스로 점검해야 한다.

### 8.1. 기획 및 아키텍처 준수
- [ ] **프로토타입 확인**: `prototype.html`의 해당 화면 섹션/변형을 누락 없이 반영했는가?
- [ ] **가이드라인 우선**: 프로토타입 디자인과 가이드라인(명암비, 토큰 등) 충돌 시 가이드라인을 우선 적용했는가?
- [ ] **도메인 분리 원칙**: `core:designsystem`(도메인 무관)과 `core:ui`(도메인 종속) 컴포넌트 배치 기준을 엄수했는가?
- [ ] **재사용 우선**: 커스텀 UI 구현 전, 기존 공통 컴포넌트나 M3 표준 컴포넌트로 대체할 수 없는지 먼저 확인했는가?

### 8.2. UI/UX 및 접근성 (A11y)
- [ ] **터치 타겟**: 클릭 가능한 모든 요소가 최소 `48x48dp` 크기를 보장하는가?
- [ ] **상태(State) 대응**: 핵심 콘텐츠의 정상 / 로딩 / 오류 / 빈 상태와 오류 UI·재시도 동작이 구현되었는가? 보조 콘텐츠 실패 시 핵심 흐름을 유지하고, 성공한 빈 결과와 오류를 상태 모델에서 구분하며, 하나 이상의 복구 경로가 제공되는가?
- [ ] **접근성 제공**: 의미 있는 인터랙티브 요소에 `contentDescription`이 제공되었는가? (순수 장식만 `null`)
- [ ] **Edge-to-Edge**: 화면 상하단 인셋(`statusBarsPadding`, `navigationBarsPadding`, `imePadding`)이 겹치지 않게 처리되었는가?

### 8.3. Compose 기술 및 성능 규칙
- [ ] **하드코딩 완벽 통제 (색상/치수)**:
    - **색상**: `MaterialTheme.colorScheme` 및 확장 색상 토큰 사용 (ARGB 리터럴 금지)
    - **치수**: 간격/크기/테두리 등 모든 dp 값은 `Dimension.kt`에 정의된 `MaterialTheme.spacing`, `.size`, `.border` 확장 변수를 사용 (`16.dp` 같은 리터럴 직접 사용 금지)
- [ ] **상태 관리 (State)**: State Hoisting 원칙을 지키고, ViewModel 상태 수집 시 반드시 `collectAsStateWithLifecycle()`을 사용했는가?
- [ ] **문자열 리소스**: 하드코딩된 한글/영문 텍스트 없이 모두 `stringResource`로 추출되었는가?
- [ ] **리스트 최적화**: `LazyColumn/Row` 사용 시 항목 성능을 위해 `key`와 `contentType`을 명시했는가?
- [ ] **네트워크 이미지**: Coil `AsyncImage` 사용 시 네트워크 지연/실패에 대비한 `placeholder`와 `error` 처리가 구현되었는가?
- [ ] **Preview 완결성**: Light/Dark 모드 및 다중 상태 Preview로 작성되었고, 최상위가 `ManiculePreviewTheme`으로 래핑되었는가?
