package com.leeseungyun1020.manicule.feature.library.navigation

import androidx.annotation.Keep
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.library.LibraryRoute
import kotlinx.serialization.Serializable

/** 새 진입 시 사용할 탭. 복원된 화면에서는 저장된 사용자 선택을 유지한다. */
@Serializable
data class LibraryRoute(
    val initialTab: LibraryTab = LibraryTab.READING,
)

@Keep
@Serializable
enum class LibraryTab(
    val status: ReadingStatus,
) {
    WANT(ReadingStatus.WANT),
    READING(ReadingStatus.READING),
    FINISHED(ReadingStatus.FINISHED),
}

fun NavGraphBuilder.libraryScreen(
    onNavigateToBookDetail: (isbn: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToScanner: () -> Unit,
) {
    composable<LibraryRoute> {
        LibraryRoute(
            onNavigateToBookDetail = onNavigateToBookDetail,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToScanner = onNavigateToScanner,
        )
    }
}
