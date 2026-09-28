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
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.notifications.ReminderNotificationPublisher
import com.leeseungyun1020.manicule.core.notifications.di.NotificationsModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import com.leeseungyun1020.manicule.feature.home.R as HomeR
import com.leeseungyun1020.manicule.feature.library.R as LibraryR
import com.leeseungyun1020.manicule.feature.scanner.R as ScannerR
import com.leeseungyun1020.manicule.feature.settings.R as SettingsR
import com.leeseungyun1020.manicule.feature.stats.R as StatsR

@HiltAndroidTest
@UninstallModules(NotificationsModule::class)
class HomeNavigationTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var library: NavigationLibrary

    @BindValue
    @JvmField
    val reminderScheduler: ReminderScheduler =
        object : ReminderScheduler {
            override suspend fun schedule(time: LocalTime) = Unit

            override suspend fun scheduleNext(time: LocalTime) = Unit

            override suspend fun cancel() = Unit
        }

    @BindValue
    @JvmField
    val notificationPublisher: ReminderNotificationPublisher =
        object : ReminderNotificationPublisher {
            override fun publish(books: List<ReminderContent.Book>) = Unit
        }

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

    @Test
    fun bottomNav_fromLibraryStatsSettingsToHome_restoresHomeAndSelectsHomeTab() {
        val homeSearch = compose.activity.getString(HomeR.string.home_search_placeholder)
        val homeTab = compose.activity.getString(R.string.tab_home)
        val libraryTab = compose.activity.getString(R.string.tab_library)
        val statsTab = compose.activity.getString(R.string.tab_stats)
        val settingsTab = compose.activity.getString(R.string.tab_settings)
        val libraryTitle = compose.activity.getString(LibraryR.string.library_title)
        val statsTitle = compose.activity.getString(StatsR.string.stats_title)
        val settingsTitle = compose.activity.getString(SettingsR.string.settings_title)

        compose.onNodeWithText(homeSearch).assertIsDisplayed()
        onBottomTab(homeTab).assertIsSelected()

        // 서재로 이동 -> 홈으로 복귀
        onBottomTab(libraryTab).performClick()
        compose.onNode(androidx.compose.ui.test.hasText(libraryTitle) and !androidx.compose.ui.test.isSelectable()).assertIsDisplayed()
        onBottomTab(libraryTab).assertIsSelected()

        onBottomTab(homeTab).performClick()
        compose.onNodeWithText(homeSearch).assertIsDisplayed()
        onBottomTab(homeTab).assertIsSelected()

        // 통계로 이동 -> 홈으로 복귀
        onBottomTab(statsTab).performClick()
        compose.onNodeWithText(statsTitle).assertIsDisplayed()
        onBottomTab(statsTab).assertIsSelected()

        onBottomTab(homeTab).performClick()
        compose.onNodeWithText(homeSearch).assertIsDisplayed()
        onBottomTab(homeTab).assertIsSelected()

        // 설정으로 이동 -> 홈으로 복귀
        onBottomTab(settingsTab).performClick()
        compose.onNode(androidx.compose.ui.test.hasText(settingsTitle) and !androidx.compose.ui.test.isSelectable()).assertIsDisplayed()
        onBottomTab(settingsTab).assertIsSelected()

        onBottomTab(homeTab).performClick()
        compose.onNodeWithText(homeSearch).assertIsDisplayed()
        onBottomTab(homeTab).assertIsSelected()

        // 홈 탭 재선택 시 중복 생성 없이 여전히 홈 선택 상태
        onBottomTab(homeTab).performClick()
        compose.onNodeWithText(homeSearch).assertIsDisplayed()
        onBottomTab(homeTab).assertIsSelected()
    }

    @Test
    fun homeMoreReadingBooks_navigatesToLibraryReadingTabAndSelectsLibraryTab() {
        library.entries.value = listOf(testBookEntry(ReadingStatus.READING))

        val moreText = compose.activity.getString(HomeR.string.home_more)
        compose.onNode(
            androidx.compose.ui.test.hasText(moreText) and
                androidx.compose.ui.test.SemanticsMatcher.expectValue(
                    androidx.compose.ui.semantics.SemanticsProperties.Role,
                    androidx.compose.ui.semantics.Role.Button,
                ),
        ).performClick()

        val readingTabText = compose.activity.getString(LibraryR.string.library_tab_reading)
        compose.onNodeWithText(readingTabText).assertIsSelected()

        val libraryTabText = compose.activity.getString(R.string.tab_library)
        onBottomTab(libraryTabText).assertIsSelected()

        pressBack()
        val homeTabText = compose.activity.getString(R.string.tab_home)
        onBottomTab(homeTabText).assertIsSelected()
    }

    @Test
    fun homeChooseWantBooks_navigatesToLibraryWantTabAndSelectsLibraryTab() {
        library.entries.value = listOf(testBookEntry(ReadingStatus.WANT))

        val chooseText = compose.activity.getString(HomeR.string.home_choose)
        compose.onNodeWithText(chooseText).performClick()

        val wantTabText = compose.activity.getString(LibraryR.string.library_tab_want)
        compose.onNodeWithText(wantTabText).assertIsSelected()

        val libraryTabText = compose.activity.getString(R.string.tab_library)
        onBottomTab(libraryTabText).assertIsSelected()

        pressBack()
        val homeTabText = compose.activity.getString(R.string.tab_home)
        onBottomTab(homeTabText).assertIsSelected()
    }

    @Test
    fun bottomNav_libraryTabRestoresSavedTabState_whileHomeDirectNavigationOverrides() {
        library.entries.value =
            listOf(
                testBookEntry(ReadingStatus.READING),
                testBookEntry(ReadingStatus.WANT, isbn = "9780000000002"),
            )

        val homeTabText = compose.activity.getString(R.string.tab_home)
        val libraryTabText = compose.activity.getString(R.string.tab_library)
        val finishedTabText = compose.activity.getString(LibraryR.string.library_tab_finished)
        val readingTabText = compose.activity.getString(LibraryR.string.library_tab_reading)
        val moreText = compose.activity.getString(HomeR.string.home_more)

        // 1. 하단 서재 탭 이동 후 '다 읽음' 탭 선택
        onBottomTab(libraryTabText).performClick()
        compose.onNodeWithText(finishedTabText).performClick()
        compose.onNodeWithText(finishedTabText).assertIsSelected()

        // 2. 홈 탭으로 복귀
        onBottomTab(homeTabText).performClick()
        onBottomTab(homeTabText).assertIsSelected()

        // 3. 하단 서재 탭 다시 이동 시 이전 상태 '다 읽음'이 복원되는지 확인 (restoreState = true)
        onBottomTab(libraryTabText).performClick()
        compose.onNodeWithText(finishedTabText).assertIsSelected()

        // 4. 다시 홈으로 복귀
        onBottomTab(homeTabText).performClick()

        // 5. 홈에서 '더보기'로 서재 진입 시 저장된 상태가 아닌 '읽는 중' 탭이 열리는지 확인 (restoreState = false)
        compose.onNode(
            androidx.compose.ui.test.hasText(moreText) and
                androidx.compose.ui.test.SemanticsMatcher.expectValue(
                    androidx.compose.ui.semantics.SemanticsProperties.Role,
                    androidx.compose.ui.semantics.Role.Button,
                ),
        ).performClick()
        compose.onNodeWithText(readingTabText).assertIsSelected()
        onBottomTab(libraryTabText).assertIsSelected()
    }

    private fun onBottomTab(label: String) =
        compose.onNode(
            androidx.compose.ui.test.hasText(label) and androidx.compose.ui.test.isSelectable(),
        )

    private fun testBookEntry(
        status: ReadingStatus,
        isbn: String = "9780000000001",
    ): BookEntry =
        BookEntry(
            book =
                Book(
                    isbn = isbn,
                    title = "Test Book $isbn",
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
