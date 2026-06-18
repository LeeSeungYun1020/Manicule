package com.leeseungyun1020.manicule.core.database.dao.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity

data class BookEntryWithCurrentPage(
    @Embedded
    val entry: BookEntryEntity,
    @ColumnInfo(name = "currentPage")
    val currentPage: Int?,
)
