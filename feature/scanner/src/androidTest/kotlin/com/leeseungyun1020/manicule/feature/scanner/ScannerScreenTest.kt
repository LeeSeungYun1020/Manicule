package com.leeseungyun1020.manicule.feature.scanner

import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScannerScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val backLabel get() = context.getString(com.leeseungyun1020.manicule.core.designsystem.R.string.core_designsystem_back)

    @Test
    fun explanationButtonsAndBackForwardCallbacksWithoutAutomaticallyRequestingPermission() {
        var camera = 0
        var search = 0
        var back = 0
        compose.setContent {
            ManiculeTheme {
                ScannerScreen(ScannerUiState.PermissionDenied(), { back++ }, { search++ }, { camera++ })
            }
        }
        compose.runOnIdle { assertThat(camera).isEqualTo(0) }
        compose.onNodeWithText(context.getString(R.string.scanner_use_camera)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.scanner_search)).assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription(backLabel).performClick()
        compose.runOnIdle {
            assertThat(camera).isEqualTo(1)
            assertThat(search).isEqualTo(1)
            assertThat(back).isEqualTo(1)
        }
    }

    @Test
    fun initializingThenReadyShowsGuideAndFloatingBack() {
        val state = mutableStateOf<ScannerUiState>(ScannerUiState.Initializing)
        var back = false
        compose.setContent {
            ManiculeTheme { ScannerScreen(state.value, { back = true }, {}, {}) }
        }
        compose.onNodeWithText(context.getString(R.string.scanner_initializing)).assertIsDisplayed()
        compose.runOnIdle { state.value = ScannerUiState.Scanning }
        compose.onNodeWithText(context.getString(R.string.scanner_guide)).assertIsDisplayed()
        compose.onNodeWithTag("scanner_viewfinder").assertIsDisplayed()
        compose.onNodeWithContentDescription(backLabel).performClick()
        compose.runOnIdle { assertThat(back).isTrue() }
    }

    @Test
    fun failureOffersSearchAndBack() {
        var search = false
        var back = false
        compose.setContent {
            ManiculeTheme { ScannerScreen(ScannerUiState.Failed, { back = true }, { search = true }, {}) }
        }
        compose.onNodeWithText(context.getString(R.string.scanner_failed_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_search)).performClick()
        compose.onNodeWithContentDescription(backLabel).performClick()
        compose.runOnIdle {
            assertThat(search).isTrue()
            assertThat(back).isTrue()
        }
    }

    @Test
    fun settingsFailureAndActionsSurviveUiRestoration() {
        val restoration = StateRestorationTester(compose)
        var search = false
        restoration.setContent {
            ManiculeTheme {
                ScannerScreen(ScannerUiState.PermissionDenied(requiresSettings = true, launchFailed = true), {}, { search = true }, {})
            }
        }
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText(context.getString(R.string.scanner_permission_launch_failed)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_use_camera)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_search)).performClick()
        compose.runOnIdle { assertThat(search).isTrue() }
    }

    @Test
    fun darkLargeFontLandscapePermissionActionsRemainReachable() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                ManiculeTheme(darkTheme = true) {
                    ScannerScreen(ScannerUiState.PermissionDenied(), {}, {}, {}, Modifier.requiredSize(640.dp, 320.dp))
                }
            }
        }
        val titleText = context.getString(R.string.scanner_title)
        val appbar = compose.onNodeWithText(titleText).assertIsDisplayed().getUnclippedBoundsInRoot()
        compose.onNodeWithText(context.getString(R.string.scanner_use_camera)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_search)).performScrollTo().assertIsDisplayed()
        val appbarAfterScroll = compose.onNodeWithText(titleText).assertIsDisplayed().getUnclippedBoundsInRoot()
        assertThat(appbarAfterScroll.top).isEqualTo(appbar.top)
    }

    @Test
    fun normalPortraitMessageScreensFillAvailableHeightAndStayFixedOnDrag() {
        val stateHolder = mutableStateOf<ScannerUiState>(ScannerUiState.PermissionDenied())
        var expectedBottom = 640.dp
        compose.setContent {
            val bottomPadding = ScaffoldDefaults.contentWindowInsets.asPaddingValues().calculateBottomPadding()
            expectedBottom = 640.dp - bottomPadding
            ManiculeTheme {
                ScannerScreen(
                    uiState = stateHolder.value,
                    onNavigateBack = {},
                    onNavigateToSearch = {},
                    onUseCamera = {},
                    modifier = Modifier.requiredSize(360.dp, 640.dp),
                )
            }
        }

        val messageStates = listOf(
            ScannerUiState.PermissionDenied(),
            ScannerUiState.CameraUnavailable,
            ScannerUiState.Failed,
        )

        for (state in messageStates) {
            compose.runOnIdle { stateHolder.value = state }

            val appbar = compose.onNodeWithTag(SCANNER_TOP_BAR_TEST_TAG).assertIsDisplayed().getUnclippedBoundsInRoot()
            val card = compose.onNodeWithTag(SCANNER_MESSAGE_CARD_TEST_TAG).assertIsDisplayed().getUnclippedBoundsInRoot()

            // 점선 카드가 앱바 아래 가용 높이를 채우는지 검증 (상단은 앱바 하단, 하단은 패딩 안 가용 영역 하단)
            assertThat(card.top.value).isWithin(0.5f).of(appbar.bottom.value)
            assertThat(card.bottom.value).isWithin(0.5f).of(expectedBottom.value)

            // 드래그(swipeUp) 시도 후에도 본문 카드와 앱바 위치가 고정되어 있는지 검증
            compose.onNodeWithTag(SCANNER_MESSAGE_CARD_TEST_TAG).performTouchInput { swipeUp() }
            compose.waitForIdle()

            val appbarAfter = compose.onNodeWithTag(SCANNER_TOP_BAR_TEST_TAG).getUnclippedBoundsInRoot()
            val cardAfter = compose.onNodeWithTag(SCANNER_MESSAGE_CARD_TEST_TAG).getUnclippedBoundsInRoot()
            assertThat(cardAfter.top).isEqualTo(card.top)
            assertThat(cardAfter.bottom).isEqualTo(card.bottom)
            assertThat(appbarAfter.top).isEqualTo(appbar.top)
        }
    }

    @Test
    fun messageScreensDisplayExpectedTitlesAndActions() {
        val stateHolder = mutableStateOf<ScannerUiState>(ScannerUiState.PermissionDenied())
        compose.setContent {
            ManiculeTheme {
                ScannerScreen(stateHolder.value, {}, {}, {})
            }
        }

        // PermissionDenied: 카메라 사용, 검색 모두 표시
        compose.onNodeWithText(context.getString(R.string.scanner_permission_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_permission_description)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_use_camera)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_search)).assertIsDisplayed()

        // CameraUnavailable: 검색만 표시, 카메라 사용 없음
        compose.runOnIdle { stateHolder.value = ScannerUiState.CameraUnavailable }
        compose.onNodeWithText(context.getString(R.string.scanner_camera_unavailable_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_camera_unavailable_description)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_search)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_use_camera)).assertDoesNotExist()

        // Failed: 검색만 표시, 카메라 사용 없음
        compose.runOnIdle { stateHolder.value = ScannerUiState.Failed }
        compose.onNodeWithText(context.getString(R.string.scanner_failed_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_failed_description)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_search)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.scanner_use_camera)).assertDoesNotExist()
    }

    @Test
    fun guideMaintainsPrototypeRatioAndMaximumWidthAcrossWindowSizes() {
        val window = mutableStateOf(360.dp to 640.dp)
        compose.setContent {
            ManiculeTheme {
                ScannerScreen(ScannerUiState.Scanning, {}, {}, {}, Modifier.requiredSize(window.value.first, window.value.second))
            }
        }
        listOf(360.dp to 640.dp, 673.dp to 841.dp, 1280.dp to 800.dp, 800.dp to 240.dp).forEach { size ->
            compose.runOnIdle { window.value = size }
            val guide = compose.onNodeWithTag("scanner_viewfinder").getUnclippedBoundsInRoot()
            val width = (guide.right - guide.left).value
            val height = (guide.bottom - guide.top).value
            assertThat(width).isAtMost(320.5f)
            assertThat(width / height).isWithin(0.03f).of(21f / 13f)
        }
    }
}
