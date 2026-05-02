package com.leeseungyun1020.manicule.core.common.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * source 가 비어있으면 [defaultValue] 를 한 번 발행한다.
 */
fun <T> Flow<T>.defaultIfEmpty(defaultValue: T): Flow<T> =
    flow {
        var emitted = false
        collect {
            emitted = true
            emit(it)
        }
        if (!emitted) emit(defaultValue)
    }
