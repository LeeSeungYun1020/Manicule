package com.leeseungyun1020.manicule.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "recent_queries")
data class RecentQueryEntity(
    @PrimaryKey val query: String,
    val lastUsedAt: Instant,
)
