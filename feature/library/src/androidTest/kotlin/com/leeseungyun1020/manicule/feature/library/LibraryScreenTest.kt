package com.leeseungyun1020.manicule.feature.library

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
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
    fun content_showsThreeTabsAndSelectsBook() {
        var selectedIsbn: String? = null
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Content(ReadingStatus.READING, listOf(entry())),
                    onStatusSelected = {},
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
    fun content_placesThreeBooksOnFirstRow() {
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
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = {},
                )
            }
        }

        val firstRowTop = composeRule.onNodeWithText("첫 번째 책").getUnclippedBoundsInRoot().top
        assertThat(composeRule.onNodeWithText("두 번째 책").getUnclippedBoundsInRoot().top).isEqualTo(firstRowTop)
        assertThat(composeRule.onNodeWithText("세 번째 책").getUnclippedBoundsInRoot().top).isEqualTo(firstRowTop)
        assertThat(composeRule.onNodeWithText("네 번째 책").getUnclippedBoundsInRoot().top).isGreaterThan(firstRowTop)
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
    fun errorState_callsRetry() {
        var retried = false
        composeRule.setContent {
            ManiculeTheme {
                LibraryScreen(
                    uiState = LibraryUiState.Error(ReadingStatus.READING),
                    onStatusSelected = {},
                    onBookSelected = {},
                    onSearch = {},
                    onScan = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.library_error_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.library_retry)).performClick()
        composeRule.runOnIdle { assertThat(retried).isTrue() }
    }

    private fun entry(
        isbn: String = "9780000000001",
        title: String = "테스트 책",
    ) = BookEntry(
        book =
            Book(
                isbn = isbn,
                title = title,
                author = "작가",
                publisher = "출판사",
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
        addedAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )
}
