package com.leeseungyun1020.manicule.core.domain.library

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetLibraryBooksUseCaseTest {
    private val repository = FakeLibraryRepository()
    private val useCase = GetLibraryBooksUseCase(repository)

    @Test
    fun status_usesDefaultSort() =
        runTest {
            useCase(ReadingStatus.FINISHED).test {
                assertThat(awaitItem()).isEmpty()
                assertThat(repository.observedStatus).isEqualTo(ReadingStatus.FINISHED)
                assertThat(repository.observedSort).isEqualTo(LibrarySort.Default)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun status_forwardsSelectedSort() =
        runTest {
            val sort =
                LibrarySort(
                    criterion = LibrarySort.Criterion.ADDED_AT,
                    direction = LibrarySort.Direction.ASCENDING,
                )

            useCase(ReadingStatus.WANT, sort).test {
                assertThat(awaitItem()).isEmpty()
                assertThat(repository.observedStatus).isEqualTo(ReadingStatus.WANT)
                assertThat(repository.observedSort).isEqualTo(sort)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun noStatus_observesAllBooks() =
        runTest {
            useCase().test {
                assertThat(awaitItem()).isEmpty()
                assertThat(repository.observedAll).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class FakeLibraryRepository : LibraryRepository {
    val books = MutableStateFlow<List<BookEntry>>(emptyList())
    var observedStatus: ReadingStatus? = null
    var observedSort: LibrarySort? = null
    var observedAll = false

    override fun observeAll(): Flow<List<BookEntry>> {
        observedAll = true
        return books
    }

    override fun observeByStatus(
        status: ReadingStatus,
        sort: LibrarySort,
    ): Flow<List<BookEntry>> {
        observedStatus = status
        observedSort = sort
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
