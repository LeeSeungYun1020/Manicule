package com.leeseungyun1020.manicule.feature.scanner.navigation

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
import com.leeseungyun1020.manicule.feature.scanner.R
import kotlinx.serialization.Serializable

@Serializable
object ScannerRoute

fun NavGraphBuilder.scannerScreen() {
    composable<ScannerRoute> { ScannerStubScreen() }
}

@Composable
private fun ScannerStubScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.scanner_stub_label),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
