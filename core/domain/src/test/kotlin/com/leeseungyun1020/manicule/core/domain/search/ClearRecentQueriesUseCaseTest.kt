package com.leeseungyun1020.manicule.core.domain.search

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClearRecentQueriesUseCaseTest {

    private var clearCalled = false

    private val fakeSearchHistoryRepository =
        object : SearchHistoryRepository {
            override suspend fun saveQuery(query: String) = TODO("Not needed")

            override fun observeRecentQueries(limit: Int): Flow<List<SearchQuery>> = TODO("Not needed")

            override suspend fun removeQuery(query: String) = error("Not needed")

            override suspend fun clearHistory() {
                clearCalled = true
            }
        }

    private val useCase = ClearRecentQueriesUseCase(fakeSearchHistoryRepository)

    @Test
    fun invoke_calls_repository_clearHistory() =
        runTest {
            useCase()
            assertThat(clearCalled).isTrue()
        }
}
