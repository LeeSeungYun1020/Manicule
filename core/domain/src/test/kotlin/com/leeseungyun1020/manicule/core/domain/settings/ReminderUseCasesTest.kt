package com.leeseungyun1020.manicule.core.domain.settings

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import org.junit.Test

class ReminderUseCasesTest {
    @Test
    fun enabledReminder_isSavedAndScheduled() =
        runTest {
            val preferences = FakeUserPreferencesRepository()
            val scheduler = FakeReminderScheduler()
            val config = ReminderConfig(enabled = true, time = LocalTime(8, 30))

            SetReminderUseCase(preferences, scheduler)(config)

            assertThat(preferences.savedReminder).isEqualTo(config)
            assertThat(scheduler.scheduledTime).isEqualTo(config.time)
            assertThat(scheduler.cancelled).isFalse()
        }

    @Test
    fun disabledReminder_isSavedAndCancelled() =
        runTest {
            val preferences = FakeUserPreferencesRepository()
            val scheduler = FakeReminderScheduler()
            val config = ReminderConfig(enabled = false, time = LocalTime(19, 0))

            SetReminderUseCase(preferences, scheduler)(config)

            assertThat(preferences.savedReminder).isEqualTo(config)
            assertThat(scheduler.scheduledTime).isNull()
            assertThat(scheduler.cancelled).isTrue()
        }

    @Test
    fun reminderContent_usesMostRecentlyUpdatedReadingBook() =
        runTest {
            val older = bookEntry("Older", Instant.parse("2026-08-01T00:00:00Z"))
            val newer = bookEntry("Newer", Instant.parse("2026-08-02T00:00:00Z"))

            val content = GetReminderContentUseCase(FakeLibraryRepository(listOf(older, newer)))()

            assertThat(content).isEqualTo(ReminderContent.Book("Newer"))
        }

    @Test
    fun reminderContent_fallsBackWhenNoReadingBookExists() =
        runTest {
            val content = GetReminderContentUseCase(FakeLibraryRepository(emptyList()))()

            assertThat(content).isEqualTo(ReminderContent.Generic)
        }
}

private class FakeReminderScheduler : ReminderScheduler {
    var scheduledTime: LocalTime? = null
    var cancelled = false

    override suspend fun schedule(time: LocalTime) {
        scheduledTime = time
    }

    override suspend fun cancel() {
        cancelled = true
    }
}

private class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val preferences = MutableStateFlow(UserPreferences.Default)
    var savedReminder: ReminderConfig? = null

    override val userPreferences: Flow<UserPreferences> = preferences

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        preferences.value = preferences.value.copy(themeMode = themeMode)
    }

    override suspend fun setReminderConfig(config: ReminderConfig) {
        savedReminder = config
        preferences.value = preferences.value.copy(reminder = config)
    }
}

private class FakeLibraryRepository(
    private val entries: List<BookEntry>,
) : LibraryRepository {
    override fun observeAll(): Flow<List<BookEntry>> = flowOf(entries)

    override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> = flowOf(entries.filter { it.status == status })

    override fun observeBookEntry(isbn: String): Flow<BookEntry?> = flowOf(entries.firstOrNull { it.book.isbn == isbn })

    override suspend fun saveBookEntry(entry: BookEntry) = Unit

    override suspend fun removeBookEntry(isbn: String) = Unit
}

private fun bookEntry(
    title: String,
    updatedAt: Instant,
) = BookEntry(
    book =
        Book(
            isbn = title,
            title = title,
            author = "Author",
            publisher = "Publisher",
            publishedDate = null,
            coverUrl = null,
            totalPages = null,
            price = null,
            category = null,
            tableOfContentsUrl = null,
            introductionUrl = null,
            summaryUrl = null,
        ),
    status = ReadingStatus.READING,
    addedAt = updatedAt,
    updatedAt = updatedAt,
)
