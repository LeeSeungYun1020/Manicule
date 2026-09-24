# UI/UX 개발 가이드라인

## 읽기 경로와 판단 기준

1. 작업 화면의 변형 ID를 [프로토타입](prototype.html)에서 찾고 해당 블록만 확인한다. `rg -n 'id="<변형ID>"' plan/prototype/prototype.html`로 위치를 찾는다.
2. 아래 공통 규칙과 해당 화면의 확정 편차를 읽는다. 컴포넌트 구성은 [화면별 컴포넌트 지도](component-map.md)의 해당 변형 행만 확인하고, 구현 여부와 API는 코드에서 확인한다.
3. 완료 전 마지막 체크리스트를 점검한다. 기능·상태·예외는 프로젝트 기획, UI 구성은 이 가이드, 화면 예시는 프로토타입을 따른다. 미확정 충돌만 사용자와 재검토한다.

## 확정된 프로토타입 편차

아래 결정은 재검토가 끝났으므로 프로토타입보다 우선한다.

| 화면 | 프로토타입 | 확정 사항 |
|---|---|---|
| 전체 | 앱바 제목 좌측 정렬 | `CenterAlignedTopAppBar`로 중앙 정렬하고 스크롤 시 접는다. 홈은 검색 앱바를 사용한다. |
| 1a 홈 | 우측 상단 아바타 | 기획에 없는 요소이므로 구현하지 않는다. |
| 1a·1b 홈 | 앱바 아래 검색창·스캔 버튼 | `HomeSearchTopBar`에 로고·검색 진입·스캔 버튼을 배치한다. 검색을 탭하면 검색 화면으로 이동한다. |
| 6a 서재 | 앱바 안 정렬 상태 텍스트 | 앱바에는 정렬 아이콘만 두고, 상태 텍스트는 탭 아래·목록 위에 둔다. |
| 1a·7a·8a | 제목이 스크롤 본문 안 | 제목을 앱바로 옮긴다. |
| 5c 내 기록 | 기본 상태가 ‘읽고 싶음’ | 미등록·`UNSET`은 미선택이다. 상태를 탭할 때 서재에 추가한다. `WANT` 선택 예시는 등록된 책에만 적용한다. |

## Compose와 상태

- 화면 상태는 ViewModel 또는 최저 공통 부모에서 관리하고 `UiState`(`data class`/`sealed interface`)로 묶는다. 재사용 UI는 가능한 한 stateless로 작성한다. `StateFlow`는 `collectAsStateWithLifecycle()`로 수집한다.
- `@Composable` Unit 함수는 PascalCase를 사용하고 첫 선택 파라미터로 `modifier: Modifier = Modifier`를 받아 루트에 적용한다. 터치 영역은 `clickable`과 `padding`의 순서, 배경 범위는 `background`와 `padding`의 순서로 결정한다.
- 컬렉션 포함 모델의 안정성은 `@Immutable`/`@Stable`로 관리한다. 계산 상태는 `derivedStateOf`, 회전 후 유지할 UI 상태는 `rememberSaveable`을 사용한다. 네트워크·DB 호출은 컴포지션 본문에서 하지 않는다. 장수명 효과는 `LaunchedEffect`·`DisposableEffect`·`rememberUpdatedState`의 수명주기를 확인한다.
- 정상·로딩·오류·빈 상태를 구분한다. 초기 데이터 실패는 `ManiculeErrorState`와 재시도, 기존 데이터 갱신 실패는 기존 화면 유지와 `ManiculeSnackbarHost`, 페이지·보조 섹션 실패는 해당 위치의 인라인 오류로 알린다. 보조 콘텐츠가 실패해도 핵심 흐름을 유지하고, 성공한 빈 결과와 오류는 상태 모델에서 구분하며 재시도·재진입·재구독 중 복구 경로를 둔다.
- 필요한 UI 테스트는 Compose 테스트 API로 동작과 표시를 검증한다. 세부 API는 `android docs compose-state`, `android docs compose-side-effects`와 [Compose API 가이드](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)를 따른다.

## 레이아웃·테마·접근성

