package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

/**
 * Manicule 앱 전체에서 공통으로 사용되는 바텀 시트 컴포넌트입니다.
 *
 * 디자인 시스템 가이드라인에 맞춘 모서리 둥글기가 적용되어 있습니다.
 * 내부 여백이나 특정 배치는 [content] 내에서 직접 제어합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManiculeBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0),
            bottomEnd = CornerSize(0),
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        content = content,
    )
}

/**
 * ModalBottomSheet는 Dialog 기반이라 직접 Preview 불가.
 * 바텀시트의 시각적 형태(둥근 모서리 + 드래그 핸들 + 콘텐츠)를 정적으로 표현.
 */
@ManiculePreview
@Composable
private fun ManiculeBottomSheetPreview() {
    ManiculePreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge.copy(
                    bottomStart = CornerSize(0),
                    bottomEnd = CornerSize(0),
                ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = BottomSheetDefaults.Elevation,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BottomSheetDefaults.DragHandle()
                    Text(
                        text = "Bottom Sheet Content",
                        modifier = Modifier.padding(MaterialTheme.spacing.lg),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
                }
            }
        }
    }
}
