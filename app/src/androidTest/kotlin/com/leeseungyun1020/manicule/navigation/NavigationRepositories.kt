package com.leeseungyun1020.manicule.navigation

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
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

        override suspend fun syncBook(isbn: String): Result<BookSyncStatus> {
            syncedIsbn = isbn
            return Result.success(BookSyncStatus.COMPLETE)
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> {
            searchCalls++
            return Pager(
                PagingConfig(pageSize = 40, initialLoadSize = 40),
                pagingSourceFactory = (if (query == "Missing") emptyList() else books).asPagingSourceFactory(),
            ).flow
        }
    }

class NavigationLibrary
    @Inject
    constructor() : LibraryRepository {
        override fun observeAll(): Flow<List<BookEntry>> = flowOf(emptyList())

        override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> = flowOf(emptyList())

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = emptyList()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = flowOf(null)

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = SaveBookEntryResult.Saved

        override suspend fun removeBookEntry(isbn: String) = Unit
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
