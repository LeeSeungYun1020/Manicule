package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BookListItem(
    title: String,
    author: String,
    publisher: String,
    pubDate: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        BookCover(
            imageUrl = imageUrl,
            modifier = Modifier.size(64.dp, 92.dp),
            contentDescription = title,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier =
                Modifier
                    .height(92.dp)
                    .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$publisher · $pubDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookListItemPreview() {
    MaterialTheme {
        BookListItem(
            title = "Kotlin in action 2/e",
            author = "세바스티안 아이그너,로만 엘리자로프,스베트라나 이사코바,드미트리 제메로프 지음 ;오현석 옮김",
            publisher = "에이콘출판사",
            pubDate = "20250227",
            imageUrl = "https://nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2025/02/9791161759692.jpg",
        )
    }
}
