package com.leeseungyun1020.manicule.core.network.nlk

import com.leeseungyun1020.manicule.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * NLK API 요청에 인증키(cert_key)를 자동 주입하는 Interceptor.
 *
 * [NlkApi]의 각 메서드에서 cert_key를 직접 전달하지 않아도 되도록 한다.
 * cert_key가 이미 쿼리에 포함되어 있으면 덮어쓰지 않는다.
 */
class NlkAuthInterceptor
    @Inject
    constructor() : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val url = original.url

            if (url.queryParameter(QUERY_CERT_KEY) != null) {
                return chain.proceed(original)
            }

            val newUrl =
                url
                    .newBuilder()
                    .addQueryParameter(QUERY_CERT_KEY, BuildConfig.NLK_AUTH_KEY)
                    .build()

            return chain.proceed(original.newBuilder().url(newUrl).build())
        }

        companion object {
            private const val QUERY_CERT_KEY = "cert_key"
        }
    }
