package com.leeseungyun1020.manicule.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import com.leeseungyun1020.manicule.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ScannerNavigationTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun scannerSearchAction_navigatesToSearchAndPopsScannerFromBackStack() {
        hilt.inject()

        // 1. 서재(Library) 탭으로 이동
        compose.onNodeWithText(compose.activity.getString(R.string.tab_library)).performClick()

        // 2. 빈 서재 화면에서 스캔("Scan" / "스캔") 버튼 클릭하여 스캐너 진입
        val scanText = compose.activity.getString(com.leeseungyun1020.manicule.feature.library.R.string.library_scan)
        compose.onNodeWithText(scanText).performClick()

        // 3. 스캐너 권한 거부/실패 상태에서 검색("Search" / "직접 검색") 버튼 클릭
        val searchInScannerText = compose.activity.getString(com.leeseungyun1020.manicule.feature.scanner.R.string.scanner_search)
        compose.onNodeWithText(searchInScannerText).performClick()

        // 4. 검색 화면 진입 확인 (검색 입력창 표시)
        compose.onNode(hasSetTextAction()).assertIsDisplayed()

        // 5. 뒤로가기 수행 시 스캐너가 pop되어 서재 화면으로 복귀하는지 검증
        pressBack()
        compose.onNodeWithText(scanText).assertIsDisplayed()
    }
}
