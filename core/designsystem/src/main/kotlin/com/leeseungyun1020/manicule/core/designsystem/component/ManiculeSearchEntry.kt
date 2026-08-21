package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManiculeSearchEntry(
    onClick: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    Box(modifier = modifier) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    textFieldState = textFieldState,
                    searchBarState = searchBarState,
                    onSearch = {},
                    modifier =
                        Modifier
                            .focusProperties { canFocus = false }
                            .clearAndSetSemantics {},
                    readOnly = true,
                    placeholder = { Text(text = placeholder) },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                )
            },
            state = searchBarState,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clickable(role = Role.Button, onClick = onClick)
                    .semantics { contentDescription = placeholder },
        )
    }
}

@ManiculePreview
@Composable
private fun ManiculeSearchEntryPreview() {
    ManiculePreviewTheme {
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
