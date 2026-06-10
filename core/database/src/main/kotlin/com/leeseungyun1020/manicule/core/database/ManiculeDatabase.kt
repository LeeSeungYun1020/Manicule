package com.leeseungyun1020.manicule.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leeseungyun1020.manicule.core.database.converter.Converters
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import com.leeseungyun1020.manicule.core.database.entity.RecentQueryEntity

@Database(
    entities = [
        BookEntity::class,
        BookEntryEntity::class,
        ReadingRecordEntity::class,
        RecentQueryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ManiculeDatabase : RoomDatabase()
