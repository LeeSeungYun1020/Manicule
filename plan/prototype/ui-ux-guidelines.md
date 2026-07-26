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

> 상세: `android docs compose-state`, `android docs compose-side-effects`
> 또는 [Compose API Guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)
> 참조

---

## 3. 모바일 UI/UX 일반 규칙

- **터치 타겟**: 최소 **48 × 48 dp**. 부족 시 `Modifier.minimumInteractiveComponentSize()` 활용. 인접 타겟 간 최소 8dp 간격
- **간격 체계**: 8dp 그리드 (4 / 8 / 16 / 24 / 32 dp). 화면 좌우 패딩 16dp
- **타이포그래피 계층**: M3 15종 텍스트 스타일 (Display / Headline / Title / Body / Label × L/M/S) 준수. 프로젝트는 Noto Sans KR + 한국어 line-height 튜닝 적용 (
  `Type.kt`)
- **색상 명암비**: 일반 텍스트 4.5:1 이상, 대형 텍스트(24sp+) 및 아이콘 3.0:1 이상 (WCAG AA)
- **상태별 UI**: 모든 화면에서 로딩 / 에러 / 빈 상태를 처리. 빈 상태는 `ManiculeEmptyState` 활용
- **내비게이션**: 하단 탭 3\~5개. 시스템 뒤로가기 지원. Type-safe Navigation 사용
- **햅틱**: 길게 누르기 등 주요 제스처에 `LocalHapticFeedback` 적용

---

## 4. Material 3 가이드라인

- **테마 래퍼**: Screen / Preview 최상위에서 반드시 `ManiculeTheme`으로 래핑. `ManiculeTheme`이 `MaterialTheme` + `LocalGrassColors` 등 프로젝트 CompositionLocal을 제공
- **토큰 접근**: 컴포넌트 내부에서 `MaterialTheme.colorScheme` / `.typography` / `.shapes` 를 통해 접근. 잔디 색상은 `LocalGrassColors.current`. 하드코딩 색상(`Color(0xFF...)`) 금지
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
- **Preview 내 테마**: Preview 함수 본문에서 반드시 `ManiculeTheme { ... }` 으로 래핑.
- **PreviewParameterProvider**: 다양한 상태를 하나의 Preview 함수로 렌더링할 때 활용.

---

## 7. 공통 컴포넌트 규칙

### 현재 공통 컴포넌트

| 위치                  | 컴포넌트                                               | 용도            |
|---------------------|----------------------------------------------------|---------------|
| `core:designsystem` | `ManiculeButton` / `OutlinedButton` / `TextButton` | 버튼 3종         |
|                     | `ManiculeDialog`                                   | 확인/취소 다이얼로그   |
|                     | `ManiculeEmptyState`                               | 빈 상태 안내       |
|                     | `ManiculeLoading`                                  | 전체화면 로딩       |
|                     | `ManiculeTextField`                                | 텍스트 입력        |
|                     | `ManiculeTopAppBar`                                | 상단 앱바         |
|                     | `ManiculeIcons`                                    | 아이콘 중앙 관리     |
| `core:ui`           | `BookCover`                                        | 표지 이미지 (Coil) |
|                     | `BookListItem`                                     | 컴팩트 책 리스트 아이템 |
|                     | `BookProgressBar`                                  | 독서 진행률 바      |
|                     | `ContributionCell` / `Grid`                        | 독서 달력 잔디      |

### 활용 규칙

1. **기존 확인 우선**: 새 컴포넌트 구현 전 위 목록에서 대체 가능한지 확인.
2. **공통 추출 기준**: 2개 이상 feature 모듈에서 사용되는 UI 요소는 공통 컴포넌트로 추출.
3. **배치 기준**:
    - `core:designsystem` — 도메인 무관 기본 UI 원자 (Button, Dialog 등)
    - `core:ui` — 도메인 모델(`Book`, `ReadingRecord` 등)에 의존하는 공유 위젯
4. **커스텀 스타일링 지양**: 공통 컴포넌트 사용 시 `Modifier` 파라미터로 외형 조정. 내부 스타일 직접 변경 금지.

---

## 8. 체크리스트

UI 구현 완료 시 아래 항목을 확인한다.

- [ ] [prototype.html](prototype.html) 해당 섹션/변형 확인 완료
- [ ] 프로토타입과 가이드라인 충돌 여부 점검, 충돌 시 사용자 재검토 완료
- [ ] 기존 공통 컴포넌트 활용 가능 여부 확인
- [ ] 터치 타겟 48 × 48 dp 이상
- [ ] `ManiculeTheme` 래핑 확인 (Screen / Preview 최상위)
- [ ] `MaterialTheme` 토큰으로 접근 (하드코딩 색상 없음, 잔디는 `LocalGrassColors`)
- [ ] 로딩 / 에러 / 빈 상태 처리
- [ ] `LazyColumn` items에 `key`, `contentType` 지정
- [ ] `contentDescription` 제공 (인터랙티브/의미 있는 이미지)
- [ ] Light/Dark 모드 Preview 확인
- [ ] 다중 상태 Preview 작성 완료
- [ ] 사용자 노출 문자열 `stringResource` 사용
- [ ] Edge-to-Edge 인셋 처리 (`statusBarsPadding`, `navigationBarsPadding`, `imePadding`)
