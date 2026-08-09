package com.leeseungyun1020.manicule.feature.search.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.search.R
import kotlinx.serialization.Serializable

@Serializable
object SearchRoute

fun NavGraphBuilder.searchScreen() {
    composable<SearchRoute> { SearchStubScreen() }
}

@Composable
private fun SearchStubScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.search_stub_label),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
