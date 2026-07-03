package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.RecentQueryDao
import com.leeseungyun1020.manicule.core.database.entity.RecentQueryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomSearchHistoryLocalDataSource
    @Inject
    constructor(
        private val recentQueryDao: RecentQueryDao,
    ) : SearchHistoryLocalDataSource {
        override suspend fun save(query: RecentQueryEntity) {
            recentQueryDao.upsert(query)
        }

        override fun observeRecent(limit: Int): Flow<List<RecentQueryEntity>> = recentQueryDao.observeRecent(limit)

        override suspend fun clear() {
            recentQueryDao.clear()
        }
    }
