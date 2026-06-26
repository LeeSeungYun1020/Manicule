package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun addQuery(query: String)

    fun observeRecentQueries(limit: Int = 10): Flow<List<SearchQuery>>

    suspend fun clearHistory()
}
