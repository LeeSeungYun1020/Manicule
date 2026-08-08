package com.leeseungyun1020.manicule.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Entity(
    tableName = "reading_records",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["isbn"],
            childColumns = ["isbn"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["isbn"]),
        Index(value = ["date"]),
    ],
)
data class ReadingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isbn: String,
    val date: LocalDate,
    val time: LocalTime,
    val startPage: Int,
    val endPage: Int,
)
