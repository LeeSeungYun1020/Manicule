package com.leeseungyun1020.manicule.core.database.di

import android.content.Context
import androidx.room.Room
import com.leeseungyun1020.manicule.core.database.ManiculeDatabase
import com.leeseungyun1020.manicule.core.database.dao.BookDao
import com.leeseungyun1020.manicule.core.database.dao.BookEntryDao
import com.leeseungyun1020.manicule.core.database.dao.ReadingRecordDao
import com.leeseungyun1020.manicule.core.database.dao.RecentQueryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ManiculeDatabase =
        Room
            .databaseBuilder(
                context,
                ManiculeDatabase::class.java,
                "manicule.db",
            ).build()

    @Provides
    fun provideBookDao(database: ManiculeDatabase): BookDao = database.bookDao()

    @Provides
    fun provideBookEntryDao(database: ManiculeDatabase): BookEntryDao = database.bookEntryDao()

    @Provides
    fun provideReadingRecordDao(database: ManiculeDatabase): ReadingRecordDao = database.readingRecordDao()

    @Provides
    fun provideRecentQueryDao(database: ManiculeDatabase): RecentQueryDao = database.recentQueryDao()
}
