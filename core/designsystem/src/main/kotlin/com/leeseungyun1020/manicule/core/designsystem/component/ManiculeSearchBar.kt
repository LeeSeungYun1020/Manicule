package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManiculeSearchBar(
    state: TextFieldState,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    requestInitialFocus: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val searchBarState = rememberSearchBarState()

    LaunchedEffect(requestInitialFocus) {
        if (requestInitialFocus) {
            focusRequester.requestFocus()
        }
    }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = state,
                searchBarState = searchBarState,
                onSearch = onSearch,
                modifier = Modifier.focusRequester(focusRequester),
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
private fun ManiculeSearchBarPreview() {
    ManiculePreviewTheme {
        Column(
            modifier = Modifier.padding(ManiculeSpacing.lg),
        ) {
            ManiculeSearchBar(
                state = rememberTextFieldState(),
                onSearch = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Search books",
                requestInitialFocus = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
