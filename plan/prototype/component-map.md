# 화면별 컴포넌트 지도

UI 가이드에서 필요한 화면 변형을 찾은 뒤 해당 행만 읽는다. 각 행은 현재 화면을 구성하는 주요 Composable을 바깥쪽부터 적고, 괄호에는 그 안에서 사용하는 Composable이나 상태별 차이를 적는다. `(f)`는 feature 소유이며, 이름 앞의 `M3`는 Material 3 표준 컴포넌트를 뜻한다.

## 공용 컴포넌트

- `core:designsystem`: `ManiculeBottomSheet`, `ManiculeButton`, `ManiculeOutlinedButton`, `ManiculeTextButton`, `ManiculeIconButton`, `ManiculeCard`, `ManiculeDashedCard`, `ManiculeDialog`, `ManiculeEmptyState`, `ManiculeErrorState`, `ManiculeNetworkErrorState`, `ManiculeLoading`, `ManiculeSegmentedButton`, `ManiculeTextField`, `ManiculeTopAppBar`, `ManiculeSearchEntry`, `ManiculeSearchBar`, `ManiculeSectionHeader`, `ManiculeSnackbarHost`, `ManiculeTabRow`, `ManiculeStatTile`.
- `core:ui`: `BookCover`, `BookListItem`, `BookProgressBar`, `ReadingCalendarGrid`, `ReadingCalendarCell`, `ReadingCalendarLegend`.

## 프로토타입 변형

[prototype.html](prototype.html)의 화면 변형별 구성을 다룬다. 하단 탭 `NavigationBar`는 `app` 셸 소유라 행마다 쓰지 않는다. 오픈소스 라이선스 화면은 `feature:settings`의 자체 화면(`LicensesScreen`)으로 구현하며 프로토타입 변형에 없다.

