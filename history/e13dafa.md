# e13dafa — fix: 최근 검색어 조회 실패 복구

**Files**: `SearchViewModel.kt`, `SearchUiState.kt`, `SearchScreen.kt`

## 결정

- 최근 검색어 조회 실패 시 500ms 후 한 번만 Flow를 재구독한다.
- 재시도도 실패하면 `Unavailable`로 구분하고 2a 화면으로 점진적 저하한다.

## 이유

- 최근 검색어는 보조 콘텐츠이므로 검색 입력을 차단하지 않는다.
- 빈 결과와 실패를 상태 모델에서 구분하면서 제한된 자동 복구 경로를 제공한다.
