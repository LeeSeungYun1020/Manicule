package com.leeseungyun1020.manicule.core.data.di

import com.leeseungyun1020.manicule.core.data.datasource.BookEntryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSourceImpl
import com.leeseungyun1020.manicule.core.data.datasource.DataStoreUserPreferencesLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.ReadingRecordLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.RoomBookEntryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.RoomBookLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.RoomReadingRecordLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.SearchHistoryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.SearchHistoryLocalDataSourceImpl
import com.leeseungyun1020.manicule.core.data.datasource.UserPreferencesLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindBookLocalDataSource(dataSource: RoomBookLocalDataSource): BookLocalDataSource

    @Binds
    abstract fun bindBookRemoteDataSource(dataSource: BookRemoteDataSourceImpl): BookRemoteDataSource

    @Binds
    abstract fun bindReadingRecordLocalDataSource(dataSource: RoomReadingRecordLocalDataSource): ReadingRecordLocalDataSource

    @Binds
    abstract fun bindSearchHistoryLocalDataSource(dataSource: SearchHistoryLocalDataSourceImpl): SearchHistoryLocalDataSource

    @Binds
    abstract fun bindBookEntryLocalDataSource(dataSource: RoomBookEntryLocalDataSource): BookEntryLocalDataSource

    @Binds
    abstract fun bindUserPreferencesLocalDataSource(dataSource: DataStoreUserPreferencesLocalDataSource): UserPreferencesLocalDataSource
}
