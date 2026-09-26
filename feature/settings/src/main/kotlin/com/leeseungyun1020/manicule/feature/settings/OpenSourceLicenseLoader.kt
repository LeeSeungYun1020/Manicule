package com.leeseungyun1020.manicule.feature.settings

import android.content.Context
import com.leeseungyun1020.manicule.core.common.di.Dispatcher
import com.leeseungyun1020.manicule.core.common.di.ManiculeDispatcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

interface OpenSourceLicenseLoader {
    suspend fun loadLibraries(): List<OpenSourceLibrary>
}

@Singleton
class DefaultOpenSourceLicenseLoader internal constructor(
    private val openRawResource: (Int) -> InputStream,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : OpenSourceLicenseLoader {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        @Dispatcher(ManiculeDispatcher.IO) ioDispatcher: CoroutineDispatcher,
    ) : this(
        openRawResource = { id -> context.resources.openRawResource(id) },
        ioDispatcher = ioDispatcher,
    )

    override suspend fun loadLibraries(): List<OpenSourceLibrary> =
        withContext(ioDispatcher) {
            openRawResource(R.raw.licenses).use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString<List<OpenSourceLibrary>>(jsonString)
            }
        }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LicenseLoaderModule {
    @Binds
    abstract fun bindOpenSourceLicenseLoader(impl: DefaultOpenSourceLicenseLoader): OpenSourceLicenseLoader
}
