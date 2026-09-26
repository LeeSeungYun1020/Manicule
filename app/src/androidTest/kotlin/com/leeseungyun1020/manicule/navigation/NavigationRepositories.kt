package com.leeseungyun1020.manicule.navigation

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationBooks
    @Inject
    constructor() : BookRepository {
        @Volatile var syncedIsbn: String? = null

        @Volatile var searchCalls = 0
        private val books = List(40) { index ->
            Book(
                isbn = "isbn-$index",
                title = "Navigation book $index",
                author = "Author",
                publisher = "Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = 300,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            )
        }

        override fun observeBook(isbn: String): Flow<Book?> = flowOf(books.find { it.isbn == isbn })

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> {
            syncedIsbn = isbn
            val book = books.find { it.isbn == isbn } ?: return Result.failure(NoSuchElementException(isbn))
            return Result.success(BookSyncResult(book = book, status = BookSyncStatus.COMPLETE))
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> {
            searchCalls++
            return Pager(
                PagingConfig(pageSize = 40, initialLoadSize = 40),
                pagingSourceFactory = (if (query == "Missing") emptyList() else books).asPagingSourceFactory(),
            ).flow
        }
    }

@Singleton
class NavigationLibrary
    @Inject
    constructor() : LibraryRepository {
        val entries = MutableStateFlow<List<BookEntry>>(emptyList())

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult = ReadingStatusChangeResult.Changed

        override suspend fun updateRating(
            isbn: String,
            rating: Int,
            updatedAt: Instant,
        ): RatingChangeResult = RatingChangeResult.Changed

        override suspend fun updateMemo(
            isbn: String,
            memo: String?,
            updatedAt: Instant,
        ): com.leeseungyun1020.manicule.core.model.MemoChangeResult = com.leeseungyun1020.manicule.core.model.MemoChangeResult.Changed

        override fun observeAll(): Flow<List<BookEntry>> = entries

        override fun observeByStatus(
            status: ReadingStatus,
            sort: LibrarySort,
        ): Flow<List<BookEntry>> = entries.map { list -> list.filter { it.status == status } }

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = entries.value.filter { it.status == status }.take(limit).map { it.book }

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = entries.map { list -> list.find { it.book.isbn == isbn } }

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult {
            entries.value = entries.value.filterNot { it.book.isbn == entry.book.isbn } + entry
            return SaveBookEntryResult.Saved
        }

        override suspend fun removeBookEntry(isbn: String) {
            entries.value = entries.value.filterNot { it.book.isbn == isbn }
        }

        override suspend fun restoreDeletedEntryIfAbsent(entry: BookEntry): Boolean = error("Not used")

        override suspend fun restoreReadingStatusIfUnchanged(
            original: BookEntry,
            changedStatus: ReadingStatus,
            changedAt: kotlinx.datetime.Instant,
        ): Boolean = error("Not used")
    }

class NavigationHistory
    @Inject
    constructor() : SearchHistoryRepository {
        private val queries = MutableStateFlow<List<SearchQuery>>(emptyList())

        override suspend fun saveQuery(query: String) {
            queries.value = listOf(SearchQuery(query, Instant.fromEpochMilliseconds(0)))
        }

        override fun observeRecentQueries(limit: Int): Flow<List<SearchQuery>> = queries

        override suspend fun removeQuery(query: String) {
            queries.value = queries.value.filterNot { it.query == query }
        }

        override suspend fun clearHistory() {
            queries.value = emptyList()
        }
    }
