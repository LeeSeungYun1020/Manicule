package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManiculeSearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = {},
                modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
                readOnly = true,
                placeholder = placeholder?.let { { Text(text = it) } },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        },
        state = searchBarState,
        modifier = modifier,
    )
}

@ManiculePreview
@Composable
private fun ManiculeSearchEntryPreview() {
    ManiculeTheme {
        ManiculeSearchEntry(
            onClick = {},
            modifier = Modifier.fillMaxWidth().padding(ManiculeSpacing.lg),
            placeholder = "Find a book",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                )
            },
        )
    }
}
