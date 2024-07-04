/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components.alertdialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun ConnectTokenDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        modifier = Modifier.width(312.dp),
        title = { Text(text = stringResource(id = R.string.connect_token_title)) },
        text = {
            Column {
                Text(text = stringResource(id = R.string.nfc), fontWeight = FontWeight.W500)
                Text(text = stringResource(id = R.string.connect_nfc_token))

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = stringResource(id = R.string.usb), fontWeight = FontWeight.W500)
                Text(text = stringResource(id = R.string.connect_usb_token))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@PreviewLight
@PreviewDark
@Composable
private fun ConnectTokenDialogPreview() {
    RutokenTechTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ConnectTokenDialog(onDismissRequest = { /* Nothing to do */ })
        }
    }
}
