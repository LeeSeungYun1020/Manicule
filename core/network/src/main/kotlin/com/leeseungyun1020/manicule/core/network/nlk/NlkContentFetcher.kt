package com.leeseungyun1020.manicule.core.network.nlk

import com.leeseungyun1020.manicule.core.network.di.NLKContentOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

sealed interface NlkContentFetchResult {
    data class Success(
        val content: String,
    ) : NlkContentFetchResult

    data object Unavailable : NlkContentFetchResult

    data object RetryableFailure : NlkContentFetchResult
}

interface NlkContentFetcher {
    suspend fun fetch(url: String): NlkContentFetchResult
}

class OkHttpNlkContentFetcher
    @Inject
    constructor(
        @param:NLKContentOkHttpClient private val client: OkHttpClient,
    ) : NlkContentFetcher {
        override suspend fun fetch(url: String): NlkContentFetchResult =
            withContext(Dispatchers.IO) {
                val parsedUrl = url.toHttpUrlOrNull() ?: return@withContext NlkContentFetchResult.Unavailable
                if (parsedUrl.scheme != HTTPS_SCHEME || !parsedUrl.host.isAllowedNlkHost()) {
                    return@withContext NlkContentFetchResult.Unavailable
                }

                try {
                    client.newCall(Request.Builder().url(parsedUrl).build()).execute().use { response ->
                        when {
                            response.isSuccessful ->
                                response.body
                                    ?.string()
                                    ?.trim()
                                    ?.takeIf(String::isNotEmpty)
                                    ?.let(NlkContentFetchResult::Success)
                                    ?: NlkContentFetchResult.Unavailable

                            response.code == HTTP_REQUEST_TIMEOUT ||
                                response.code == HTTP_TOO_MANY_REQUESTS ||
                                response.code in HTTP_SERVER_ERROR_RANGE ->
                                NlkContentFetchResult.RetryableFailure

                            else -> NlkContentFetchResult.Unavailable
                        }
                    }
                } catch (_: IOException) {
                    NlkContentFetchResult.RetryableFailure
                }
            }

        private fun String.isAllowedNlkHost(): Boolean = this == NLK_HOST || endsWith(".$NLK_HOST")

        private companion object {
            const val HTTPS_SCHEME = "https"
            const val NLK_HOST = "nl.go.kr"
            const val HTTP_REQUEST_TIMEOUT = 408
            const val HTTP_TOO_MANY_REQUESTS = 429
            val HTTP_SERVER_ERROR_RANGE = 500..599
        }
    }
