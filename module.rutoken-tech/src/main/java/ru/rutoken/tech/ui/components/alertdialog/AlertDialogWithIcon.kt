/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components.alertdialog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun AlertDialogWithIcon(
    icon: @Composable () -> Unit,
    title: String,
    text: String? = null,
    onDismissOrConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissOrConfirm,
        confirmButton = {
            TextButton(onClick = onDismissOrConfirm) {
                Text(stringResource(id = R.string.ok))
            }
        },
        modifier = Modifier.width(312.dp),
        icon = icon,
        title = { Text(modifier = Modifier.fillMaxWidth(), text = title, textAlign = TextAlign.Center) },
        text = text?.let { { Text(text = it) } },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@PreviewLight
@PreviewDark
@Composable
private fun AlertDialogWithIconAndTextPreview() {
    RutokenTechTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AlertDialogWithIcon(
                icon = { AppIcons.ValidSignature() },
                title = stringResource(id = R.string.verify_valid_signature),
                text = stringResource(id = R.string.verify_invalid_chain),
                onDismissOrConfirm = { /* Nothing to do */ }
            )
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun AlertDialogWithIconPreview() {
    RutokenTechTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AlertDialogWithIcon(
                icon = { AppIcons.InvalidSignature() },
                title = stringResource(id = R.string.verify_invalid_signature),
                onDismissOrConfirm = { /* Nothing to do */ }
            )
        }
    }
}