package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.common.time.Clock
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

internal fun Clock.observeToday(): Flow<LocalDate> =
    flow {
        while (currentCoroutineContext().isActive) {
            val today = today()
            emit(today)

            val nextDayStart = today.plus(DatePeriod(days = 1)).atStartOfDayIn(timeZone())
            val delayMillis = (nextDayStart - now()).inWholeMilliseconds.coerceAtLeast(1L)
            delay(delayMillis)
        }
    }.distinctUntilChanged()
