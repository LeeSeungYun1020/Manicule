package com.leeseungyun1020.manicule.core.network.nlk

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.network.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class NlkAuthInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        client =
            OkHttpClient
                .Builder()
                .addInterceptor(NlkAuthInterceptor())
                .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `interceptor adds cert_key when missing`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val request =
            Request
                .Builder()
                .url(server.url("/seoji/SearchApi.do?result_style=json"))
                .build()

        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertThat(recorded.requestUrl?.queryParameter("cert_key")).isEqualTo(BuildConfig.NLK_AUTH_KEY)
    }

    @Test
    fun `interceptor does not overwrite existing cert_key`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val request =
            Request
                .Builder()
                .url(server.url("/seoji/SearchApi.do?cert_key=custom-key"))
                .build()

        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertThat(recorded.requestUrl!!.queryParameter("cert_key")).isEqualTo("custom-key")
    }
}
