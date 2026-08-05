# f2caed9 — feat: 피드백·상태 공용 컴포넌트 추가

**Files**: `ManiculeSnackbarHost.kt`, `ManiculeEmptyState.kt`, `ManiculeLoading.kt`

## 결정
- Undo 스낵바는 현재 요청을 닫고 최신 요청을 표시하며 결과를 호출부에 반환한다.
- 빈 상태의 동작은 `RowScope` 슬롯으로, 로딩 크기는 호출부 `Modifier`로 소유한다.

## 이유
- 삭제 Undo가 연속 발생해도 오래된 안내가 새 상태를 가리지 않아야 한다.
- 빈 상태마다 필요한 Filled·Outlined 조합과 전체 화면·Paging 로딩 크기가 달라 공통 컴포넌트가 화면 정책을 강제하지 않도록 한다.
