package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leeseungyun1020.manicule.core.database.entity.RecentQueryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentQueryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(query: RecentQueryEntity)

    @Query("SELECT * FROM recent_queries ORDER BY lastUsedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 10): Flow<List<RecentQueryEntity>>

    @Query("DELETE FROM recent_queries")
    suspend fun clear()
}
