package com.leeseungyun1020.manicule.feature.library.navigation

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
import com.leeseungyun1020.manicule.feature.library.R
import kotlinx.serialization.Serializable

@Serializable
object LibraryRoute

fun NavGraphBuilder.libraryScreen() {
    composable<LibraryRoute> { LibraryStubScreen() }
}

@Composable
private fun LibraryStubScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.library_stub_label),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
