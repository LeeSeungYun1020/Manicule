# bf604dc — fix: 예약 시 알림 채널 생성

**Files**: `ReminderNotificationChannel.kt`, `WorkManagerReminderScheduler.kt`, `ReminderNotificationPublisher.kt`

## 결정

- 리마인더 알림 채널을 예약 시 생성하고 발행 시에도 존재를 보장한다.

## 이유

- 예약 직후 Android 시스템 알림 설정에서 채널을 확인하고 설정할 수 있어야 한다.
- 발행 경로에서도 채널을 생성해 예약 외 실행이나 복구 상황에서 알림이 누락되지 않게 한다.
