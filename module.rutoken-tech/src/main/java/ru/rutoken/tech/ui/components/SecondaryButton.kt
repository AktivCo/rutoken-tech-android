/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun SecondaryButtonBox(
    modifier: Modifier,
    text: String,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: () -> Unit
) {
    Box(modifier = modifier.padding(padding), contentAlignment = Alignment.Center) {
        SecondaryButton(text, onClick)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.height(40.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun SecondaryButtonPreview() {
    RutokenTechTheme {
        Surface {
            Column {
                SecondaryButtonBox(modifier = Modifier, text = "Text") {}
            }
        }
    }
}