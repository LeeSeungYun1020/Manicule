package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@Immutable
data class CustomPeriodRange(
    val start: LocalDate,
    val end: LocalDate,
) {
    val dayCount: Int get() = end.toEpochDays() - start.toEpochDays() + 1

    fun isValid(today: LocalDate): Boolean = validate(today) == null

    fun validate(today: LocalDate): ValidationError? {
        if (start > end) return ValidationError.START_AFTER_END
        if (end > today) return ValidationError.FUTURE_DATE
        if (dayCount > MAX_DAYS) return ValidationError.EXCEEDS_MAX_DAYS
        return null
    }

    fun formatIso(): String = "$start/$end"

    enum class ValidationError {
        START_AFTER_END,
        EXCEEDS_MAX_DAYS,
        FUTURE_DATE,
    }

    companion object {
        const val MAX_DAYS = 365
        const val DEFAULT_DAYS = 28

        fun defaultFor(today: LocalDate): CustomPeriodRange = CustomPeriodRange(today.minus(DatePeriod(days = DEFAULT_DAYS - 1)), today)

        fun parseIso(raw: String): CustomPeriodRange? {
            val parts = if ('/' in raw) raw.split('/') else raw.split("..")
            if (parts.size != 2) return null
            return runCatching {
                val start = LocalDate.parse(parts[0].trim())
                val end = LocalDate.parse(parts[1].trim())
                CustomPeriodRange(start, end)
            }.getOrNull()
        }
    }
}
