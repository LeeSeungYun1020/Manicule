package com.leeseungyun1020.manicule.core.network.nlk

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import java.io.IOException

class NlkContentFetcherTest {
    private val requestedUrls = mutableListOf<String>()
    private var responseCode = 200
    private var responseBody = " content "
    private var failure: IOException? = null
    private val client =
        OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    requestedUrls += chain.request().url.toString()
                    failure?.let { throw it }
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(responseCode)
                        .message("Response")
                        .body(responseBody.toResponseBody("text/plain".toMediaType()))
                        .build()
                },
            ).build()
    private val fetcher = OkHttpNlkContentFetcher(client)

    @Test
    fun fetch_successfulResponse_returnsContent() =
        runTest {
            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.Success("content"))
            assertThat(fetcher.fetch("https://nl.go.kr/contents.txt"))
                .isEqualTo(NlkContentFetchResult.Success("content"))
            assertThat(requestedUrls).hasSize(2)
        }

    @Test
    fun fetch_invalidUrl_returnsUnavailable() =
        runTest {
            assertThat(fetcher.fetch("http://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.Unavailable)
            assertThat(fetcher.fetch("https://nl.go.kr.evil.example/book.txt"))
                .isEqualTo(NlkContentFetchResult.Unavailable)
            assertThat(fetcher.fetch("not a url"))
                .isEqualTo(NlkContentFetchResult.Unavailable)
            assertThat(requestedUrls).isEmpty()
        }

    @Test
    fun fetch_blankBody_returnsUnavailable() =
        runTest {
            responseBody = "  "

            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.Unavailable)
        }

    @Test
    fun fetch_notFound_returnsUnavailable() =
        runTest {
            responseCode = 404

            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.Unavailable)
        }

    @Test
    fun fetch_requestTimeout_returnsRetryableFailure() =
        runTest {
            responseCode = 408

            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.RetryableFailure)
        }

    @Test
    fun fetch_tooManyRequests_returnsRetryableFailure() =
        runTest {
            responseCode = 429

            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.RetryableFailure)
        }

    @Test
    fun fetch_serverError_returnsRetryableFailure() =
        runTest {
            responseCode = 503

            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.RetryableFailure)
        }

    @Test
    fun fetch_ioFailure_returnsRetryableFailure() =
        runTest {
            failure = IOException("unavailable")

            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt"))
                .isEqualTo(NlkContentFetchResult.RetryableFailure)
        }
}
