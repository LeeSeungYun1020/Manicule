package com.leeseungyun1020.manicule.core.domain.library

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Test

class RestoreBookEntryUseCaseTest {
    private val repository = RestoreRepository()
    private val useCase = RestoreBookEntryUseCase(repository)

    @Test
    fun restore_passesWholeSnapshotToRepository() =
        runTest {
            val entry = snapshot()
            assertThat(useCase(entry)).isTrue()
            assertThat(repository.saved).isEqualTo(entry)
        }

    @Test
    fun invalidSave_doesNotReportRestoreSuccess() =
        runTest {
            repository.result = SaveBookEntryResult.InvalidRating(6)
            assertThat(useCase(snapshot())).isFalse()
        }

    private fun snapshot() =
        BookEntry(
            book = Book(
                isbn = "isbn",
                title = "Title",
                author = "Author",
                publisher = "Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = 100,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            ),
            status = ReadingStatus.FINISHED,
            rating = 5,
            memo = "Memo",
            addedAt = Instant.fromEpochMilliseconds(1),
            updatedAt = Instant.fromEpochMilliseconds(2),
            finishedAt = LocalDate(2026, 9, 1),
            currentPage = 80,
        )

    private class RestoreRepository : LibraryRepository {
        var saved: BookEntry? = null
        var result: SaveBookEntryResult = SaveBookEntryResult.Saved

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult {
            saved = entry
            return result
        }

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult = error("Unused")

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

        override fun observeByStatus(
            status: ReadingStatus,
            sort: LibrarySort,
        ): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = emptyList()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = emptyFlow()

        override suspend fun removeBookEntry(isbn: String) = Unit
    }
}
