package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextButton
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.feature.bookdetail.R

@Composable
fun BookDetailExpandableText(
    title: String,
    text: String?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember(text) { mutableStateOf(false) }
    var hasOverflow by remember(text) { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth().animateContentSize()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = text ?: stringResource(R.string.book_detail_content_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = if (text == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> if (!expanded) hasOverflow = result.hasVisualOverflow },
        )
        if (hasOverflow || expanded) {
            ManiculeTextButton(
                onClick = { expanded = !expanded },
                text = stringResource(if (expanded) R.string.book_detail_collapse else R.string.book_detail_expand),
                modifier = Modifier.heightIn(min = MaterialTheme.size.touchTargetMin),
            )
        }
    }
}

private const val COLLAPSED_LINES = 4

@ManiculePreview
@Composable
private fun BookDetailExpandableTextPreview() {
    ManiculeTheme {
        BookDetailExpandableText(
            title = "책 소개",
            text = "긴 책 소개를 펼치고 접을 수 있습니다. ".repeat(20),
        )
    }
}
