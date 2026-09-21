package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookDetail
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.bookdetail.components.BookDetailExpandableText
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import com.leeseungyun1020.manicule.core.designsystem.R as DesignSystemR

class BookDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun informationTab_displaysBibliographicFields_andChangesTab() {
        var selectedTab: BookDetailTab? = null
        var navigatedBack = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = contentState(),
                    onNavigateBack = { navigatedBack = true },
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = { selectedTab = it },
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Author").assertIsDisplayed()
        composeRule.onNodeWithText("Publisher").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_tab_my_records)).performClick()
        composeRule.onNodeWithContentDescription(context.getString(DesignSystemR.string.core_designsystem_back)).performClick()

        assertThat(selectedTab).isEqualTo(BookDetailTab.MyRecords)
        assertThat(navigatedBack).isTrue()
    }

    @Test
    fun fatalError_displaysRetry() {
        var retried = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = BookDetailUiState.Error,
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_retry)).performClick()

        assertThat(retried).isTrue()
    }

    @Test
    fun refreshFailure_keepsContent_andRetryDismissesSnackbar() {
        var retried = false
        var uiState by mutableStateOf(contentState(refreshStatus = RefreshStatus.Failed))
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {
                        retried = true
                        uiState = uiState.copy(refreshStatus = RefreshStatus.Refreshing)
                    },
                )
            }
        }

        val refreshError = context.getString(R.string.book_detail_refresh_error)
        composeRule.onNodeWithText("Author").assertIsDisplayed()
        composeRule.onNodeWithText(refreshError).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_retry)).performClick()

        assertThat(retried).isTrue()
        composeRule.onAllNodesWithText(refreshError).assertCountEquals(0)
        composeRule.onNodeWithText("Author").assertIsDisplayed()
    }

    @Test
    fun expandableText_preservesExpandedStateAfterRestoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ManiculeTheme {
                BookDetailExpandableText(
                    title = "Introduction",
                    text = "Long content ".repeat(100),
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_expand)).performClick()
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(context.getString(R.string.book_detail_collapse)).assertIsDisplayed()
    }

    @Test
    fun unregisteredAndReviewOnly_showNoSelection_andRegisteredStatesMatchDatabase() {
        var uiState by mutableStateOf(recordsState(null))
        var requested: ReadingStatus? = null
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(uiState, {}, {}, {}, { requested = it }, {})
            }
        }
        statusLabels.forEach { composeRule.onNodeWithText(context.getString(it)).assertIsNotSelected() }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_want)).performClick()
        assertThat(requested).isEqualTo(ReadingStatus.WANT)
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_want)).assertIsNotSelected()
        composeRule.runOnIdle { uiState = recordsState(ReadingStatus.UNSET) }
        statusLabels.forEach { composeRule.onNodeWithText(context.getString(it)).assertIsNotSelected() }
        listOf(ReadingStatus.WANT, ReadingStatus.READING, ReadingStatus.FINISHED).forEachIndexed { index, status ->
            composeRule.runOnIdle { uiState = recordsState(status) }
            statusLabels.forEachIndexed { labelIndex, label ->
                val node = composeRule.onNodeWithText(context.getString(label))
                if (index == labelIndex) node.assertIsSelected() else node.assertIsNotSelected()
            }
        }
    }

    @Test
    fun saving_disablesEveryStatus_andKeepsPersistedSelection() {
        var requested = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    recordsState(ReadingStatus.READING).copy(statusChange = StatusChangeState.Saving(ReadingStatus.FINISHED)),
                    {},
                    {},
                    {},
                    { requested = true },
                    {},
                )
            }
        }
        statusLabels.forEach {
            composeRule.onNodeWithText(context.getString(it)).assertIsNotEnabled().performClick()
        }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_reading)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_saving)).assertIsDisplayed()
        assertThat(requested).isFalse()
    }

    @Test
    fun saveError_takesPriorityOverRefreshError_andRetriesRequestedStatus() {
        var uiState by mutableStateOf(recordsState(ReadingStatus.READING).copy(refreshStatus = RefreshStatus.Failed))
        var retriedStatus: ReadingStatus? = null
        var refreshed = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onTabSelected = {},
                    onRetry = { refreshed = true },
                    onStatusSelected = {
                        retriedStatus = it
                        uiState = uiState.copy(statusChange = StatusChangeState.Saving(it))
                    },
                    onStatusErrorDismissed = {},
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_refresh_error)).assertIsDisplayed()
        composeRule.runOnIdle { uiState = uiState.copy(statusChange = StatusChangeState.Failed(ReadingStatus.FINISHED, 1)) }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_error)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_reading)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_retry)).performClick()
        assertThat(retriedStatus).isEqualTo(ReadingStatus.FINISHED)
        assertThat(refreshed).isFalse()
        composeRule.onAllNodesWithText(context.getString(R.string.book_detail_status_error)).assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_saving)).assertIsDisplayed()
    }

    @Test
    fun largeFont_keepsSegmentHeightsAligned_whenLabelWraps() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                ManiculeTheme {
                    BookDetailScreen(recordsState(null), {}, {}, {}, {}, {})
                }
            }
        }
        val heights = statusLabels.map {
            composeRule.onNodeWithText(context.getString(it)).assertIsDisplayed().fetchSemanticsNode().boundsInRoot.height
        }
        heights.forEach { assertThat(it).isWithin(1f).of(heights.first()) }
    }

    @Test
    fun myRecordsTab_emptyRecords_displaysEmptyStateAndOpensBottomSheet() {
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = recordsState(ReadingStatus.READING, emptyList()),
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_records_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_title)).assertIsDisplayed()
    }

    @Test
    fun addRecordBottomSheet_enterPages_andSaves() {
        var savedStart = 0
        var savedEnd = 0
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = recordsState(ReadingStatus.READING, emptyList()),
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                    onAddRecord = { _, _, start, end ->
                        savedStart = start
                        savedEnd = end
                    },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_end_page)).performTextInput("25")
        composeRule.onAllNodesWithText(context.getString(R.string.book_detail_add_record_button))[1].performClick()

        assertThat(savedStart).isEqualTo(1)
        assertThat(savedEnd).isEqualTo(25)
    }

    @Test
    fun addRecordBottomSheet_preservesDraftAfterStateRestoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = recordsState(ReadingStatus.READING, emptyList()),
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_date_yesterday)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_end_page)).performTextInput("25")
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_date_yesterday)).assertIsSelected()
        composeRule.onNodeWithText("25").assertIsDisplayed()
    }

    @Test
    fun addRecordBottomSheet_formIsVerticallyScrollable() {
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = recordsState(ReadingStatus.READING, emptyList()),
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()

        composeRule.onNode(
            hasScrollAction() and
                hasAnyDescendant(hasText(context.getString(R.string.book_detail_add_record_title))),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun addRecordBottomSheet_staysOpenWhileSaving_andClosesAfterSuccess() {
        var uiState by mutableStateOf(recordsState(ReadingStatus.READING, emptyList()))
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                    onAddRecord = { _, _, _, _ ->
                        uiState = uiState.copy(recordSaving = RecordSavingState.Saving(1L))
                    },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_end_page)).performTextInput("25")
        composeRule.onAllNodesWithText(context.getString(R.string.book_detail_add_record_button))[1].performClick()

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_title)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.book_detail_add_record_button))[1].assertIsNotEnabled()

        composeRule.runOnIdle { uiState = uiState.copy(recordSaving = RecordSavingState.Succeeded(1L)) }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_title)).assertDoesNotExist()
    }

    @Test
    fun addRecordBottomSheet_restoredIdleDoesNotDiscardInFlightDraft() {
        var uiState by mutableStateOf(recordsState(ReadingStatus.READING, emptyList()))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                    onAddRecord = { _, _, _, _ ->
                        uiState = uiState.copy(recordSaving = RecordSavingState.Saving(1L))
                    },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_end_page)).performTextInput("25")
        composeRule.onAllNodesWithText(context.getString(R.string.book_detail_add_record_button))[1].performClick()
        composeRule.runOnIdle { uiState = uiState.copy(recordSaving = RecordSavingState.Idle) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_title)).assertIsDisplayed()
        composeRule.onNodeWithText("25").assertIsDisplayed()
    }

    @Test
    fun addRecordBottomSheet_failureKeepsDraftForRetry() {
        var uiState by mutableStateOf(recordsState(ReadingStatus.READING, emptyList()))
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                    onAddRecord = { _, _, _, _ ->
                        uiState = uiState.copy(recordSaving = RecordSavingState.Saving(1L))
                    },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_end_page)).performTextInput("25")
        composeRule.onAllNodesWithText(context.getString(R.string.book_detail_add_record_button))[1].performClick()
        composeRule.runOnIdle { uiState = uiState.copy(recordSaving = RecordSavingState.Failed(1L)) }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_title)).assertIsDisplayed()
        composeRule.onNodeWithText("25").assertIsDisplayed()
    }

    @Test
    fun recordSavingFailed_showsSnackbar() {
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = recordsState(ReadingStatus.READING).copy(recordSaving = RecordSavingState.Failed(1L)),
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_record_save_error)).assertIsDisplayed()
    }

    @Test
    fun myRecordsTab_withRecords_displaysProgressBarAndGroupedSessions() {
        val record = ReadingRecord(1L, "123", LocalDate(2026, 9, 19), LocalTime(14, 0), 1, 30)
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = recordsState(ReadingStatus.READING, listOf(record)),
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("30 / 300쪽").assertIsDisplayed()
        composeRule.onNodeWithText("9월 19일").assertIsDisplayed()
        composeRule.onNodeWithText("30p").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.book_detail_edit_record))
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(context.getString(R.string.book_detail_delete_record))
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_button)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_add_record_title)).assertIsDisplayed()
    }

    @Test
    fun myRecordsTab_longHistory_onlyComposesVisibleSessions() {
        val records =
            (1L..100L).map { id ->
                ReadingRecord(id, "123", LocalDate(2026, 9, 19), LocalTime(14, 0), id.toInt(), id.toInt())
            }
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = recordsState(ReadingStatus.READING, records),
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("오후 2:00 · 1–1쪽").assertIsDisplayed()
        composeRule.onNodeWithText("오후 2:00 · 100–100쪽").assertDoesNotExist()
    }

    @Composable
    private fun BookDetailScreen(
        uiState: BookDetailUiState,
        onNavigateBack: () -> Unit,
        onTabSelected: (BookDetailTab) -> Unit,
        onRetry: () -> Unit,
        onStatusSelected: (ReadingStatus) -> Unit,
        onStatusErrorDismissed: () -> Unit,
        onAddRecord: (LocalDate, LocalTime, Int, Int) -> Unit = { _, _, _, _ -> },
        onRecordErrorDismissed: () -> Unit = {},
    ) {
        com.leeseungyun1020.manicule.feature.bookdetail.BookDetailScreen(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onTabSelected = onTabSelected,
            onRetry = onRetry,
            onStatusSelected = onStatusSelected,
            onStatusErrorDismissed = onStatusErrorDismissed,
            onAddRecord = onAddRecord,
            onRecordErrorDismissed = onRecordErrorDismissed,
        )
    }

    private companion object {
        val statusLabels =
            listOf(R.string.book_detail_status_want, R.string.book_detail_status_reading, R.string.book_detail_status_finished)

        fun recordsState(
            status: ReadingStatus?,
            records: List<ReadingRecord> = emptyList(),
        ) = contentState().copy(
            selectedTab = BookDetailTab.MyRecords,
            records = records,
            bookDetail = BookDetail(
                testBook,
                status?.let {
                    BookEntry(
                        testBook,
                        it,
                        rating = 4,
                        memo = "Keep review",
                        addedAt = Instant.fromEpochMilliseconds(1),
                        updatedAt = Instant.fromEpochMilliseconds(1),
                    )
                },
            ),
        )

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val testBook =
            Book(
                isbn = "123",
                title = "Test Book",
                author = "Author",
                publisher = "Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = 300,
                price = 20_000,
                category = "Category",
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
                introduction = "Introduction",
                tableOfContents = "Contents",
            )

        fun contentState(refreshStatus: RefreshStatus = RefreshStatus.Idle) =
            BookDetailUiState.Content(
                bookDetail = BookDetail(testBook, entry = null),
                selectedTab = BookDetailTab.Information,
                refreshStatus = refreshStatus,
            )
    }
}
