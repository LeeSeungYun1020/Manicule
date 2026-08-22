# d4fa50b — fix: 다크 모드 프리뷰 콘텐츠 색상 적용

**Files**: `ManiculePreview.kt`, `component/`

## 결정
- 공용 UI Preview는 `ManiculePreviewTheme`을 통해 `background`/`onBackground` Surface 환경을 공유한다.

## 이유
- `@Preview(showBackground = true)`는 `LocalContentColor`를 제공하지 않아 다크 모드에서 색상 미지정 콘텐츠가 검게 표시된다.
- 실제 앱은 루트 Surface가 콘텐츠 색상을 제공하므로 런타임 컴포넌트의 색상 계약은 변경하지 않는다.