- 클릭 영역은 최소 48×48dp, 인접 타겟 간격은 최소 8dp다. 부족한 영역은 `Modifier.minimumInteractiveComponentSize()`를 사용한다. 독서 달력 셀에는 기획의 밀집형 예외를 적용한다.
- 간격은 8dp 그리드(4/8/16/24/32dp), 화면 좌우 패딩은 16dp다. 실제 UI 치수는 `MaterialTheme.spacing`·`size`·`border` 토큰을 사용하고 `16.dp` 같은 리터럴을 직접 넣지 않는다. 텍스트는 M3 계층과 프로젝트의 Noto Sans KR·한국어 line-height 설정을 따른다.
- 일반 텍스트 명암비는 4.5:1 이상, 24sp 이상 텍스트와 아이콘은 3:1 이상이다. 이미지 위 글씨에는 딤 또는 그라디언트 오버레이를 둔다. 상태·옵션 구분에 임의의 강조색을 추가하지 않고 아이콘·텍스트로 표현한다.
- 앱 루트는 `ManiculeTheme`으로 감싼다. 컴포넌트는 `MaterialTheme.colorScheme`·`typography`·`shapes`·치수 토큰을 사용하며 ARGB 리터럴을 넣지 않는다. 달력 레벨은 `MaterialTheme.maniculeColors.calendarLevels`를 사용한다.
- `ManiculeExtendedColors` 필드는 M3 색상 역할에 맞는 값이 없고, 라이트·다크 값이 다르며, 둘 이상의 컴포넌트가 공유할 때만 추가한다. 표지·카메라 딤은 `scrim`, 차트 막대는 `primary`, 격자선은 `outlineVariant`, 축 레이블은 `onSurfaceVariant`를 우선 사용한다.
- `ManiculeTopAppBar`는 `enterAlwaysScrollBehavior()`와 `Scaffold`의 `nestedScroll`을 연결한다. 책 정보·서재의 탭은 앱바 밖 상단에 남긴다. 검색 화면과 카메라 스캔은 앱바가 없고, 스캔 실패·권한 거부는 앱바를 사용한다. 하단 탭은 3~5개로 구성하며 시스템 뒤로가기와 타입 안전 내비게이션을 지원한다.
- M3 표준 컴포넌트를 우선 사용하고 깊이는 그림자 대신 tonal elevation으로 표현한다. 상태 전환에는 `AnimatedVisibility`·`animateContentSize`·`Crossfade`를 사용하며 화면 전환의 공유 요소 효과는 필요 시 검토한다. 창 크기는 `WindowSizeClass`의 Compact·Medium·Expanded로 대응한다.
- 사용자 노출 문자열은 `stringResource`로 관리한다. RTL에는 `start`/`end` 패딩과 방향성 아이콘의 `AutoMirrored`를 사용한다. 인터랙티브 요소에는 접근성 설명을 제공하고 순수 장식에는 `null`을 사용한다. 카드의 텍스트 그룹은 의미를 합친다.
- 앱은 `enableEdgeToEdge()`를 사용한다. 상태바·내비게이션바·IME 인셋이 콘텐츠와 겹치지 않게 처리한다. `LazyColumn`/`LazyRow` 항목은 안정적인 `key`와 `contentType`을 지정한다. 네트워크 표지는 `BookCover` 또는 Coil `AsyncImage`를 사용하고 `crossfade(true)`·placeholder·error를 제공한다.

## Preview

- UI 컴포넌트마다 파일 하단에 private Preview 함수를 최소 하나 두고, 가능한 모든 상태와 Light/Dark·글꼴 확대를 확인한다. 기존 `ManiculePreview` 어노테이션과 `ManiculePreviewTheme`을 사용한다. 여러 상태는 `PreviewParameterProvider`로 묶을 수 있다.
- `ManiculePreviewTheme`은 앱 루트와 같은 Surface·콘텐츠 색상 환경을 제공한다. 테마를 개별 화면에서 중복 적용하지 않는다.

## 완료 점검

UI 구현 후 사용자에게 보고하기 전에 해당 항목을 확인한다.

- [ ] 기획의 기능·예외, 해당 프로토타입 변형, 확정 편차가 반영됐는가?
- [ ] 공용 컴포넌트 또는 M3 표준을 재사용하고 도메인에 맞는 모듈에 배치했는가?
- [ ] 정상·로딩·오류·빈 상태와 복구 동작을 확인했는가?
- [ ] 터치 영역·명암비·접근성·RTL·인셋을 확인했는가?
- [ ] 테마·치수·문자열 토큰, 리스트·이미지 처리, Preview 상태를 확인했는가?
