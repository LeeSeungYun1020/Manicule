# 4ceaae3 — feat: 월요일 기준 독서 달력 선택 계약 구현

**Files**: `DateExt.kt`, `ReadingCalendarGrid.kt`, `ReadingCalendarGridTest.kt`

## 결정

- 독서 달력의 주 시작 요일은 월요일로 고정한다.
- 날짜 선택 가능 여부는 `ReadingCalendarGrid` 호출부가 결정하고, 선택 불가능한 날짜에는 `clickable`을 적용하지 않는다.

## 이유

- 현재 기획의 월~일 배열과 날짜 범위 계산을 같은 기준으로 맞춘다. 월요일/일요일 사용자 설정은 #46으로 분리해 현재 PR의 범위를 유지한다.
- V7은 기록이 있는 날짜만 상세 화면을 열 수 있으므로, 기록이 없는 날짜에 클릭 피드백을 보여 주고 결과가 없는 동작을 피한다.
- 공용 컴포넌트는 `isDateSelectable`과 `onDateSelected`만 제공해 선택 정책과 후속 동작을 feature가 소유하게 한다.
