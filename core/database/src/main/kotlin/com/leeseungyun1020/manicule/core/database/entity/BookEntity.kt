package com.leeseungyun1020.manicule.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val publishedDate: LocalDate?,
    val coverUrl: String?,
    val totalPages: Int?,
    val price: Int?,
    val category: String?,
    val tableOfContentsUrl: String?,
    val introductionUrl: String?,
    val summaryUrl: String?,
)
