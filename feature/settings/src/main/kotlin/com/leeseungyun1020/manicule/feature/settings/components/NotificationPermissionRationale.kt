package com.leeseungyun1020.manicule.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeDialog
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.feature.settings.R

@Composable
internal fun NotificationPermissionRationale(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    ManiculeDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.settings_notification_permission_title),
        message = stringResource(R.string.settings_notification_permission_denied),
        confirmText = stringResource(R.string.settings_continue),
        onConfirm = onContinue,
        dismissText = stringResource(R.string.settings_cancel),
    )
}

@ManiculePreview
@Composable
private fun NotificationPermissionRationalePreview() {
    ManiculePreviewTheme {
        NotificationPermissionRationale(onContinue = {}, onDismiss = {})
    }
}
