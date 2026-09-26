package com.leeseungyun1020.manicule.feature.home

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.home.HomeData
import com.leeseungyun1020.manicule.core.domain.home.HomeReadingSummary
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.leeseungyun1020.manicule.core.ui.R as CoreUiR

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun firstUser_showsDisabledSummaryAndOnboarding() {
        setHome(HomeUiState.Content(homeData()))

        composeRule.onNodeWithText(context.getString(R.string.home_onboarding_heading)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_onboarding_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_empty_summary_description)).assertIsDisplayed()
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.home_days, 0, 0)).assertIsDisplayed()
        composeRule
            .onAllNodesWithText(context.resources.getQuantityString(R.plurals.home_reading_books, 1, 1))
            .assertCountEquals(0)
    }

    @Test
    fun readingBook_requestsBookDetail() {
        var selectedIsbn: String? = null
        setHome(
            HomeUiState.Content(homeData(readingBooks = listOf(bookEntry()))),
            onBookSelected = { selectedIsbn = it },
        )

        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.home_reading_books, 1, 1)).assertIsDisplayed()
        composeRule.onNodeWithText("In progress").performClick()
        composeRule.onNodeWithText("10 / 100").assertDoesNotExist()
        composeRule.onNodeWithText("10%").assertDoesNotExist()

        assertThat(selectedIsbn).isEqualTo("9780000000001")
    }

    @Test
    fun readingBooks_rowUsesFullWidthAndStartsAtScreenInset() {
        setHome(HomeUiState.Content(homeData(readingBooks = listOf(bookEntry()))))

        val row = composeRule.onNodeWithTag("home_reading_books").fetchSemanticsNode().boundsInRoot
        val header =
            composeRule
                .onNodeWithText(context.resources.getQuantityString(R.plurals.home_reading_books, 1, 1))
                .fetchSemanticsNode()
                .boundsInRoot
        val book = composeRule.onNodeWithText("In progress").fetchSemanticsNode().boundsInRoot

        assertThat(row.left).isLessThan(header.left)
        assertThat(book.left).isEqualTo(header.left)
    }

    @Test
    fun continuingUser_showsRecentDaysInOneRow() {
        setHome(HomeUiState.Content(homeData(hasLibraryBooks = true)))

        val weekdays = context.resources.getStringArray(R.array.home_weekdays)
        val firstDay = composeRule.onNodeWithText(weekdays[1], useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val lastDay = composeRule.onNodeWithText(weekdays[0], useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

        assertThat(firstDay.top).isEqualTo(lastDay.top)
        assertThat(firstDay.right).isLessThan(lastDay.left)
    }

    @Test
    fun continuingUser_exposesEachRecentDayDateAndPages() {
        val data = homeData(hasLibraryBooks = true)
        val summary = requireNotNull(data.summary)
        val days = summary.recentDays.mapIndexed { index, day -> ReadingCalendarDay.of(day.date, if (index == 1) 25 else 0) }
        setHome(HomeUiState.Content(data.copy(summary = summary.copy(recentDays = days))))

        val emptyDay = days.first().date
        val emptyDescription =
            context.getString(
                CoreUiR.string.reading_calendar_cell_no_record_content_description,
                emptyDay.year,
                emptyDay.monthNumber,
                emptyDay.dayOfMonth,
            )
        val readDay = days[1].date
        val readDescription =
            context.resources.getQuantityString(
                CoreUiR.plurals.reading_calendar_cell_content_description,
                25,
                readDay.year,
                readDay.monthNumber,
                readDay.dayOfMonth,
                25,
            )

        composeRule.onNodeWithContentDescription(emptyDescription).assertIsDisplayed().assertHasNoClickAction()
        composeRule.onNodeWithContentDescription(readDescription).assertIsDisplayed().assertHasNoClickAction()
    }

    @Test
    fun readingBooks_more_requestsReadingLibrary() {
        var showReading = false
        setHome(
            HomeUiState.Content(homeData(readingBooks = listOf(bookEntry()))),
            onShowReadingBooks = { showReading = true },
        )

        composeRule
            .onNode(hasText(context.getString(R.string.home_more)) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()

        assertThat(showReading).isTrue()
    }

    @Test
    fun search_requestsSearchNavigation() {
        var searched = false
        setHome(HomeUiState.Content(homeData()), onSearch = { searched = true })

        composeRule.onNodeWithText(context.getString(R.string.home_search_placeholder)).performClick()

        assertThat(searched).isTrue()
    }

    @Test
    fun scan_requestsScannerNavigation() {
        var scanned = false
        setHome(HomeUiState.Content(homeData()), onScan = { scanned = true })

        composeRule.onNodeWithContentDescription(context.getString(R.string.home_scan)).performClick()

        assertThat(scanned).isTrue()
    }

    @Test
    fun search_remainsAvailableWhileHomeLoads() {
        var searched = false
        setHome(HomeUiState.Loading, onSearch = { searched = true })

        composeRule.onNodeWithText(context.getString(R.string.home_search_placeholder)).performClick()

        assertThat(searched).isTrue()
    }

    @Test
    fun noReadingBooksWithWant_showsChooseAction() {
        var choseWantBook = false
        setHome(
            HomeUiState.Content(homeData(hasLibraryBooks = true, wantBookCount = 2)),
            onChooseWantBook = { choseWantBook = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.home_choose)).performClick()
        composeRule.onNodeWithContentDescription(context.getString(R.string.home_search)).assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription(context.getString(R.string.home_scan)).assertCountEquals(2)

        assertThat(choseWantBook).isTrue()
    }

    @Test
    fun error_showsRetry() {
        var retried = false
        setHome(HomeUiState.Error, onRetry = { retried = true })

        composeRule.onNodeWithText(context.getString(R.string.home_retry)).performClick()

        assertThat(retried).isTrue()
    }

    @Test
    fun summaryFailure_keepsReadingBooksVisible() {
        setHome(HomeUiState.Content(homeData(readingBooks = listOf(bookEntry())).copy(summary = null)))

        composeRule.onNodeWithText("In progress").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_summary_error_title)).assertIsDisplayed()
    }

    @Test
    fun readingSummary_requestsStatsNavigation() {
        var showStats = false
        setHome(HomeUiState.Content(homeData(hasLibraryBooks = true)), onShowStats = { showStats = true })

        composeRule.onNodeWithText(context.getString(R.string.home_streak)).performClick()

        assertThat(showStats).isTrue()
    }

    @Test
    fun noReadingBooksWithoutWant_scan_requestsScannerNavigation() {
        var scanned = false
        setHome(
            HomeUiState.Content(homeData(hasLibraryBooks = true, wantBookCount = 0)),
            onScan = { scanned = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.home_scan)).performClick()

        assertThat(scanned).isTrue()
    }

    private fun setHome(
        state: HomeUiState,
        onSearch: () -> Unit = {},
        onScan: () -> Unit = {},
        onBookSelected: (String) -> Unit = {},
        onShowReadingBooks: () -> Unit = {},
        onChooseWantBook: () -> Unit = {},
        onShowStats: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeRule.setContent {
            ManiculeTheme {
                HomeScreen(
                    uiState = state,
                    onSearch = onSearch,
                    onScan = onScan,
                    onBookSelected = onBookSelected,
                    onShowReadingBooks = onShowReadingBooks,
                    onChooseWantBook = onChooseWantBook,
                    onShowStats = onShowStats,
                    onRetry = onRetry,
                )
            }
        }
    }

    private fun homeData(
        hasLibraryBooks: Boolean = false,
        readingBooks: List<BookEntry> = emptyList(),
        wantBookCount: Int = 0,
    ): HomeData {
        val today = LocalDate(2026, 9, 21)
        return HomeData(
            hasLibraryBooks = hasLibraryBooks || readingBooks.isNotEmpty(),
            hasReadingRecords = false,
            readingBooks = readingBooks,
            wantBookCount = wantBookCount,
            summary =
                HomeReadingSummary(
                    today = today,
                    todayPages = 0,
                    currentStreak = 0,
                    recentDays = (0..6).map { index -> ReadingCalendarDay.of(today.minus(DatePeriod(days = 6 - index)), 0) },
                ),
        )
    }

    private fun bookEntry(): BookEntry =
        BookEntry(
            book =
                Book(
                    isbn = "9780000000001",
                    title = "In progress",
                    author = "Author",
                    publisher = "Publisher",
                    publishedDate = null,
                    coverUrl = null,
                    totalPages = 100,
                    price = null,
                    category = null,
                    tableOfContentsUrl = null,
                    introductionUrl = null,
                    summaryUrl = null,
                ),
            status = ReadingStatus.READING,
            addedAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
            currentPage = 10,
        )

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}
