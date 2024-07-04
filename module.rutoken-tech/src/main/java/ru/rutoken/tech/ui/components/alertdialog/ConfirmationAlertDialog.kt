/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components.alertdialog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun ConfirmationAlertDialog(
    title: String? = null,
    text: String,
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val dialogTitle = @Composable { Text(text = title!!) }

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        modifier = Modifier.width(312.dp),
        title = if (title != null) dialogTitle else null,
        text = { Text(text = text) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@PreviewLight
@PreviewDark
@Composable
private fun ConfirmationAlertDialogNullTitlePreview() {
    RutokenTechTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ConfirmationAlertDialog(
                text = stringResource(id = R.string.delete_all_users),
                dismissText = stringResource(id = R.string.cancel),
                confirmText = stringResource(id = R.string.delete),
                onDismiss = { /* Nothing to do */ },
                onConfirm = { /* Nothing to do */ }
            )
        }
    }
}