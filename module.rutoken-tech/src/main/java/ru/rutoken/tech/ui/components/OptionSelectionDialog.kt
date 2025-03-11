/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun OptionSelectionDialog(
    text: String,
    firstOptionText: String,
    onFirstOptionClick: () -> Unit,
    secondOptionText: String,
    onSecondOptionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = { /* Can't be dismissed manually */ }) {
        Surface(
            modifier = modifier.width(312.dp),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .weight(weight = 1f, fill = false)
                        .padding(bottom = 24.dp)
                        .align(Alignment.Start)
                ) {
                    Text(
                        text = text,
                        color = AlertDialogDefaults.textContentColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onFirstOptionClick) {
                        Text(
                            text = firstOptionText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    TextButton(onClick = onSecondOptionClick) {
                        Text(
                            text = secondOptionText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun OptionSelectionDialogPreview() {
    RutokenTechTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OptionSelectionDialog(
                text = "Подписание завершено",
                firstOptionText = "Поделиться",
                onFirstOptionClick = { /* Nothing to do */ },
                secondOptionText = "К документам",
                onSecondOptionClick = { /* Nothing to do */ },
            )
        }
    }
}
