package com.leeseungyun1020.manicule.core.domain.settings

import kotlinx.datetime.LocalTime

interface ReminderScheduler {
    /**
     * 다음 로컬 [time]부터 매일 리마인더를 예약한다.
     *
     * 시스템 최적화의 영향을 받는 best-effort 작업이며 정확 알람이 아니다.
     */
    suspend fun schedule(time: LocalTime)

    /**
     * 현재 실행 중인 리마인더 뒤에 다음 로컬 [time] 작업을 연결한다.
     */
    suspend fun scheduleNext(time: LocalTime)

    suspend fun cancel()
}
