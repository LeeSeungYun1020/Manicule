package com.leeseungyun1020.manicule.core.data.mapper

import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.BookEntry

fun BookEntryWithCurrentPage.asExternalModel(bookEntity: BookEntity): BookEntry {
    require(this.entry.isbn == bookEntity.isbn) {
        "BookEntryWithCurrentPage (isbn=${entry.isbn}) and BookEntity (isbn=${bookEntity.isbn}) do not match."
    }
    return BookEntry(
        book = bookEntity.asExternalModel(),
        status = entry.status,
        rating = entry.rating,
        memo = entry.memo,
        addedAt = entry.addedAt,
        updatedAt = entry.updatedAt,
        finishedAt = entry.finishedAt,
        currentPage = currentPage,
    )
}

fun BookEntry.asEntity() =
    BookEntryEntity(
        isbn = book.isbn,
        status = status,
        rating = rating,
        memo = memo,
        addedAt = addedAt,
        updatedAt = updatedAt,
        finishedAt = finishedAt,
    )