| 변형 | 화면 | 구성 컴포저블 |
|---|---|---|
| 1a | 홈 · 계속 사용자 | `HomeSearchTopBar`(f) · `ReadingSummary`(f, `ManiculeCard` 안에 `SummaryMetrics`·`HomeWeekStrip`(`ReadingCalendarCell`×7)·`ReadingCalendarLegend`) · `ReadingBooks`(f, `ManiculeSectionHeader`·`ReadingBookCard`×n) |
| 1b | 홈 · 첫 사용자 | `HomeSearchTopBar`(f) · `OnboardingContent`(f, `ManiculeDashedCard` 안에 `SummaryMetrics`·`HomeWeekStrip`, `ManiculeCard` 안에 `OnboardingStep`·`ManiculeButton`·`ManiculeOutlinedButton`) |
| 1c | 홈 · 읽는 중 없음 | `HomeSearchTopBar`(f) · `ReadingSummary`(f) · `NoReadingBooks`(f, `ManiculeEmptyState`와 검색·스캔 또는 서재 이동 액션) |
| 2a | 검색어 없음 | `ManiculeSearchBar`(초기 포커스) · `IdleSearchContent`(f, `EmptyRecentQuery` 안에 `ManiculeEmptyState`); 앱바 없음 |
| 2b | 최근 검색어 | `ManiculeSearchBar` · `RecentQueryList`(f, `ManiculeSectionHeader`·`QueryListItem`×n; 항목은 M3 `ListItem`·`HorizontalDivider`) |
| 2c | 입력 중 로컬 필터 | `ManiculeSearchBar` · `FilteredQueryList`(f, `QueryListItem`×n; 일치 문자열 강조) |
| 3a | 검색 결과 | `ManiculeSearchBar` · `SearchResultList`(f, 결과 캡션·`SearchResultItem`(`BookListItem` 사용)×n·추가 로딩/오류의 `SearchAppendState`) |
| 3b | 결과 없음 | `ManiculeSearchBar` · `SearchResultList`(f, 빈 결과에서 `ManiculeEmptyState`·`ManiculeButton`으로 스캔 이동) |
| 4a | 카메라 스캔 | `CameraPreview`(f) · `BarcodeScannerOverlay`(f, 카메라 위 뒤로가기·가이드); 앱바 없음 |
| 4b | 인식 실패 | `ScannerMessageScreen`(f, `ManiculeTopAppBar`·`ManiculeEmptyState`·`ManiculeButton`으로 검색 이동) |
| 4c | 권한 거부 | `ScannerMessageScreen`(f, `ManiculeTopAppBar`·`ManiculeEmptyState`·`ManiculeButton`·`ManiculeOutlinedButton`) |
| 5a | 책 정보 탭 | `BookDetailScreenTopBar`(f, `ManiculeTopAppBar`·`ManiculeTabRow`) · `BookInfoTabContent`(f, `BookCover`·출판 정보·`BookDetailExpandableText`×2) |
| 5b | 내 기록 있음 | `BookDetailScreenTopBar`(f) · `MyRecordTabContent`(f, `StatusSelector`·`BookDetailReviewCard`(`BookDetailRatingBar`·`ManiculeTextField`)·`BookProgressBar`·`ManiculeSectionHeader`·`ReadingRecordDateHeader`·`ReadingRecordSessionItem`×n) |
| 5c | 내 기록 없음 | `BookDetailScreenTopBar`(f) · `MyRecordTabContent`(f, 평점·메모가 비면 `BookDetailReviewCard`의 `ManiculeDashedCard`, 기록이 비면 `EmptyReadingRecord`의 `ManiculeEmptyState`·기록 추가 버튼) |
| 5d | 기록 추가 시트 | `AddRecordBottomSheet`(f, `ManiculeBottomSheet` 안에 날짜·시간용 `ManiculeSegmentedButton`×2, 페이지용 `ManiculeTextField`×2, `ManiculeButton`); 직접 선택 시 M3 `DatePickerDialog` 또는 `AlertDialog`(`TimePicker`) |
| 5e | 삭제 스낵바 | `ManiculeSnackbarHost`(Undo 액션 표시) |
| 5f | 다 읽음 확인 | `FinishCheckDialog`(f, 축하 아이콘을 넣은 `ManiculeDialog`) |
| 6a | 읽고 싶음 탭 | `LibraryTopBar`(f, `ManiculeTopAppBar`·`ManiculeTabRow`·정렬 상태를 표시하는 `LibraryActionRow`) · `LibraryGrid`(f, `LibraryBookCard`×n) |
| 6b | 읽는 중 탭 | `LibraryTopBar`(f, `ManiculeTopAppBar`·`ManiculeTabRow`·`LibraryActionRow`) · `LibraryGrid`(f, `LibraryBookCard`×n; 카드에 `BookmarkRibbon`·`BookCoverStatusOverlay`로 진도율 표시) |
| 6c | 다 읽음 탭 | `LibraryTopBar`(f, `ManiculeTopAppBar`·`ManiculeTabRow`·`LibraryActionRow`) · `LibraryGrid`(f, `LibraryBookCard`×n; 카드에 `BookCoverStatusOverlay`로 완료 날짜 표시) |
| 6d | 서재 빈 상태 | `LibraryTopBar`(f, `ManiculeTopAppBar`·`ManiculeTabRow`) · `EmptyLibrary`(f, `ManiculeEmptyState`·`ManiculeButton`·`ManiculeOutlinedButton`) |
| 6e | 정렬 시트 | `SortBottomSheet`(f, `ManiculeBottomSheet` 안에 M3 `ListItem`×3·`ManiculeSegmentedButton`·`ManiculeOutlinedButton`·`ManiculeButton`) |
| 6f | 롱프레스 메뉴 | `LibraryActionBottomSheet`(f, `ManiculeBottomSheet` 안에 `BookCover`·M3 `ListItem`×3) |
| 7a | 통계 4주 | `ManiculeTopAppBar` · `ManiculeSegmentedButton`(기간) · `StatsCalendarCard`(f, `ReadingCalendarGrid`·`ReadingCalendarLegend`) · `StatsSummary`(f, `ManiculeStatTile`×3) · `ReadingChartCard`(f, `ReadingChartUnitSelector`·`ReadingChart`) |
| 7b | 통계 1년 | `ManiculeTopAppBar` · `ManiculeSegmentedButton`(기간) · `StatsCalendarCard`(f, `ReadingCalendarGrid`·`ReadingCalendarLegend`) · `StatsSummary`(f, `ManiculeStatTile`×3) · `ReadingChartCard`(f, `ReadingChartUnitSelector`·`ReadingChart`; 가운데 그래프 가로 스크롤·양쪽 축 고정) |
| 7c | 통계 오늘 | `ManiculeTopAppBar` · `ManiculeSegmentedButton`(기간) · `StatsCalendarCard`(f, `TodayCalendarStrip`·`ReadingCalendarLegend`; 오늘 셀 선택 시 목록으로 스크롤) · `StatsSummary`(f) · `ReadingDayBookItem`(f, `BookListItem` 사용)×n |
| 7d | 기간 설정 시트 | `CustomPeriodBottomSheet`(f, `ManiculeBottomSheet` 안에 `CustomPeriodContent`(M3 `ListItem`×2·`ManiculeButton`)); 날짜 직접 선택 시 M3 `DatePickerDialog` |
| 7e | 날짜 탭 시트 | `ReadingDayBottomSheet`(f, `ManiculeBottomSheet` 안에 `ReadingDayBookItem`×n; 항목은 `BookListItem` 사용) |
| 8a | 설정 | `ManiculeTopAppBar` · `ThemeSection`(f, `ManiculeSectionHeader`·`ManiculeSegmentedButton`) · `ReminderSection`(f, `ManiculeSectionHeader`·`ReminderToggle`·시간 변경 시 `ReminderTimePicker`) · `SupportSection`(f, `ManiculeSectionHeader`·M3 `ListItem`×2) |

## 사용 원칙

- 새 UI 컴포넌트를 만들기 전에 공용 목록과 M3 표준을 확인한다.
- 둘 이상의 feature에서 쓰이고 M3로 대체할 수 없을 때 공용 추출을 검토한다.
- 도메인 무관 UI는 `core:designsystem`, 도서·독서 모델에 의존하는 UI는 `core:ui`가 소유한다.
- 공용 컴포넌트의 외형은 공개 `Modifier`와 API로 조정한다.
- `ManiculeIcons.kt`와 `core:designsystem`의 `strings.xml`은 병렬 충돌을 줄이기 위해 파일 말미에 추가한다.
