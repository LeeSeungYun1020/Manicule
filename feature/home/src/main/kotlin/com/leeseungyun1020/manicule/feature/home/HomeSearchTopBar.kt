package com.leeseungyun1020.manicule.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing

private val SearchAppBarHeight = 64.dp
private val SearchFieldHeight = 56.dp
private val BrandMarkSize = 40.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeSearchTopBar(
    onSearch: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Box(
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(TopAppBarDefaults.windowInsets),
            contentAlignment = Alignment.TopCenter,
        ) {
            Row(
                modifier =
                    Modifier
                        .widthIn(max = ManiculeSize.contentMaxWidth)
                        .fillMaxWidth()
                        .height(SearchAppBarHeight)
                        .padding(horizontal = ManiculeSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(ManiculeSize.touchTargetMin),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_home_brand_mark),
                        contentDescription = stringResource(R.string.home_title),
                        modifier = Modifier.size(BrandMarkSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(ManiculeSpacing.sm))
                Surface(
                    onClick = onSearch,
                    modifier = Modifier.weight(1f).height(SearchFieldHeight),
                    shape = SearchBarDefaults.inputFieldShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.home_search_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.width(ManiculeSpacing.sm))
                ManiculeIconButton(
                    onClick = onScan,
                    modifier = Modifier.size(ManiculeSize.touchTargetMin),
                ) {
                    Icon(
                        imageVector = ManiculeIcons.ScanBarcode,
                        contentDescription = stringResource(R.string.home_scan),
                    )
                }
            }
        }
    }
}

@ManiculePreview
@Preview(name = "Foldable", widthDp = 673, showBackground = true)
@Preview(name = "Tablet", widthDp = 1200, showBackground = true)
@Composable
private fun HomeSearchTopBarPreview() {
    ManiculePreviewTheme {
        HomeSearchTopBar(onSearch = {}, onScan = {})
    }
}
