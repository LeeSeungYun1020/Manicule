# 1dfdd7c — feat: 독서 리마인더 플랫폼 구현

**Files**: `ReminderScheduler.kt`, `WorkManagerReminderScheduler.kt`, `ReminderWorker.kt`

## 결정
- 매일 리마인더는 다음 로컬 선택 시각까지 첫 실행을 지연한 24시간 unique periodic work로 예약한다.
- 알림 권한이나 앱·채널 알림이 차단된 경우 게시를 건너뛰고 작업은 성공 처리한다.

## 이유
- WorkManager 주기 작업은 시스템 최적화 영향을 받으므로 정확 알람이 아닌 best-effort 리마인더로 다룬다.
- 사용자가 알림을 차단한 상태는 일시적 콘텐츠 오류가 아니므로 불필요한 재시도를 만들지 않는다.
