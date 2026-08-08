# 13daace — refactor: 검색 진입과 입력 컴포넌트 분리

**Files**: `ManiculeSearchEntry.kt`, `ManiculeSearchBar.kt`, `TopNavigationComponentsTest.kt`

## 결정
- 검색 화면 진입은 `ManiculeSearchEntry`, 검색어 입력과 제출은 `ManiculeSearchBar`가 담당한다.
- 입력 컴포넌트는 초기 진입 포커스 요청만 명시적으로 지원한다.

## 이유
- `readOnly`, `autoFocus`, `onReadOnlyClick` 조합은 무의미한 상태를 허용하고 진입과 입력 역할을 하나의 API에 결합했다.
- 검색바를 유지하면서 교체되는 최근 검색어와 결과 본문은 feature 화면이 계속 소유해야 한다.
