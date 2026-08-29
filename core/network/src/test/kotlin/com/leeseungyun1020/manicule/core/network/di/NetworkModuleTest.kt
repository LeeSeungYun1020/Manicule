package com.leeseungyun1020.manicule.core.network.di

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.network.BuildConfig
import com.leeseungyun1020.manicule.core.network.nlk.NlkAuthInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test

class NetworkModuleTest {
    @Test
    fun apiClient_addsCertKey() {
        var requestedUrl = ""
        val client =
            NetworkModule
                .provideNLKApiOkHttpClient(NlkAuthInterceptor())
                .withTerminalInterceptor { request -> requestedUrl = request.url.toString() }

        client.execute("https://www.nl.go.kr/seoji/SearchApi.do")

        assertThat(requestedUrl).contains("cert_key=${BuildConfig.NLK_AUTH_KEY}")
    }

    @Test
    fun contentClient_doesNotAddCertKey() {
        var requestedUrl = ""
        val client =
            NetworkModule
                .provideNLKContentOkHttpClient()
                .withTerminalInterceptor { request -> requestedUrl = request.url.toString() }

        client.execute("https://www.nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/introduction.txt")

        assertThat(requestedUrl).doesNotContain("cert_key")
    }

    @Test
    fun logging_redactsCertKey() {
        val logs = mutableListOf<String>()
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(NlkAuthInterceptor())
                .addInterceptor(
                    NetworkModule.nlkHttpLoggingInterceptor(
                        logger = HttpLoggingInterceptor.Logger { message -> logs += message },
                    ),
                ).build()
                .withTerminalInterceptor()

        client.execute("https://www.nl.go.kr/seoji/SearchApi.do")

        val output = logs.joinToString("\n")
        assertThat(output).contains("cert_key")
        assertThat(output).doesNotContain(BuildConfig.NLK_AUTH_KEY)
    }

    @Test
    fun logging_redactsEncodedCertKeyQueryParameter() {
        val logs = mutableListOf<String>()
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(
                    NetworkModule.nlkHttpLoggingInterceptor(
                        logger = HttpLoggingInterceptor.Logger { message -> logs += message },
                    ),
                ).build()
                .withTerminalInterceptor()

        client.execute(
            "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=encoded%2Fsecret%2Bvalue&result_style=json",
        )

        val output = logs.joinToString("\n")
        assertThat(output).contains("cert_key=██")
        assertThat(output).doesNotContain("encoded%2Fsecret%2Bvalue")
        assertThat(output).contains("result_style=json")
    }

    private fun OkHttpClient.withTerminalInterceptor(onRequest: (Request) -> Unit = {}): OkHttpClient =
        newBuilder()
            .addInterceptor(
                Interceptor { chain ->
                    onRequest(chain.request())
                    Response
                        .Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("".toResponseBody())
                        .build()
                },
            ).build()

    private fun OkHttpClient.execute(url: String) {
        newCall(Request.Builder().url(url).build()).execute().close()
    }
}
