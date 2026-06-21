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
                    title = "kotlin in action",
                )

            assertThat(result.totalCount).isEqualTo("2")
            assertThat(result.docs).hasSize(2)

            val book = result.docs.first()
            assertThat(book.isbn).isEqualTo("9791161759692")
            assertThat(book.title).isEqualTo("Kotlin in action 2/e")
            assertThat(book.author).isEqualTo("세바스티안 아이그너,로만 엘리자로프,스베트라나 이사코바,드미트리 제메로프 지음 ;오현석 옮김")
            assertThat(book.publisher).isEqualTo("에이콘출판사")
            assertThat(book.publishPredate).isEqualTo("20250227")
            assertThat(book.prePrice).isEqualTo("48000")
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
              "TOTAL_COUNT": "2",
              "PAGE_NO": "1",
              "docs": [
                {
                  "PUBLISHER": "에이콘출판사",
                  "DDC": "",
                  "UPDATE_DATE": "20250327",
                  "BOOK_TB_CNT": "",
                  "BOOK_SUMMARY": "",
                  "EA_ADD_CODE": "93000",
                  "PUBLISHER_URL": "www.acornpub.co.kr",
                  "AUTHOR": "세바스티안 아이그너,로만 엘리자로프,스베트라나 이사코바,드미트리 제메로프 지음 ;오현석 옮김",
                  "SERIES_TITLE": "",
                  "KDC": "",
                  "EDITION_STMT": "",
                  "BOOK_TB_CNT_URL": "",
                  "SET_ISBN": "",
                  "REAL_PUBLISH_DATE": "20250227",
                  "TITLE_URL": "https://nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2025/02/9791161759692.jpg",
                  "PRE_PRICE": "48000",
                  "BOOK_INTRODUCTION_URL": "",
                  "DEPOSIT_YN": "Y",
                  "BOOK_SIZE": "188*235",
                  "BOOK_SUMMARY_URL": "",
                  "EBOOK_YN": "N",
                  "REAL_PRICE": "",
                  "FORM": "종이책",
                  "FORM_DETAIL": "무선제본",
                  "PAGE": "803 p.",
                  "CONTROL_NO": "",
                  "SERIES_NO": "",
                  "EA_ISBN": "9791161759692",
                  "INPUT_DATE": "20250221",
                  "BOOK_INTRODUCTION": "2017년 출간한 Kotlin in Action(9791161750712)의 개정판으로, 원서와 판차를 같이 합니다.",
                  "SET_EXPRESSION": "",
                  "VOL": "",
                  "CIP_YN": "",
                  "SUBJECT": "0",
                  "BIB_YN": "Y",
                  "TITLE": "Kotlin in action 2/e",
                  "PUBLISH_PREDATE": "20250227",
                  "RELATED_ISBN": "",
                  "SET_ADD_CODE": ""
                },
                {
                  "PUBLISHER": "에이콘",
                  "DDC": "005.133",
                  "UPDATE_DATE": "20171108",
                  "BOOK_TB_CNT": "",
                  "BOOK_SUMMARY": "",
                  "EA_ADD_CODE": "94000",
                  "PUBLISHER_URL": "www.acornpub.co.kr",
                  "AUTHOR": "드미트리 제메로프,스베트라나 이사코바 지음 ;오현석 옮김",
                  "SERIES_TITLE": "에이콘 모바일 프로그래밍 시리즈",
                  "KDC": "005.133",
                  "EDITION_STMT": "",
                  "BOOK_TB_CNT_URL": "https://www.nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/6399868_6.txt",
                  "SET_ISBN": "9788960770836",
                  "REAL_PUBLISH_DATE": "",
                  "TITLE_URL": "http://www.nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/6399868_3.jpg",
                  "PRE_PRICE": "36000",
                  "BOOK_INTRODUCTION_URL": "https://www.nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/6399868_5.txt",
                  "DEPOSIT_YN": "Y",
                  "BOOK_SIZE": "",
                  "BOOK_SUMMARY_URL": "",
                  "EBOOK_YN": "N",
                  "REAL_PRICE": "",
                  "FORM": "종이책",
                  "FORM_DETAIL": "무선제본",
                  "PAGE": "574 p.",
                  "CONTROL_NO": "CIP2017027395",
                  "SERIES_NO": "",
                  "EA_ISBN": "9791161750712",
                  "INPUT_DATE": "20171020",
                  "BOOK_INTRODUCTION": "",
                  "SET_EXPRESSION": "",
                  "VOL": "",
                  "CIP_YN": "Y",
                  "SUBJECT": "0",
                  "BIB_YN": "Y",
                  "TITLE": "Kotlin in action",
                  "PUBLISH_PREDATE": "20171031",
                  "RELATED_ISBN": "",
                  "SET_ADD_CODE": "94000 "
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
