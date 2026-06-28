package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.entity.RecentQueryEntity
import kotlinx.coroutines.flow.Flow

interface SearchHistoryLocalDataSource {
    suspend fun upsert(query: RecentQueryEntity)

    fun observeRecent(limit: Int): Flow<List<RecentQueryEntity>>

    suspend fun clear()
}
