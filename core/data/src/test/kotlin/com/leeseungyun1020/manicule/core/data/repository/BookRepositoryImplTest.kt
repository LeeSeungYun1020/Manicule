package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.RetrofitBookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.datasource.RoomBookLocalDataSource
import com.leeseungyun1020.manicule.core.database.dao.BookDao
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import com.leeseungyun1020.manicule.core.network.nlk.NlkContentFetcher
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
    private lateinit var fakeContentFetcher: FakeNlkContentFetcher

    @Before
    fun setup() {
        fakeBookDao = FakeBookDao()
        fakeNlkApi = FakeNlkApi()
        fakeContentFetcher = FakeNlkContentFetcher()
        bookRepository =
            BookRepositoryImpl(
                RoomBookLocalDataSource(fakeBookDao),
                RetrofitBookRemoteDataSource(fakeNlkApi, fakeContentFetcher),
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

    @Test
    fun syncBook_blankRequiredFields_failsWithoutChangingCache() =
        runTest {
            val cached =
                BookEntity(
                    isbn = "123",
                    title = "Cached Book",
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
            fakeBookDao.upsert(cached)
            val initialUpsertCallCount = fakeBookDao.upsertCallCount
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    docs =
                        listOf(
                            NlkBookDto(
                                isbn = " ",
                                title = "Book",
                                bookIntroductionUrl = "https://www.nl.go.kr/introduction.txt",
                            ),
                            NlkBookDto(
                                isbn = "123",
                                title = " ",
                                bookTbCntUrl = "https://www.nl.go.kr/contents.txt",
                            ),
                        ),
                )

            val result = bookRepository.syncBook("123")

            assertThat(result.isFailure).isTrue()
            assertThat(fakeBookDao.upsertCallCount).isEqualTo(initialUpsertCallCount)
            assertThat(fakeBookDao.getByIsbn("123")).isEqualTo(cached)
            assertThat(fakeContentFetcher.requestedUrls).isEmpty()
        }

    @Test
    fun syncBook_prefersInlineContent_withoutFetchingUrls() =
        runTest {
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    docs =
                        listOf(
                            NlkBookDto(
                                isbn = "123",
                                title = "Book",
                                bookIntroduction = "Inline introduction",
                                bookIntroductionUrl = "https://www.nl.go.kr/introduction.txt",
                                bookTbCnt = "Inline contents",
                                bookTbCntUrl = "https://www.nl.go.kr/contents.txt",
                            ),
                        ),
                )

            assertThat(bookRepository.syncBook("123").getOrThrow()).isEqualTo(BookSyncStatus.COMPLETE)

            val book = fakeBookDao.getByIsbn("123")
            assertThat(book?.introduction).isEqualTo("Inline introduction")
            assertThat(book?.tableOfContents).isEqualTo("Inline contents")
            assertThat(fakeContentFetcher.requestedUrls).isEmpty()
        }

    @Test
    fun syncBook_fetchesMissingContent_andReportsPartialFailure() =
        runTest {
            val introductionUrl = "https://www.nl.go.kr/introduction.txt"
            val contentsUrl = "https://nl.go.kr/contents.txt"
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    docs =
                        listOf(
                            NlkBookDto(
                                isbn = "123",
                                title = "Book",
                                bookIntroductionUrl = introductionUrl,
                                bookTbCntUrl = contentsUrl,
                            ),
                        ),
                )
            fakeContentFetcher.responses[introductionUrl] = Result.success("Fetched introduction")
            fakeContentFetcher.responses[contentsUrl] = Result.failure(IllegalStateException("unavailable"))

            assertThat(bookRepository.syncBook("123").getOrThrow())
                .isEqualTo(BookSyncStatus.AUXILIARY_CONTENT_FAILED)

            val book = fakeBookDao.getByIsbn("123")
            assertThat(book?.introduction).isEqualTo("Fetched introduction")
            assertThat(book?.tableOfContents).isNull()
            assertThat(fakeContentFetcher.requestedUrls).containsExactly(introductionUrl, contentsUrl)
        }

    @Test
    fun syncBook_preservesCachedContent_whenAuxiliaryRefreshesFail() =
        runTest {
            val initialEntity =
                BookEntity(
                    isbn = "123",
                    title = "Old Book",
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
                    introduction = "Cached introduction",
                    tableOfContents = "Cached contents",
                )
            fakeBookDao.upsert(initialEntity)

            val introductionUrl = "https://www.nl.go.kr/introduction.txt"
            val contentsUrl = "https://nl.go.kr/contents.txt"
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    docs =
                        listOf(
                            NlkBookDto(
                                isbn = "123",
                                title = "New Book",
                                bookIntroductionUrl = introductionUrl,
                                bookTbCntUrl = contentsUrl,
                            ),
                        ),
                )
            fakeContentFetcher.responses[introductionUrl] = Result.failure(IllegalStateException("failed"))
            fakeContentFetcher.responses[contentsUrl] = Result.failure(IllegalStateException("failed"))

            assertThat(bookRepository.syncBook("123").getOrThrow())
                .isEqualTo(BookSyncStatus.AUXILIARY_CONTENT_FAILED)

            val book = fakeBookDao.getByIsbn("123")
            assertThat(book?.title).isEqualTo("New Book")
            assertThat(book?.introduction).isEqualTo("Cached introduction")
            assertThat(book?.tableOfContents).isEqualTo("Cached contents")
        }

    @Test
    fun syncBook_usesSummaryUrl_whenIntroductionUrlIsAbsent() =
        runTest {
            val summaryUrl = "https://www.nl.go.kr/summary.txt"
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    docs =
                        listOf(
                            NlkBookDto(
                                isbn = "123",
                                title = "Book",
                                bookSummaryUrl = summaryUrl,
                            ),
                        ),
                )
            fakeContentFetcher.responses[summaryUrl] = Result.success("Fetched summary as introduction")

            assertThat(bookRepository.syncBook("123").isSuccess).isTrue()

            val book = fakeBookDao.getByIsbn("123")
            assertThat(book?.introduction).isEqualTo("Fetched summary as introduction")
            assertThat(fakeContentFetcher.requestedUrls).containsExactly(summaryUrl)
        }
}

class FakeBookDao : BookDao {
    private val booksFlow = MutableStateFlow<Map<String, BookEntity>>(emptyMap())
    var upsertCallCount = 0
        private set

    override suspend fun getByIsbn(isbn: String): BookEntity? = booksFlow.value[isbn]

    override fun observeByIsbn(isbn: String): Flow<BookEntity?> = booksFlow.map { it[isbn] }

    override suspend fun upsert(book: BookEntity) {
        upsertCallCount++
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

class FakeNlkContentFetcher : NlkContentFetcher {
    val responses = mutableMapOf<String, Result<String?>>()
    val requestedUrls = mutableListOf<String>()

    override suspend fun fetch(url: String): String? {
        requestedUrls += url
        return responses[url]?.getOrThrow()
    }
}
