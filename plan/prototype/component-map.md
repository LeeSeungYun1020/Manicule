# 화면별 컴포넌트 지도

UI 가이드에서 필요한 화면 변형을 찾은 뒤 해당 행만 읽는다. 이 표는 화면 콘텐츠의 설계이며 구현 여부는 코드에서 확인한다. `(f)`는 feature 소유, 나머지는 공용 컴포넌트 또는 M3 표준이다.

## 공용 컴포넌트

- `core:designsystem`: `ManiculeBottomSheet`, `ManiculeButton`, `ManiculeIconButton`, `ManiculeCard`, `ManiculeDashedCard`, `ManiculeDialog`, `ManiculeEmptyState`, `ManiculeErrorState`, `ManiculeNetworkErrorState`, `ManiculeLoading`, `ManiculeSegmentedButton`, `ManiculeTextField`, `ManiculeTopAppBar`, `ManiculeSearchEntry`, `ManiculeSearchBar`, `ManiculeSectionHeader`, `ManiculeSnackbarHost`, `ManiculeTabRow`, `ManiculeStatTile`.
- `core:ui`: `BookCover`, `BookListItem`, `BookProgressBar`, `ReadingCalendarGrid`와 셀·범례.

## 프로토타입 변형

[prototype.html](prototype.html)의 화면 변형별 구성을 다룬다. 하단 탭 `NavigationBar`는 `app` 셸 소유라 행마다 쓰지 않는다. 오픈소스 라이선스 화면은 `feature:settings`의 자체 화면(`LicensesScreen`)으로 구현하며 프로토타입 변형에 없다.

| 변형 | 화면 | 구성 컴포넌트 |
|---|---|---|
| 1a | 홈 · 계속 사용자 | `HomeSearchTopBar`(f, 로고·검색·스캔) · `ManiculeCard`(요약, 클릭) · `ReadingCalendarGrid`(최근 7일) · `ManiculeStatTile`×2 · `ManiculeSectionHeader`(더보기) · `BookCarouselItem`(f) |
| 1b | 홈 · 첫 사용자 | `HomeSearchTopBar`(f) · `ManiculeDashedCard`(빈 요약) · `OnboardingGuide`(f) · `ManiculeButton`×2 |
| 1c | 홈 · 읽는 중 없음 | 1a 요약부 · `ManiculeEmptyState`(inline, 액션 1~2개) |
| 2a | 검색어 없음 | `ManiculeSearchBar`(초기 포커스) · `ManiculeEmptyState`, 앱바 없음 |
| 2b | 최근 검색어 | `ManiculeSearchBar` · `ManiculeSectionHeader` · M3 `ListItem`(History, Delete)×n · `HorizontalDivider` |
| 2c | 입력 중 로컬 필터 | `ManiculeSearchBar` · M3 `ListItem`(Search, 입력값 강조는 호출부 `AnnotatedString`)×n |
| 3a | 검색 결과 | `ManiculeSearchBar` · “검색 결과” 캡션(`labelMedium`, `onSurfaceVariant`, 배지 아님) · `BookListItem`×n · `ManiculeLoading`(페이징) |
| 3b | 결과 없음 | `ManiculeSearchBar` · `ManiculeEmptyState`(스캔 액션) |
| 4a | 카메라 스캔 | `BarcodeScannerOverlay`(f, `colorScheme.scrim` + 알파), 앱바 없음·카메라 위 뒤로가기 |
| 4b | 인식 실패 | `ManiculeTopAppBar` · `ManiculeEmptyState`(검색 액션) |
| 4c | 권한 거부 | `ManiculeTopAppBar` · `ManiculeEmptyState`(카메라 사용·검색, Filled + Outlined 액션) |
| 5a | 책 정보 탭 | `ManiculeTopAppBar` + `ManiculeTabRow`(앱바 밖) · `BookCover`(중형) · 정보 행 · `BookDetailExpandableText`(f)×2 |
| 5b | 내 기록 있음 | `ManiculeTabRow` · `ManiculeSegmentedButton`(상태 3) · `BookDetailRatingBar`(f) · `ManiculeTextField`(`maxLines`) · `BookProgressBar` · `ManiculeSectionHeader` · M3 `ListItem`(기록)×n |
| 5c | 내 기록 없음 | 5b · `ManiculeDashedCard`(리뷰 유도, 평점 0) · `ManiculeEmptyState`(기록 없음) |
| 5d | 기록 추가 시트 | `ManiculeBottomSheet` · `ManiculeSegmentedButton`×2(날짜/시간) · `ManiculeTextField`(`keyboardType = Number`)×2 · `ManiculeButton` · M3 `DatePickerDialog`/`TimePickerDialog`(직접 선택) |
| 5e | 삭제 스낵바 | `ManiculeSnackbarHost` + `showUndoSnackbar` |
| 5f | 다 읽음 확인 | `ManiculeDialog`(Celebration 아이콘) |
| 6a | 읽고 싶음 탭 | `ManiculeTopAppBar`(정렬 아이콘) + `ManiculeTabRow`(앱바 밖) · 정렬 상태 캡션(`labelMedium`, 탭 아래·목록 위) · `LibraryBookCard`(f)×n |
| 6b | 읽는 중 탭 | 6a의 `LibraryBookCard`(f)에 진도율 책갈피 |
| 6c | 다 읽음 탭 | 6a의 `LibraryBookCard`(f)에 완료 날짜 |
| 6d | 서재 빈 상태 | `ManiculeTabRow` · `ManiculeEmptyState`(검색·스캔 액션) |
| 6e | 정렬 시트 | `ManiculeBottomSheet` · M3 `ListItem`(선택)×3 · `ManiculeSegmentedButton`(방향) · `ManiculeButton`(적용) |
| 6f | 롱프레스 메뉴 | `ManiculeBottomSheet` · M3 `ListItem`(삭제·상태 변경×2), 모두 `onSurface` |
| 7a | 통계 4주 | `ManiculeTopAppBar` · `ManiculeSegmentedButton`(기간 4) · `ManiculeCard` · `ReadingCalendarGrid` · `ReadingCalendarLegend` · `ManiculeStatTile`×3 · `ReadingChart`(f) |
| 7b | 통계 1년 | 7a, 가로 스크롤·좌우 축 고정 |
| 7c | 통계 오늘 | 7a 요약부 · `BookListItem`(소형, trailing=쪽수)×n |
| 7d | 기간 설정 시트 | `ManiculeBottomSheet` · M3 `ListItem`×2(시작/종료일) · M3 `DatePickerDialog` · `ManiculeButton` |
| 7e | 날짜 탭 시트 | `ManiculeBottomSheet` · `BookListItem`(소형)×n |
| 8a | 설정 | `ManiculeTopAppBar` · `ManiculeSectionHeader`×3 · `ManiculeSegmentedButton`(테마 3) · M3 `ListItem`(리마인더, trailing=Switch) · M3 `ListItem`(시간·라이선스·버전) · M3 `TimePickerDialog` |

## 사용 원칙

- 새 UI 컴포넌트를 만들기 전에 공용 목록과 M3 표준을 확인한다.
- 둘 이상의 feature에서 쓰이고 M3로 대체할 수 없을 때 공용 추출을 검토한다.
- 도메인 무관 UI는 `core:designsystem`, 도서·독서 모델에 의존하는 UI는 `core:ui`가 소유한다.
- 공용 컴포넌트의 외형은 공개 `Modifier`와 API로 조정한다.
- `ManiculeIcons.kt`와 `core:designsystem`의 `strings.xml`은 병렬 충돌을 줄이기 위해 파일 말미에 추가한다.
