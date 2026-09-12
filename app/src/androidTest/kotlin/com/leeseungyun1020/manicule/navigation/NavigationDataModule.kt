package com.leeseungyun1020.manicule.navigation

import com.leeseungyun1020.manicule.core.data.di.DataModule
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepositoryImpl
import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.data.repository.StatsRepositoryImpl
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DataModule::class])
abstract class NavigationDataModule {
    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: NavigationBooks): BookRepository

    @Binds
    @Singleton
    abstract fun bindReadingRecordRepository(impl: ReadingRecordRepositoryImpl): ReadingRecordRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: NavigationHistory): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: NavigationLibrary): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
