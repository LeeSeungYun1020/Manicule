package com.leeseungyun1020.manicule.feature.search

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.ui.preview.BookPreviewParameterProvider
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SearchResultAppearanceTest(
    private val darkTheme: Boolean,
    private val fontScale: Float,
) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun compactResults_keepBookTextVisible() {
        showResults(empty = false)
        compose.onNodeWithText("Search results").assertIsDisplayed()
        compose.onNodeWithText(BookPreviewParameterProvider().values.first().title).assertIsDisplayed()
        capture("results")
    }

    @Test
    fun compactEmptyResult_keepsScannerVisible() {
        showResults(empty = true)
        compose.onNodeWithText("No search results").assertIsDisplayed()
        compose.onNodeWithText("Scan").assertIsDisplayed().assertIsNotEnabled()
        capture("empty")
    }

    private fun showResults(empty: Boolean) {
        val books = if (empty) emptyList() else BookPreviewParameterProvider().values.toList()
        val results = flowOf(loadedSearchData(books))
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                ManiculePreviewTheme(darkTheme = darkTheme) {
                    Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                        SearchScreen(
                            uiState = SearchUiState(query = "Book", inputPhase = SearchInputPhase.SUBMITTED),
                            searchResults = results,
                            searchFieldState = rememberTextFieldState("Book"),
                            onSearch = {},
                            onQuerySelected = {},
                            onNavigateBack = {},
                            onBookSelected = {},
                            scannerAction = SearchScannerAction.Unavailable,
                        )
                    }
                }
            }
        }
    }

    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        File(context.getExternalFilesDir(null), "$name-dark-$darkTheme-font-$fontScale.png").outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "dark={0}, font={1}")
        fun appearances() = listOf(arrayOf<Any>(false, 1f), arrayOf<Any>(true, 1f), arrayOf<Any>(false, 1.5f))
    }
}
