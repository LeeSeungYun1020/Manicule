package com.leeseungyun1020.manicule.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.datetime.LocalDate

class BookPreviewParameterProvider : PreviewParameterProvider<Book> {
    override val values: Sequence<Book> =
        sequenceOf(
            Book(
                isbn = "9791161759692",
                title = "Kotlin in Action 2/e",
                author = "세바스티안 아이그너 외",
                publisher = "에이콘출판사",
                publishedDate = LocalDate(2025, 2, 27),
                coverUrl = "https://nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2025/02/9791161759692.jpg",
                totalPages = 320,
                price = 40_000,
                category = "프로그래밍",
                tableOfContentsUrl = "https://example.com/contents",
                introductionUrl = "https://example.com/introduction",
                summaryUrl = "https://example.com/summary",
            ),
            Book(
                isbn = "9788954699914",
                title = "긴 제목과 부제까지 표시되는 책의 목록 말줄임 상태",
                author = "여러 저자 이름이 길게 이어지는 미리보기 저자 정보",
                publisher = "미리보기출판사",
                publishedDate = null,
                coverUrl = null,
                totalPages = null,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            ),
        )
}
