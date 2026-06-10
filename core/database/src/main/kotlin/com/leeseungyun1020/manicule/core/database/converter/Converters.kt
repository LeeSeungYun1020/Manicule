package com.leeseungyun1020.manicule.core.database.converter

import androidx.room.TypeConverter
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun fromReadingStatus(value: ReadingStatus?): String? = value?.name

    @TypeConverter
    fun toReadingStatus(value: String?): ReadingStatus? = ReadingStatus.fromOrNull(value)
}
