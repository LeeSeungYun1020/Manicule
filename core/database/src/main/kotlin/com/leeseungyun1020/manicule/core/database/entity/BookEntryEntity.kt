package com.leeseungyun1020.manicule.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "book_entries",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["isbn"],
            childColumns = ["isbn"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class BookEntryEntity(
    @PrimaryKey val isbn: String,
    val status: ReadingStatus,
    val rating: Int?,
    val memo: String?,
    val addedAt: Instant,
    val updatedAt: Instant,
    val finishedAt: LocalDate?,
)
