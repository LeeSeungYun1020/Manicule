package com.leeseungyun1020.manicule.core.network.di

import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import com.leeseungyun1020.manicule.core.network.nlk.NlkAuthInterceptor
import com.leeseungyun1020.manicule.core.network.nlk.NlkContentFetcher
import com.leeseungyun1020.manicule.core.network.nlk.OkHttpNlkContentFetcher
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingsModule {
    @Binds
    @Singleton
    abstract fun bindNlkContentFetcher(fetcher: OkHttpNlkContentFetcher): NlkContentFetcher
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    @NLKApiOkHttpClient
    fun provideNLKApiOkHttpClient(authInterceptor: NlkAuthInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(nlkHttpLoggingInterceptor())
            .build()

    @Provides
    @Singleton
    @NLKContentOkHttpClient
    fun provideNLKContentOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(nlkHttpLoggingInterceptor())
            .build()

    @Provides
    @Singleton
    fun provideNlkApi(
        @NLKApiOkHttpClient client: OkHttpClient,
        json: Json,
    ): NlkApi =
        Retrofit
            .Builder()
            .baseUrl(NLK_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NlkApi::class.java)

    internal fun nlkHttpLoggingInterceptor(
        logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger.DEFAULT,
    ): HttpLoggingInterceptor {
        val redactingLogger =
            HttpLoggingInterceptor.Logger { message ->
                logger.log(
                    message.replace(certKeyQueryPattern) { match ->
                        "${match.groupValues[1]}$REDACTED_VALUE"
                    },
                )
            }
        return HttpLoggingInterceptor(redactingLogger).apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    private val certKeyQueryPattern = Regex("""([?&]cert_key=)[^&#\s]*""")

    private const val NLK_BASE_URL = "https://www.nl.go.kr/"
    private const val REDACTED_VALUE = "██"
}
