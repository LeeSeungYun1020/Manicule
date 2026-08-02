package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

@Composable
fun ManiculeButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = MaterialTheme.spacing.xl, vertical = MaterialTheme.spacing.md),
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            }
            Text(text = text)
        }
    }
}

@Composable
fun ManiculeOutlinedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text = text)
    }
}

@Composable
fun ManiculeTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text = text)
    }
}

@Composable
fun ManiculeIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier, // IconButton 기본적으로 48x48 최소 터치 영역을 가짐
        enabled = enabled,
    ) {
        icon()
    }
}

@ManiculePreview
@Composable
private fun ManiculeButtonPreview() {
    ManiculeTheme {
        Column(
            modifier = Modifier.padding(ManiculeSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ManiculeSpacing.lg),
        ) {
            ManiculeButton(
                onClick = {},
                text = "아이콘 있는 버튼",
                leadingIcon = {
                    Icon(
                        imageVector = ManiculeIcons.Search,
                        contentDescription = null,
                    )
                },
            )
            ManiculeButton(onClick = {}, text = "일반 버튼")
            ManiculeButton(onClick = {}, text = "비활성화 버튼", enabled = false)
            ManiculeOutlinedButton(onClick = {}, text = "아웃라인 버튼")
            ManiculeTextButton(onClick = {}, text = "텍스트 버튼")
            ManiculeIconButton(
                onClick = {},
                icon = {
                    Icon(
                        imageVector = ManiculeIcons.Search,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
