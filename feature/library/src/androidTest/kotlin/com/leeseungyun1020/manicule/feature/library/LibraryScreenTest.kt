package com.leeseungyun1020.manicule.feature.library

import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun longPress_opensOnlyOtherStatusesAndDelete() {
        var changedTo: ReadingStatus? = null
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, listOf(entry())),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                    onChangeStatus = { _, status -> changedTo = status },
                )
            }
        }

        composeRule.onNodeWithText("테스트 책").performSemanticsAction(SemanticsActions.OnLongClick)
        composeRule.onNodeWithText(context.getString(R.string.library_action_move_want)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.library_action_move_finished)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.library_action_move_reading)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.library_action_delete)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.library_action_move_finished)).performClick()
        composeRule.runOnIdle { assertThat(changedTo).isEqualTo(ReadingStatus.FINISHED) }
    }

    @Test
    fun content_showsThreeTabsAndSelectsBook() {
        var selectedIsbn: String? = null
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, listOf(entry())),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = { selectedIsbn = it },
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.library_tab_want)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.library_tab_reading)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.library_tab_finished)).assertIsDisplayed()
        composeRule.onNodeWithText("테스트 책").performClick()
        composeRule.runOnIdle { assertThat(selectedIsbn).isEqualTo("9780000000001") }
    }

    @Test
    fun tabClick_reportsSelectedStatus() {
        var selectedStatus: ReadingStatus? = null
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, listOf(entry())),
                    onStatusSelected = { selectedStatus = it },
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.library_tab_finished)).performClick()
        composeRule.runOnIdle { assertThat(selectedStatus).isEqualTo(ReadingStatus.FINISHED) }
    }

    @Test
    fun emptyState_callsSearchAndScan() {
        var searched = false
        var scanned = false
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.WANT, emptyList()),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = { searched = true },
                    onScan = { scanned = true },
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag("library_empty_icon", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("library_search_icon", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("library_scan_icon", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.library_search)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.library_scan)).performClick()
        composeRule.runOnIdle {
            assertThat(searched).isTrue()
            assertThat(scanned).isTrue()
        }
    }

    @Test
    fun content_placesThreeBooksOnFirstRowAtPhoneWidth() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.WANT,
                            listOf(
                                entry(isbn = "9780000000001", title = "첫 번째 책"),
                                entry(isbn = "9780000000002", title = "두 번째 책"),
                                entry(isbn = "9780000000003", title = "세 번째 책"),
                                entry(isbn = "9780000000004", title = "네 번째 책"),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                    modifier = Modifier.width(360.dp),
                )
            }
        }

        val firstRowTop = composeRule.onNodeWithText("첫 번째 책").getUnclippedBoundsInRoot().top
        assertThat(composeRule.onNodeWithText("두 번째 책").getUnclippedBoundsInRoot().top).isEqualTo(firstRowTop)
        assertThat(composeRule.onNodeWithText("세 번째 책").getUnclippedBoundsInRoot().top).isEqualTo(firstRowTop)
        assertThat(composeRule.onNodeWithText("네 번째 책").getUnclippedBoundsInRoot().top).isGreaterThan(firstRowTop)
    }

    @Test
    fun content_placesAtLeastFourBooksOnFirstRowAtExpandedWidth() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.WANT,
                            listOf(
                                entry(isbn = "9780000000001", title = "첫 번째 책"),
                                entry(isbn = "9780000000002", title = "두 번째 책"),
                                entry(isbn = "9780000000003", title = "세 번째 책"),
                                entry(isbn = "9780000000004", title = "네 번째 책"),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                    modifier = Modifier.requiredWidth(600.dp),
                )
            }
        }

        val firstRowTop = composeRule.onNodeWithText("첫 번째 책").getUnclippedBoundsInRoot().top
        assertThat(composeRule.onNodeWithText("네 번째 책").getUnclippedBoundsInRoot().top).isEqualTo(firstRowTop)
    }

    @Test
    fun addButton_isDisplayedOnlyWhenBooksAreShownAndCallsSearch() {
        var searched = false
        lateinit var updateUiState: (LibraryUiState) -> Unit
        composeRule.setContent {
            val uiState = remember { mutableStateOf<LibraryUiState>(LibraryUiState.Loading(ReadingStatus.READING)) }
            updateUiState = { uiState.value = it }
            ManiculeTheme {
                LibraryScreen(
                    uiState = uiState.value,
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = { searched = true },
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        val addButton =
            composeRule.onNodeWithContentDescription(context.getString(R.string.library_add_book))
        addButton.assertIsNotDisplayed()

        composeRule.runOnIdle { updateUiState(LibraryUiState.Error(ReadingStatus.READING)) }
        addButton.assertIsNotDisplayed()

        composeRule.runOnIdle {
            updateUiState(LibraryUiState.Content(ReadingStatus.WANT, emptyList()))
        }
        addButton.assertIsNotDisplayed()

        composeRule.runOnIdle {
            updateUiState(LibraryUiState.Content(ReadingStatus.READING, listOf(entry())))
        }
        addButton.assertIsDisplayed()
        addButton.performClick()
        composeRule.runOnIdle { assertThat(searched).isTrue() }
    }

    @Test
    fun content_showsCurrentSortCaption() {
        val sort =
            LibrarySort(
                criterion = LibrarySort.Criterion.ADDED_AT,
                direction = LibrarySort.Direction.ASCENDING,
            )
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, listOf(entry()), sort),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(
            context.getString(
                R.string.library_sort_caption,
                context.getString(R.string.library_sort_added_at),
                context.getString(R.string.library_sort_oldest),
            ),
        ).assertIsDisplayed()
    }

    @Test
    fun sortSheet_appliesDraftOnlyAfterApply() {
        var selectedSort: LibrarySort? = null
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, listOf(entry())),
                    onStatusSelected = {},
                    onSortSelected = { selectedSort = it },
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.library_sort_title))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.library_sort_rating)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.library_sort_rating)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.library_sort_lowest)).performClick()
        composeRule.runOnIdle { assertThat(selectedSort).isNull() }

        composeRule.onNodeWithText(context.getString(R.string.library_sort_apply)).performClick()

        composeRule.runOnIdle {
            assertThat(selectedSort)
                .isEqualTo(
                    LibrarySort(
                        criterion = LibrarySort.Criterion.RATING,
                        direction = LibrarySort.Direction.ASCENDING,
                    ),
                )
        }
    }

    @Test
    fun sortSheet_cancelKeepsAppliedSort() {
        var selectedSort: LibrarySort? = null
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, listOf(entry())),
                    onStatusSelected = {},
                    onSortSelected = { selectedSort = it },
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.library_sort_title))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.library_sort_added_at)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.library_sort_cancel)).performClick()

        composeRule.runOnIdle { assertThat(selectedSort).isNull() }
        composeRule.onNodeWithText(context.getString(R.string.library_sort_cancel)).assertDoesNotExist()
    }

    @Test
    fun emptyState_hidesSortAction() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, emptyList()),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.library_sort_title))
            .assertDoesNotExist()
    }

    @Test
    fun readingTab_showsProgressPercentageAndBookmarkRibbon() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.READING,
                            listOf(
                                entry(
                                    status = ReadingStatus.READING,
                                    totalPages = 300,
                                    currentPage = 150,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.library_book_progress, 50)).assertIsDisplayed()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun readingTab_whenZeroProgress_showsZeroPercentAndNoBookmark() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.READING,
                            listOf(
                                entry(
                                    status = ReadingStatus.READING,
                                    totalPages = 300,
                                    currentPage = 0,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.library_book_progress, 0)).assertIsDisplayed()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun readingTab_whenTotalPagesNull_doesNotShowOverlay() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.READING,
                            listOf(
                                entry(
                                    status = ReadingStatus.READING,
                                    totalPages = null,
                                    currentPage = 150,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag("book_cover_status_overlay", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun readingTab_whenTotalPagesNotPositive_doesNotShowOverlay() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.READING,
                            listOf(
                                entry(
                                    isbn = "9780000000001",
                                    totalPages = 0,
                                    currentPage = 1,
                                ),
                                entry(
                                    isbn = "9780000000002",
                                    totalPages = -1,
                                    currentPage = 1,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag("book_cover_status_overlay", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun readingTab_whenCurrentPageNull_showsZeroPercentAndNoBookmark() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.READING,
                            listOf(
                                entry(
                                    totalPages = 300,
                                    currentPage = null,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.library_book_progress, 0)).assertIsDisplayed()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun readingTab_whenCurrentPageExceedsTotalPages_clampsToHundredPercent() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.READING,
                            listOf(
                                entry(
                                    totalPages = 300,
                                    currentPage = 400,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.library_book_progress, 100)).assertIsDisplayed()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun finishedTab_showsFinishedDateOverlay() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.FINISHED,
                            listOf(
                                entry(
                                    status = ReadingStatus.FINISHED,
                                    finishedAt = kotlinx.datetime.LocalDate(2026, 7, 8),
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.library_book_finished_date, 2026, 7, 8))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun finishedTab_whenFinishedAtNull_doesNotShowOverlay() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.FINISHED,
                            listOf(
                                entry(
                                    status = ReadingStatus.FINISHED,
                                    finishedAt = null,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag("book_cover_status_overlay", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun wantTab_doesNotShowOverlay() {
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState =
                        LibraryUiState.Content(
                            ReadingStatus.WANT,
                            listOf(
                                entry(
                                    status = ReadingStatus.WANT,
                                    totalPages = 300,
                                    currentPage = 150,
                                ),
                            ),
                        ),
                    onStatusSelected = {},
                    onSortSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag("book_cover_status_overlay", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag("bookmark_ribbon", useUnmergedTree = true).assertDoesNotExist()
    }

    private fun entry(
        isbn: String = "9780000000001",
        title: String = "테스트 책",
        status: ReadingStatus = ReadingStatus.READING,
        totalPages: Int? = null,
        currentPage: Int? = null,
        finishedAt: kotlinx.datetime.LocalDate? = null,
    ) = BookEntry(
        book =
            Book(
                isbn = isbn,
                title = title,
                author = "작가",
                publisher = "출판사",
                publishedDate = null,
                coverUrl = null,
                totalPages = totalPages,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            ),
        status = status,
        addedAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
        currentPage = currentPage,
        finishedAt = finishedAt,
    )
}
