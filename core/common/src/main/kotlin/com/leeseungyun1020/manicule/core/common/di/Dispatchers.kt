package com.leeseungyun1020.manicule.core.common.di

import javax.inject.Qualifier

/**
 * Qualifier for [kotlinx.coroutines.CoroutineDispatcher] instances injected by
 * [DispatchersModule]. Use this to declare the dispatcher a class actually
 * needs so it remains substitutable in tests.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val maniculeDispatcher: ManiculeDispatcher)

enum class ManiculeDispatcher {
    /** IO 작업 (네트워크, DB, 파일). */
    IO,

    /** CPU 바운드 계산 작업 (정렬, 잔디 계산 등). */
    Default,
}
