package com.leeseungyun1020.manicule.core.common.result

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResultTest {

    @Test
    fun `asResult emits Loading then Success`() = runTest {
        flowOf(1, 2).asResult().test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            assertThat(awaitItem()).isEqualTo(Result.Success(1))
            assertThat(awaitItem()).isEqualTo(Result.Success(2))
            awaitComplete()
        }
    }

    @Test
    fun `asResult emits Loading then Error on exception`() = runTest {
        val expected = IllegalStateException("boom")
        flow<Int> { throw expected }.asResult().test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Error::class.java)
            assertThat((item as Result.Error).exception).isEqualTo(expected)
            awaitComplete()
        }
    }
}
