package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.datasource.SearchHistoryLocalDataSourceImpl
import com.leeseungyun1020.manicule.core.database.dao.RecentQueryDao
import com.leeseungyun1020.manicule.core.database.entity.RecentQueryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class SearchHistoryRepositoryImplTest {

    private lateinit var repository: SearchHistoryRepositoryImpl
    private lateinit var fakeDao: FakeRecentQueryDao
    private lateinit var fakeClock: FakeClock

    @Before
    fun setup() {
        fakeDao = FakeRecentQueryDao()
        fakeClock = FakeClock()
        repository = SearchHistoryRepositoryImpl(SearchHistoryLocalDataSourceImpl(fakeDao), fakeClock)
    }

    @Test
    fun saveQuery_inserts_or_updates_dao() =
        runTest {
            repository.addQuery("test")
            assertThat(fakeDao.queries).hasSize(1)
            assertThat(fakeDao.queries[0].query).isEqualTo("test")

            // Update
            repository.addQuery("test")
            assertThat(fakeDao.queries).hasSize(1)
        }

    @Test
    fun observeRecentQueries_returns_mapped_flow() =
        runTest {
            repository.addQuery("apple")
            repository.addQuery("banana")

            val queries = repository.observeRecentQueries(10).first()
            assertThat(queries).hasSize(2)
            assertThat(queries.map { it.query }).containsExactly("banana", "apple")
        }

    @Test
    fun clearHistory_deletes_all() =
        runTest {
            repository.addQuery("test")
            repository.clearHistory()
            assertThat(fakeDao.queries).isEmpty()
        }
}

class FakeClock(
    var currentTime: Instant = Instant.parse("2026-06-28T00:00:00Z"),
) : Clock {
    override fun now(): Instant = currentTime

    override fun timeZone(): kotlinx.datetime.TimeZone = kotlinx.datetime.TimeZone.UTC

    fun advanceBy(duration: kotlin.time.Duration) {
        currentTime += duration
    }
}

class FakeRecentQueryDao : RecentQueryDao {
    val queries = mutableListOf<RecentQueryEntity>()

    override suspend fun upsert(query: RecentQueryEntity) {
        val idx = queries.indexOfFirst { it.query == query.query }
        if (idx >= 0) {
            queries[idx] = query
        } else {
            queries.add(query)
        }
    }

    override fun observeRecent(limit: Int): Flow<List<RecentQueryEntity>> = flowOf(queries.sortedByDescending { it.lastUsedAt }.take(limit))

    override suspend fun clear() {
        queries.clear()
    }
}
