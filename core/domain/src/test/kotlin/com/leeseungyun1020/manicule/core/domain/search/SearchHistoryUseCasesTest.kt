package com.leeseungyun1020.manicule.core.domain.search

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test

class SearchHistoryUseCasesTest {
    private val recentQueries =
        listOf(
            SearchQuery("Kotlin", Instant.fromEpochMilliseconds(200)),
            SearchQuery("Compose", Instant.fromEpochMilliseconds(100)),
        )
    private val repository = FakeSearchHistoryRepository(recentQueries)

    @Test
    fun getRecentQueries_returns_repository_flow_with_limit() =
        runTest {
            val result = GetRecentQueriesUseCase(repository)(limit = 1).first()

            assertThat(repository.observedLimit).isEqualTo(1)
            assertThat(result).containsExactlyElementsIn(recentQueries)
        }

    @Test
    fun saveRecentQuery_saves_non_blank_query() =
        runTest {
            SaveRecentQueryUseCase(repository)("Kotlin")

            assertThat(repository.savedQueries).containsExactly("Kotlin")
        }

    @Test
    fun saveRecentQuery_ignores_blank_query() =
        runTest {
            SaveRecentQueryUseCase(repository)("   ")

            assertThat(repository.savedQueries).isEmpty()
        }

    @Test
    fun deleteRecentQuery_deletes_non_blank_query() =
        runTest {
            DeleteRecentQueryUseCase(repository)("Kotlin")

            assertThat(repository.deletedQueries).containsExactly("Kotlin")
        }

    @Test
    fun deleteRecentQuery_ignores_blank_query() =
        runTest {
            DeleteRecentQueryUseCase(repository)("   ")

            assertThat(repository.deletedQueries).isEmpty()
        }
}

private class FakeSearchHistoryRepository(
    private val recentQueries: List<SearchQuery>,
) : SearchHistoryRepository {
    var observedLimit: Int? = null
    val savedQueries = mutableListOf<String>()
    val deletedQueries = mutableListOf<String>()

    override suspend fun saveQuery(query: String) {
        savedQueries += query
    }

    override fun observeRecentQueries(limit: Int): Flow<List<SearchQuery>> {
        observedLimit = limit
        return flowOf(recentQueries)
    }

    override suspend fun deleteQuery(query: String) {
        deletedQueries += query
    }

    override suspend fun clearHistory() = Unit
}
