package com.leeseungyun1020.manicule.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import com.leeseungyun1020.manicule.MainActivity
import com.leeseungyun1020.manicule.R
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import com.leeseungyun1020.manicule.feature.home.R as HomeR
import com.leeseungyun1020.manicule.feature.scanner.R as ScannerR
import com.leeseungyun1020.manicule.feature.stats.R as StatsR

@HiltAndroidTest
class HomeNavigationTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var library: NavigationLibrary

    @Before
    fun setUp() {
        hilt.inject()
        library.entries.value = emptyList()
    }

    @Test
    fun homeTopBarScan_navigatesToScannerAndBackRestoresHome() {
        val scanDescription = compose.activity.getString(HomeR.string.home_scan)
        compose.onNodeWithContentDescription(scanDescription).performClick()

        val scannerTitle = compose.activity.getString(ScannerR.string.scanner_title)
        compose.onNodeWithText(scannerTitle).assertIsDisplayed()

        pressBack()

        val searchPlaceholder = compose.activity.getString(HomeR.string.home_search_placeholder)
        compose.onNodeWithText(searchPlaceholder).assertIsDisplayed()
        compose.onNodeWithContentDescription(scanDescription).assertIsDisplayed()
    }

    @Test
    fun homeOnboardingScan_navigatesToScannerAndBackRestoresHome() {
        val scanText = compose.activity.getString(HomeR.string.home_scan)
        compose.onNodeWithText(scanText).performClick()

        val scannerTitle = compose.activity.getString(ScannerR.string.scanner_title)
        compose.onNodeWithText(scannerTitle).assertIsDisplayed()

        pressBack()

        val onboardingHeading = compose.activity.getString(HomeR.string.home_onboarding_heading)
        compose.onNodeWithText(onboardingHeading).assertIsDisplayed()
    }

    @Test
    fun homeNoReadingBooksWithoutWant_scan_navigatesToScannerAndBackRestoresHome() {
        library.entries.value = listOf(testBookEntry(ReadingStatus.FINISHED))

        val scanText = compose.activity.getString(HomeR.string.home_scan)
        compose.onNodeWithText(scanText).performClick()

        val scannerTitle = compose.activity.getString(ScannerR.string.scanner_title)
        compose.onNodeWithText(scannerTitle).assertIsDisplayed()

        pressBack()

        val noReadingTitle = compose.activity.getString(HomeR.string.home_no_reading_title)
        compose.onNodeWithText(noReadingTitle).assertIsDisplayed()
    }

    @Test
    fun homeReadingSummary_navigatesToStatsAndSelectsStatsTab() {
        library.entries.value = listOf(testBookEntry(ReadingStatus.READING))

        val streakText = compose.activity.getString(HomeR.string.home_streak)
        compose.onNodeWithText(streakText).performClick()

        val statsTitle = compose.activity.getString(StatsR.string.stats_title)
        compose.onNodeWithText(statsTitle).assertIsDisplayed()

        val statsTabText = compose.activity.getString(R.string.tab_stats)
        compose.onNodeWithText(statsTabText).assertIsSelected()
    }

    private fun testBookEntry(status: ReadingStatus): BookEntry =
        BookEntry(
            book =
                Book(
                    isbn = "9780000000001",
                    title = "Test Book",
                    author = "Author",
                    publisher = "Publisher",
                    publishedDate = null,
                    coverUrl = null,
                    totalPages = 200,
                    price = null,
                    category = null,
                    tableOfContentsUrl = null,
                    introductionUrl = null,
                    summaryUrl = null,
                ),
            status = status,
            addedAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
            currentPage = if (status == ReadingStatus.READING) 20 else null,
        )
}
