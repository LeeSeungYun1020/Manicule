package com.leeseungyun1020.manicule.core.data.repository

import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.RetrofitBookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.datasource.RoomBookLocalDataSource
import com.leeseungyun1020.manicule.core.database.dao.BookDao
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import com.leeseungyun1020.manicule.core.network.nlk.NlkContentFetchResult
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
    fun searchBooks_usesTenItemsForInitialAndAppendLoads() =
        runTest {
            fakeNlkApi.responseProvider = { request ->
                if (request.title == null) {
                    NlkSearchResponseDto(
                        totalCount = "0",
                        pageNo = request.pageNo.toString(),
                    )
                } else {
                    val firstIndex = (request.pageNo - 1) * request.pageSize + 1
                    val lastIndex = minOf(firstIndex + request.pageSize - 1, 20)
                    NlkSearchResponseDto(
                        totalCount = "20",
                        pageNo = request.pageNo.toString(),
                        docs =
                            (firstIndex..lastIndex).map { index ->
                                NlkBookDto(
                                    isbn = "isbn-$index",
                                    title = "Book $index",
                                )
                            },
                    )
                }
            }

            val books =
                bookRepository.searchBooks("Compose").asSnapshot {
                    scrollTo(index = 10)
                }

            assertThat(books).hasSize(20)
            assertThat(fakeNlkApi.requests.map { it.pageSize })
                .containsExactly(10, 10, 10)
            assertThat(fakeNlkApi.requests.map { it.pageNo })
                .containsExactly(1, 1, 2)
        }

    @Test
    fun searchBooks_deduplicatesIsbnsAcrossPages() =
        runTest {
            fakeNlkApi.responseProvider = { request ->
                if (request.title == null) {
                    NlkSearchResponseDto(totalCount = "0", pageNo = request.pageNo.toString())
                } else {
                    when (request.pageNo) {
                        1 ->
                            NlkSearchResponseDto(
                                totalCount = "20",
                                pageNo = "1",
                                docs = (1..10).map { NlkBookDto(isbn = "isbn-$it", title = "Book $it") },
                            )
                        2 ->
                            NlkSearchResponseDto(
                                totalCount = "20",
                                pageNo = "2",
                                docs = (10..20).map { NlkBookDto(isbn = "isbn-$it", title = "Book $it") },
                            )
                        else -> NlkSearchResponseDto(totalCount = "20", pageNo = request.pageNo.toString())
                    }
                }
            }

            val books =
                bookRepository.searchBooks("Compose").asSnapshot {
                    scrollTo(index = 10)
                }

            // 1페이지(1..10)와 2페이지(10..20)에서 중복된 isbn-10이 제거되어 총 20권이어야 함
            assertThat(books).hasSize(20)
            assertThat(books.map { it.isbn }).containsNoDuplicates()
            assertThat(books.first().isbn).isEqualTo("isbn-1")
            assertThat(books.last().isbn).isEqualTo("isbn-20")
        }

    @Test
    fun searchBooksByIsbn_requestsNlkApiWithIsbnParameterAndReturnsPagingData() =
        runTest {
            fakeNlkApi.responseProvider = { request ->
                if (request.isbn == "9788954699914") {
                    NlkSearchResponseDto(
                        totalCount = "1",
                        pageNo = request.pageNo.toString(),
                        docs =
                            listOf(
                                NlkBookDto(
                                    isbn = "9788954699914",
                                    title = "Kotlin in Action",
                                    author = "Author",
                                ),
                            ),
                    )
                } else {
                    NlkSearchResponseDto(totalCount = "0", pageNo = request.pageNo.toString())
                }
            }

            val books = bookRepository.searchBooksByIsbn("9788954699914").asSnapshot()

            assertThat(books).hasSize(1)
            assertThat(books.first().isbn).isEqualTo("9788954699914")
            assertThat(fakeNlkApi.requests).hasSize(1)
            val request = fakeNlkApi.requests.first()
            assertThat(request.isbn).isEqualTo("9788954699914")
            assertThat(request.title).isNull()
            assertThat(request.author).isNull()
            assertThat(request.pageNo).isEqualTo(1)
            assertThat(request.pageSize).isEqualTo(10)
        }

    @Test
    fun syncBook_returnsCanonicalIsbnWhenItDiffersFromQuery() =
        runTest {
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    totalCount = "1",
                    pageNo = "1",
                    docs = listOf(NlkBookDto(isbn = "canonical-isbn", title = "Book")),
                )

            val book = bookRepository.syncBook("scanned-raw-value").getOrThrow().book

            assertThat(book.isbn).isEqualTo("canonical-isbn")
            assertThat(fakeBookDao.getByIsbn("canonical-isbn")).isNotNull()
            assertThat(fakeBookDao.getByIsbn("scanned-raw-value")).isNull()
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

            assertThat(bookRepository.syncBook("123").getOrThrow().status).isEqualTo(BookSyncStatus.COMPLETE)

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
            fakeContentFetcher.responses[introductionUrl] = NlkContentFetchResult.Success("Fetched introduction")
            fakeContentFetcher.responses[contentsUrl] = NlkContentFetchResult.RetryableFailure

            assertThat(bookRepository.syncBook("123").getOrThrow().status)
                .isEqualTo(BookSyncStatus.AUXILIARY_CONTENT_FAILED)

            val book = fakeBookDao.getByIsbn("123")
            assertThat(book?.introduction).isEqualTo("Fetched introduction")
            assertThat(book?.tableOfContents).isNull()
            assertThat(fakeContentFetcher.requestedUrls).containsExactly(introductionUrl, contentsUrl)
        }

    @Test
    fun syncBook_unavailableContent_savesBookWithoutRetryableFailure() =
        runTest {
            val introductionUrl = "https://www.nl.go.kr/missing.txt"
            fakeNlkApi.mockResponse =
                NlkSearchResponseDto(
                    docs =
                        listOf(
                            NlkBookDto(
                                isbn = "123",
                                title = "Book",
                                bookIntroductionUrl = introductionUrl,
                            ),
                        ),
                )
            fakeContentFetcher.responses[introductionUrl] = NlkContentFetchResult.Unavailable

            assertThat(bookRepository.syncBook("123").getOrThrow().status).isEqualTo(BookSyncStatus.COMPLETE)
            assertThat(fakeBookDao.getByIsbn("123")?.title).isEqualTo("Book")
            assertThat(fakeBookDao.getByIsbn("123")?.introduction).isNull()
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
            fakeContentFetcher.responses[introductionUrl] = NlkContentFetchResult.RetryableFailure
            fakeContentFetcher.responses[contentsUrl] = NlkContentFetchResult.RetryableFailure

            assertThat(bookRepository.syncBook("123").getOrThrow().status)
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
            fakeContentFetcher.responses[summaryUrl] = NlkContentFetchResult.Success("Fetched summary as introduction")

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

data class FakeNlkRequest(
    val pageNo: Int,
    val pageSize: Int,
    val title: String?,
    val author: String?,
    val isbn: String?,
)

class FakeNlkApi : NlkApi {
    var mockResponse = NlkSearchResponseDto()
    val requests = mutableListOf<FakeNlkRequest>()
    var responseProvider: (FakeNlkRequest) -> NlkSearchResponseDto = { mockResponse }

    override suspend fun searchBooks(
        resultStyle: String,
        pageNo: Int,
        pageSize: Int,
        title: String?,
        author: String?,
        isbn: String?,
    ): NlkSearchResponseDto {
        val request = FakeNlkRequest(pageNo, pageSize, title, author, isbn)
        requests += request
        return responseProvider(request)
    }
}

class FakeNlkContentFetcher : NlkContentFetcher {
    val responses = mutableMapOf<String, NlkContentFetchResult>()
    val requestedUrls = mutableListOf<String>()

    override suspend fun fetch(url: String): NlkContentFetchResult {
        requestedUrls += url
        return responses[url] ?: NlkContentFetchResult.Unavailable
    }
}
