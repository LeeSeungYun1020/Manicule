package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSourceImpl
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSourceImpl
import com.leeseungyun1020.manicule.core.database.dao.BookDao
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkBookDto
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BookRepositoryImplTest {

    private lateinit var bookRepository: BookRepositoryImpl
    private lateinit var fakeBookDao: FakeBookDao
    private lateinit var fakeNlkApi: FakeNlkApi

    @Before
    fun setup() {
        fakeBookDao = FakeBookDao()
        fakeNlkApi = FakeNlkApi()
        bookRepository =
            BookRepositoryImpl(
                BookLocalDataSourceImpl(fakeBookDao),
                BookRemoteDataSourceImpl(fakeNlkApi),
            )
    }

    @Test
    fun getBook_returns_book_if_exists_in_dao() =
        runTest {
            val entity =
                BookEntity(
                    isbn = "123",
                    title = "Test Book",
                    author = "Test Author",
                    publisher = "Test Publisher",
                    publishedDate = null,
                    coverUrl = null,
                    totalPages = null,
                    price = null,
                    category = null,
                    tableOfContentsUrl = null,
                    introductionUrl = null,
                    summaryUrl = null,
                )
            fakeBookDao.upsert(entity)

            val result = bookRepository.getBook("123")
            assertThat(result.isSuccess).isTrue()
            val book = result.getOrNull()
            assertThat(book).isNotNull()
            assertThat(book?.isbn).isEqualTo("123")
            assertThat(book?.title).isEqualTo("Test Book")
        }

    @Test
    fun getBook_returns_failure_if_not_exists() =
        runTest {
            val result = bookRepository.getBook("999")
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(NoSuchElementException::class.java)
        }

    @Test
    fun fetchAndCacheBook_fetches_from_api_and_saves_to_dao() =
        runTest {
            val dto =
                NlkBookDto(
                    isbn = "456",
                    title = "API Book",
                    author = "API Author",
                    publisher = "API Publisher",
                    publishPredate = "20240101",
                    titleUrl = "",
                    page = "200",
                    prePrice = "10000",
                    subject = "",
                    bookTbCntUrl = "",
                    bookIntroductionUrl = "",
                    bookSummaryUrl = "",
                )
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    totalCount = "1",
                    pageNo = "1",
                    docs = listOf(dto),
                )

            val result = bookRepository.fetchAndCacheBook("456")

            // Assert returned book
            assertThat(result.isSuccess).isTrue()
            val book = result.getOrNull()
            assertThat(book).isNotNull()
            assertThat(book?.isbn).isEqualTo("456")
            assertThat(book?.title).isEqualTo("API Book")

            // Assert cached in dao
            val cached = fakeBookDao.getByIsbn("456")
            assertThat(cached).isNotNull()
            assertThat(cached?.isbn).isEqualTo("456")
            assertThat(cached?.title).isEqualTo("API Book")
        }

    @Test
    fun fetchAndCacheBook_returns_failure_if_api_returns_empty() =
        runTest {
            fakeNlkApi.mockResponse = NlkSearchResponseDto(docs = emptyList())
            val result = bookRepository.fetchAndCacheBook("789")
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(NoSuchElementException::class.java)

            val cached = fakeBookDao.getByIsbn("789")
            assertThat(cached).isNull()
        }
}

class FakeBookDao : BookDao {
    private val books = mutableMapOf<String, BookEntity>()

    override suspend fun getByIsbn(isbn: String): BookEntity? = books[isbn]

    override fun observeByIsbn(isbn: String): Flow<BookEntity?> = flowOf(books[isbn])

    override suspend fun upsert(book: BookEntity) {
        books[book.isbn] = book
    }
}

class FakeNlkApi : NlkApi {
    var mockResponse = NlkSearchResponseDto()

    override suspend fun searchBooks(
        resultStyle: String,
        pageNo: Int,
        pageSize: Int,
        title: String?,
        author: String?,
        isbn: String?,
    ): NlkSearchResponseDto = mockResponse
}
