package com.leeseungyun1020.manicule.core.network.nlk

import com.leeseungyun1020.manicule.core.network.di.NLKOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

interface NlkContentFetcher {
    suspend fun fetch(url: String): String?
}

class OkHttpNlkContentFetcher
    @Inject
    constructor(
        @param:NLKOkHttpClient private val client: OkHttpClient,
    ) : NlkContentFetcher {
        override suspend fun fetch(url: String): String? =
            withContext(Dispatchers.IO) {
                val parsedUrl = url.toHttpUrlOrNull() ?: return@withContext null
                if (parsedUrl.scheme != HTTPS_SCHEME || !parsedUrl.host.isAllowedNlkHost()) return@withContext null

                client.newCall(Request.Builder().url(parsedUrl).build()).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    response.body?.string()?.trim()?.ifBlank { null }
                }
            }

        private fun String.isAllowedNlkHost(): Boolean = this == NLK_HOST || endsWith(".$NLK_HOST")

        private companion object {
            const val HTTPS_SCHEME = "https"
            const val NLK_HOST = "nl.go.kr"
        }
    }
