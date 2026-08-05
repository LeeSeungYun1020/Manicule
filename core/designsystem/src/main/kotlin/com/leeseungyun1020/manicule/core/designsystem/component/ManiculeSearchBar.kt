package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.clickable
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
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManiculeSearchBar(
    state: TextFieldState,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    readOnly: Boolean = false,
    autoFocus: Boolean = false,
    onReadOnlyClick: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val searchBarState = rememberSearchBarState()

    LaunchedEffect(autoFocus, readOnly) {
        if (autoFocus && !readOnly) {
            focusRequester.requestFocus()
        }
    }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = state,
                searchBarState = searchBarState,
                onSearch = onSearch,
                modifier =
                    Modifier
                        .focusRequester(focusRequester)
                        .then(
                            if (readOnly && onReadOnlyClick != null) {
                                Modifier.clickable(onClick = onReadOnlyClick)
                            } else {
                                Modifier
                            },
                        ),
                readOnly = readOnly,
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
    ManiculeTheme {
        Column(
            modifier = Modifier.padding(ManiculeSpacing.lg),
        ) {
            ManiculeSearchBar(
                state = rememberTextFieldState(),
                onSearch = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Search books",
                autoFocus = true,
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

@ManiculePreview
@Composable
private fun ManiculeReadOnlySearchBarPreview() {
    ManiculeTheme {
        ManiculeSearchBar(
            state = rememberTextFieldState(),
            onSearch = {},
            modifier = Modifier.fillMaxWidth().padding(ManiculeSpacing.lg),
            placeholder = "Find a book",
            readOnly = true,
            onReadOnlyClick = {},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                )
            },
        )
    }
}
