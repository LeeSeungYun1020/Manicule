package com.leeseungyun1020.manicule.core.database.dao.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity

data class BookEntryWithCurrentPage(
    @Embedded
    val entry: BookEntryEntity,
    @Relation(
        parentColumn = "isbn",
        entityColumn = "isbn",
    )
    val book: BookEntity,
    @ColumnInfo(name = "currentPage")
    val currentPage: Int?,
)
