package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.datasource.SearchHistoryLocalDataSource
import com.leeseungyun1020.manicule.core.database.entity.RecentQueryEntity
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchHistoryRepositoryImpl
    @Inject
    constructor(
        private val searchHistoryLocalDataSource: SearchHistoryLocalDataSource,
        private val clock: Clock,
    ) : SearchHistoryRepository {

        override suspend fun saveQuery(query: String) {
            val entity =
                RecentQueryEntity(
                    query = query,
                    lastUsedAt = clock.now(),
                )
            searchHistoryLocalDataSource.save(entity)
        }

        override fun observeRecentQueries(limit: Int): Flow<List<SearchQuery>> =
            searchHistoryLocalDataSource.observeRecent(limit).map { list ->
                list.map { SearchQuery(query = it.query, executedAt = it.lastUsedAt) }
            }

        override suspend fun deleteQuery(query: String) {
            searchHistoryLocalDataSource.delete(query)
        }

        override suspend fun clearHistory() {
            searchHistoryLocalDataSource.clear()
        }
    }
