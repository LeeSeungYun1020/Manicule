package com.leeseungyun1020.manicule.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.leeseungyun1020.manicule.core.model.ContributionDay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class ContributionPreviewParameterProvider : PreviewParameterProvider<List<ContributionDay>> {
    override val values: Sequence<List<ContributionDay>>
        get() {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val days =
                (364 downTo 0).map { daysAgo ->
                    val date = today.minus(daysAgo, DateTimeUnit.DAY)
                    val pages =
                        when {
                            daysAgo % 7 == 0 -> 0
                            daysAgo % 5 == 0 -> 10
                            daysAgo % 3 == 0 -> 30
                            daysAgo % 2 == 0 -> 60
                            else -> 120
                        }
                    ContributionDay.of(date, pages)
                }
            return sequenceOf(days)
        }
}
