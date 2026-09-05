package com.leeseungyun1020.manicule.core.domain.library

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetLibraryBooksUseCaseTest {
    private val repository = FakeLibraryRepository()
    private val useCase = GetLibraryBooksUseCase(repository)

    @Test
    fun status_observesOnlyThatStatus() =
        runTest {
            useCase(ReadingStatus.FINISHED).test {
                assertThat(awaitItem()).isEmpty()
                assertThat(repository.observedStatus).isEqualTo(ReadingStatus.FINISHED)
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class FakeLibraryRepository : LibraryRepository {
    val books = MutableStateFlow<List<BookEntry>>(emptyList())
    var observedStatus: ReadingStatus? = null

    override fun observeAll(): Flow<List<BookEntry>> = books

    override suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: kotlinx.datetime.Instant,
        finishedAt: kotlinx.datetime.LocalDate?,
    ): ReadingStatusChangeResult = error("Not used by this test")

    override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> {
        observedStatus = status
        return books
    }

    override suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book> = emptyList()

    override fun observeBookEntry(isbn: String): Flow<BookEntry?> = MutableStateFlow(null)

    override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = SaveBookEntryResult.Saved

    override suspend fun removeBookEntry(isbn: String) = Unit
}
