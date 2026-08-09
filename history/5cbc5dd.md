# 5cbc5dd — feat: 공유 독서 통계 집계 구현

**Files**: `ReadingRecordStatsDao.kt`, `StatsRepository.kt`, `GetPeriodSummaryUseCase.kt`

## 결정
- 기존 기록 CRUD와 분리된 집계 DAO가 포함 범위의 세션 합계와 고유 ISBN 수를 `Flow`로 제공한다.
- 현재 streak는 오늘 또는 어제까지 이어진 과거 기록만, 기간 최장 streak는 요청 범위의 기록만 사용한다.

## 이유
- 별도 집계 경계는 기존 CRUD 시그니처와 DB 스키마를 유지하면서 홈과 통계가 같은 계약을 재사용하게 한다.
- SQL에서 세션 쪽수와 고유 책 수를 계산하면 재독·겹침 기록을 보존하고 일별 책 수의 중복 합산을 피할 수 있다.
