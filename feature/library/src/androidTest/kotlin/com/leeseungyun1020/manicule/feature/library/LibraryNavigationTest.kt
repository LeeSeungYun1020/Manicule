package com.leeseungyun1020.manicule.feature.library

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryRoute as LibraryDestination

@RunWith(AndroidJUnit4::class)
class LibraryNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var navController: NavHostController

    @Test
    fun defaultRoute_showsReadingTabAndBooks() {
        setContent()
        composeRule.runOnIdle { navController.navigate(LibraryDestination()) }

        assertSelectedTab(LibraryTab.READING)
        composeRule.onNodeWithText("READING book").assertIsDisplayed()
    }

    @Test
    fun wantRoute_showsWantTabAndAllowsSelectingAnotherTab() {
        setContent()
        composeRule.runOnIdle { navController.navigate(LibraryDestination(LibraryTab.WANT)) }

        assertSelectedTab(LibraryTab.WANT)
        composeRule.onNodeWithText("WANT book").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.library_tab_finished)).performClick()
        assertSelectedTab(LibraryTab.FINISHED)
        composeRule.onNodeWithText("FINISHED book").assertIsDisplayed()
    }

    @Test
    fun restoredLibraryEntry_keepsUserSelectedTab() {
        setContent()
        composeRule.runOnIdle { navController.navigate(LibraryDestination()) }
        composeRule.onNodeWithText(context.getString(R.string.library_tab_finished)).performClick()

        composeRule.runOnIdle {
            navController.navigate(LibraryTestHomeRoute) {
                popUpTo<LibraryTestHomeRoute> { saveState = true }
                launchSingleTop = true
            }
        }
        composeRule.runOnIdle {
            navController.navigate(LibraryDestination()) {
                restoreState = true
                launchSingleTop = true
            }
        }

        assertSelectedTab(LibraryTab.FINISHED)
        composeRule.onNodeWithText("FINISHED book").assertIsDisplayed()
    }

    @Test
    fun newWantEntry_usesRequestedTabAfterPreviousEntryWasRemoved() {
        setContent()
        composeRule.runOnIdle { navController.navigate(LibraryDestination()) }
        composeRule.onNodeWithText(context.getString(R.string.library_tab_finished)).performClick()
        composeRule.runOnIdle { navController.popBackStack() }
        composeRule.runOnIdle { navController.navigate(LibraryDestination(LibraryTab.WANT)) }

        assertSelectedTab(LibraryTab.WANT)
        composeRule.runOnIdle {
            val entry = checkNotNull(navController.currentBackStackEntry)
            assertThat(entry.destination.hasRoute<LibraryDestination>()).isTrue()
            assertThat(entry.toRoute<LibraryDestination>()).isEqualTo(LibraryDestination(LibraryTab.WANT))
        }
    }

    private fun setContent() {
        val getLibraryBooks = GetLibraryBooksUseCase(NavigationLibraryRepository())
        composeRule.setContent {
            navController = rememberNavController()
            ManiculeTheme {
                NavHost(navController = navController, startDestination = LibraryTestHomeRoute) {
                    composable<LibraryTestHomeRoute> { Text("Home") }
                    composable<LibraryDestination> {
                        LibraryRoute(
                            onNavigateToBookDetail = {},
                            onNavigateToSearch = {},
                            onNavigateToScanner = {},
                            viewModel =
                                viewModel(
                                    factory =
                                        viewModelFactory {
                                            initializer {
                                                LibraryViewModel(getLibraryBooks, createSavedStateHandle())
                                            }
                                        },
                                ),
                        )
                    }
                }
            }
        }
    }

    private fun assertSelectedTab(tab: LibraryTab) {
        val label =
            when (tab) {
                LibraryTab.WANT -> R.string.library_tab_want
                LibraryTab.READING -> R.string.library_tab_reading
                LibraryTab.FINISHED -> R.string.library_tab_finished
            }
        composeRule.onNodeWithText(context.getString(label)).assertIsSelected()
    }
}

@Serializable
private object LibraryTestHomeRoute

private class NavigationLibraryRepository : LibraryRepository {
    override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> = flowOf(listOf(entry(status)))

    override fun observeAll(): Flow<List<BookEntry>> = error("Not used")

    override fun observeBookEntry(isbn: String): Flow<BookEntry?> = error("Not used")

    override suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book> = error("Not used")

    override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = error("Not used")

    override suspend fun removeBookEntry(isbn: String): Unit = error("Not used")

    private fun entry(status: ReadingStatus) =
        BookEntry(
            book =
                Book(
                    isbn = status.name,
                    title = "${status.name} book",
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
            status = status,
            addedAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
}
