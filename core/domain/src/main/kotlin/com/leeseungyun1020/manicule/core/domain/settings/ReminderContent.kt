package com.leeseungyun1020.manicule.core.domain.settings

sealed interface ReminderContent {
    data class Book(
        val title: String,
    ) : ReminderContent

    data object Generic : ReminderContent
}
