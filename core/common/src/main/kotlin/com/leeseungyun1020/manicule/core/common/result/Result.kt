package com.leeseungyun1020.manicule.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * 데이터 로딩의 3가지 상태를 표현하는 sealed 인터페이스.
 *
 * UI Layer에서는 이 [Result] 를 그대로 collect 하여 로딩/성공/에러 분기를 단순화한다.
 */
sealed interface Result<out T> {
    data object Loading : Result<Nothing>

    data class Success<T>(
        val data: T,
    ) : Result<T>

    data class Error(
        val exception: Throwable? = null,
    ) : Result<Nothing>
}

/**
 * Flow<T> 를 [Result] 의 흐름으로 변환한다.
 *
 * - 첫 emit 직전 [Result.Loading] 을 발행한다.
 * - 값은 [Result.Success] 로 감싼다.
 * - 예외 발생 시 [Result.Error] 로 변환한다.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
