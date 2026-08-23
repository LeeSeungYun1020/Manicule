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

class NlkContentFetcherTest {
    private val requestedUrls = mutableListOf<String>()
    private val client =
        OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    requestedUrls += chain.request().url.toString()
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(" content ".toResponseBody("text/plain".toMediaType()))
                        .build()
                },
            ).build()
    private val fetcher = OkHttpNlkContentFetcher(client)

    @Test
    fun fetch_allowsHttpsNlkHosts() =
        runTest {
            assertThat(fetcher.fetch("https://www.nl.go.kr/book.txt")).isEqualTo("content")
            assertThat(fetcher.fetch("https://nl.go.kr/contents.txt")).isEqualTo("content")
            assertThat(requestedUrls).hasSize(2)
        }

    @Test
    fun fetch_rejectsHttpAndForeignHosts_beforeRequest() =
        runTest {
            assertThat(fetcher.fetch("http://www.nl.go.kr/book.txt")).isNull()
            assertThat(fetcher.fetch("https://nl.go.kr.evil.example/book.txt")).isNull()
            assertThat(fetcher.fetch("not a url")).isNull()
            assertThat(requestedUrls).isEmpty()
        }
}
