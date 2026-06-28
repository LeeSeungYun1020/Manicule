package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSourceImpl
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSourceImpl
import com.leeseungyun1020.manicule.core.database.dao.BookDao
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkBookDto
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    fun scenario1_local_exists_remote_success() =
        runTest {
            val localEntity =
                BookEntity(
                    isbn = "123",
                    title = "Local Book",
                    author = "Author",
                    publisher = "Publisher",
                    publishedDate = null,
                    coverUrl = null,
                    totalPages = null,
                    price = null,
                    category = null,
                    tableOfContentsUrl = null,
                    introductionUrl = null,
                    summaryUrl = null,
                )
            fakeBookDao.upsert(localEntity)

            val remoteDto =
                NlkBookDto(
                    isbn = "123",
                    title = "Remote Book",
                    author = "Author",
                    publisher = "Publisher",
                    publishPredate = "",
                    titleUrl = "",
                    page = "",
                    prePrice = "",
                    subject = "",
                    bookTbCntUrl = "",
                    bookIntroductionUrl = "",
                    bookSummaryUrl = "",
                )
            fakeNlkApi.mockResponse = NlkSearchResponseDto(totalCount = "1", pageNo = "1", docs = listOf(remoteDto))

            val results = mutableListOf<Book?>()
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    bookRepository.observeBook("123").toList(results)
                }

            // 처음에 로컬 데이터가 방출됨
            assertThat(results.last()?.title).isEqualTo("Local Book")

            // 원격 동기화 수행
            val syncResult = bookRepository.syncBook("123")
            assertThat(syncResult.isSuccess).isTrue()

            // 로컬 데이터 업데이트 후 새로운 원격 데이터가 방출됨
            assertThat(results.last()?.title).isEqualTo("Remote Book")

            job.cancel()
        }

    @Test
    fun scenario2_local_exists_remote_fail() =
        runTest {
            val localEntity =
                BookEntity(
                    isbn = "123",
                    title = "Local Book",
                    author = "Author",
                    publisher = "Publisher",
                    publishedDate = null,
                    coverUrl = null,
                    totalPages = null,
                    price = null,
                    category = null,
                    tableOfContentsUrl = null,
                    introductionUrl = null,
                    summaryUrl = null,
                )
            fakeBookDao.upsert(localEntity)

            fakeNlkApi.mockResponse = NlkSearchResponseDto(docs = emptyList()) // 원격 데이터 없음(또는 실패)

            val results = mutableListOf<Book?>()
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    bookRepository.observeBook("123").toList(results)
                }

            // 처음에 로컬 데이터 방출됨
            assertThat(results.last()?.title).isEqualTo("Local Book")

            // 원격 동기화 실패
            val syncResult = bookRepository.syncBook("123")
            assertThat(syncResult.isFailure).isTrue()

            // 여전히 로컬 데이터가 유지됨
            assertThat(results.last()?.title).isEqualTo("Local Book")

            job.cancel()
        }

    @Test
    fun scenario3_local_empty_remote_success() =
        runTest {
            // 로컬 데이터 없음
            val remoteDto =
                NlkBookDto(
                    isbn = "123",
                    title = "Remote Book",
                    author = "Author",
                    publisher = "Publisher",
                    publishPredate = "",
                    titleUrl = "",
                    page = "",
                    prePrice = "",
                    subject = "",
                    bookTbCntUrl = "",
                    bookIntroductionUrl = "",
                    bookSummaryUrl = "",
                )
            fakeNlkApi.mockResponse = NlkSearchResponseDto(totalCount = "1", pageNo = "1", docs = listOf(remoteDto))

            val results = mutableListOf<Book?>()
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    bookRepository.observeBook("123").toList(results)
                }

            // 처음에 로컬 데이터가 없으므로 null 방출 (UI는 로딩 등 대기 상태)
            assertThat(results.last()).isNull()

            // 원격 동기화 수행
            val syncResult = bookRepository.syncBook("123")
            assertThat(syncResult.isSuccess).isTrue()

            // 원격 데이터가 로컬에 반영된 후 방출됨
            assertThat(results.last()?.title).isEqualTo("Remote Book")

            job.cancel()
        }

    @Test
    fun scenario4_local_empty_remote_fail() =
        runTest {
            // 로컬 데이터 없음
            fakeNlkApi.mockResponse = NlkSearchResponseDto(docs = emptyList()) // 원격 데이터 없음

            val results = mutableListOf<Book?>()
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    bookRepository.observeBook("123").toList(results)
                }

            // 처음에 로컬 데이터가 없으므로 null 방출 (대기 상태)
            assertThat(results.last()).isNull()

            // 원격 동기화 실패
            val syncResult = bookRepository.syncBook("123")
            assertThat(syncResult.isFailure).isTrue()

            // 여전히 null 유지 (이후 ViewModel에서 null & syncResult.isFailure 확인 후 에러 UI 표시)
            assertThat(results.last()).isNull()

            job.cancel()
        }
}

class FakeBookDao : BookDao {
    private val booksFlow = MutableStateFlow<Map<String, BookEntity>>(emptyMap())

    override suspend fun getByIsbn(isbn: String): BookEntity? = booksFlow.value[isbn]

    override fun observeByIsbn(isbn: String): Flow<BookEntity?> = booksFlow.map { it[isbn] }

    override suspend fun upsert(book: BookEntity) {
        booksFlow.update { it + (book.isbn to book) }
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
