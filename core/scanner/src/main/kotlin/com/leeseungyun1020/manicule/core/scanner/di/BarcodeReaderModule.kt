package com.leeseungyun1020.manicule.core.scanner.di

import com.leeseungyun1020.manicule.core.scanner.BarcodeReaderFactory
import com.leeseungyun1020.manicule.core.scanner.MlKitBarcodeReaderFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BarcodeReaderModule {
    @Provides
    fun provideBarcodeReaderFactory(): BarcodeReaderFactory = MlKitBarcodeReaderFactory()
}
