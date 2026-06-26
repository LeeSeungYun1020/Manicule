package com.leeseungyun1020.manicule.core.data.di

import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSourceImpl
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSourceImpl
import com.leeseungyun1020.manicule.core.data.datasource.ReadingRecordLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.ReadingRecordLocalDataSourceImpl
import com.leeseungyun1020.manicule.core.data.datasource.SearchHistoryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.SearchHistoryLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindBookLocalDataSource(dataSource: BookLocalDataSourceImpl): BookLocalDataSource

    @Binds
    abstract fun bindBookRemoteDataSource(dataSource: BookRemoteDataSourceImpl): BookRemoteDataSource

    @Binds
    abstract fun bindReadingRecordLocalDataSource(dataSource: ReadingRecordLocalDataSourceImpl): ReadingRecordLocalDataSource

    @Binds
    abstract fun bindSearchHistoryLocalDataSource(dataSource: SearchHistoryLocalDataSourceImpl): SearchHistoryLocalDataSource
}
