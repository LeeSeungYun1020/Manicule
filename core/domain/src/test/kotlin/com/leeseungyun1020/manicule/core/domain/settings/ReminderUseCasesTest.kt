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
    fun enabledReminder_restoresPreviousConfigWhenSchedulingFails() =
        runTest {
            val preferences = FakeUserPreferencesRepository()
            val expectedFailure = IllegalStateException("Scheduling failed")
            val scheduler = FakeReminderScheduler(scheduleFailure = expectedFailure)
            val config = ReminderConfig(enabled = true, time = LocalTime(8, 30))

            val result = runCatching { SetReminderUseCase(preferences, scheduler)(config) }

            assertThat(result.exceptionOrNull()).isSameInstanceAs(expectedFailure)
            assertThat(preferences.savedReminder).isEqualTo(ReminderConfig.Default)
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
    fun activeReminderTime_returnsEnabledReminderTime() =
        runTest {
            val preferences = FakeUserPreferencesRepository()
            val config = ReminderConfig(enabled = true, time = LocalTime(8, 30))
            preferences.setReminderConfig(config)

            val time = GetActiveReminderTimeUseCase(preferences)()

            assertThat(time).isEqualTo(config.time)
        }

    @Test
    fun activeReminderTime_returnsNullWhenReminderIsDisabled() =
        runTest {
            val preferences = FakeUserPreferencesRepository()

            val time = GetActiveReminderTimeUseCase(preferences)()

            assertThat(time).isNull()
        }

    @Test
    fun reminderContent_returnsFiveMostRecentlyUpdatedReadingBooks() =
        runTest {
            val entries =
                (1..6).map { day ->
                    bookEntry("Book $day", Instant.parse("2026-08-0${day}T00:00:00Z"))
                }

            val content = GetReminderContentUseCase(FakeLibraryRepository(entries))()

            assertThat(content)
                .containsExactly(
                    ReminderContent.Book("Book 6"),
                    ReminderContent.Book("Book 5"),
                    ReminderContent.Book("Book 4"),
                    ReminderContent.Book("Book 3"),
                    ReminderContent.Book("Book 2"),
                ).inOrder()
        }

    @Test
    fun reminderContent_returnsEmptyListWhenNoReadingBookExists() =
        runTest {
            val content = GetReminderContentUseCase(FakeLibraryRepository(emptyList()))()

            assertThat(content).isEmpty()
        }

    @Test
    fun reminderContent_excludesBlankTitles() =
        runTest {
            val entries =
                listOf(
                    bookEntry("", Instant.parse("2026-08-02T00:00:00Z")),
                    bookEntry("Book", Instant.parse("2026-08-01T00:00:00Z")),
                )

            val content = GetReminderContentUseCase(FakeLibraryRepository(entries))()

            assertThat(content).containsExactly(ReminderContent.Book("Book"))
        }
}

private class FakeReminderScheduler(
    private val scheduleFailure: Exception? = null,
) : ReminderScheduler {
    var scheduledTime: LocalTime? = null
    var cancelled = false

    override suspend fun schedule(time: LocalTime) {
        scheduleFailure?.let { throw it }
        scheduledTime = time
    }

    override suspend fun scheduleNext(time: LocalTime) {
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

    override suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book> =
        entries
            .filter { it.status == status }
            .sortedWith(compareByDescending<BookEntry> { it.updatedAt }.thenBy { it.book.isbn })
            .take(limit)
            .map(BookEntry::book)

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
