package com.leeseungyun1020.manicule.core.network.nlk

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class NlkApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: NlkApi

    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(NlkApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `searchBooks parses response correctly`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(SAMPLE_RESPONSE),
            )

            val result =
                api.searchBooks(
                    pageNo = 1,
                    pageSize = 10,
                    title = "코틀린",
                )

            assertThat(result.totalCount).isEqualTo("1")
            assertThat(result.docs).hasSize(1)

            val book = result.docs.first()
            assertThat(book.isbn).isEqualTo("9788966263370")
            assertThat(book.title).isEqualTo("코틀린 인 액션")
            assertThat(book.author).isEqualTo("드미트리 제메로프")
            assertThat(book.publisher).isEqualTo("에이콘출판")
            assertThat(book.publishPredate).isEqualTo("20200101")
            assertThat(book.prePrice).isEqualTo("36000")
        }

    @Test
    fun `searchBooks sends correct query parameters`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(EMPTY_RESPONSE))

            api.searchBooks(
                pageNo = 2,
                pageSize = 20,
                title = "검색어",
            )

            val request = server.takeRequest()
            val url = request.requestUrl!!
            assertThat(url.queryParameter("result_style")).isEqualTo("json")
            assertThat(url.queryParameter("page_no")).isEqualTo("2")
            assertThat(url.queryParameter("page_size")).isEqualTo("20")
            assertThat(url.queryParameter("title")).isEqualTo("검색어")
        }

    @Test
    fun `searchBooks with isbn sends isbn parameter`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(EMPTY_RESPONSE))

            api.searchBooks(
                pageNo = 1,
                pageSize = 10,
                isbn = "9788966263370",
            )

            val request = server.takeRequest()
            val url = request.requestUrl!!
            assertThat(url.queryParameter("isbn")).isEqualTo("9788966263370")
            assertThat(url.queryParameter("title")).isNull()
        }

    @Test
    fun `empty response returns zero docs`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(EMPTY_RESPONSE))

            val result =
                api.searchBooks(
                    pageNo = 1,
                    pageSize = 10,
                    title = "존재하지않는책",
                )

            assertThat(result.totalCount).isEqualTo("0")
            assertThat(result.docs).isEmpty()
        }

    companion object {
        private val SAMPLE_RESPONSE =
            """
            {
              "TOTAL_COUNT": "1",
              "PAGE_NO": "1",
              "docs": [
                {
                  "EA_ISBN": "9788966263370",
                  "TITLE": "코틀린 인 액션",
                  "AUTHOR": "드미트리 제메로프",
                  "PUBLISHER": "에이콘출판",
                  "PUBLISH_PREDATE": "20200101",
                  "TITLE_URL": "https://example.com/cover.jpg",
                  "PRE_PRICE": "36000",
                  "SUBJECT": "005.133",
                  "BOOK_TB_CNT_URL": "",
                  "BOOK_INTRODUCTION_URL": "",
                  "BOOK_SUMMARY_URL": "",
                  "PAGE": "464"
                }
              ]
            }
            """.trimIndent()

        private val EMPTY_RESPONSE =
            """
            {
              "TOTAL_COUNT": "0",
              "PAGE_NO": "1",
              "docs": []
            }
            """.trimIndent()
    }
}
