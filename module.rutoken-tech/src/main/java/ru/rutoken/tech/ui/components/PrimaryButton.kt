/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.ui.theme.RutokenTechTheme

@Composable
fun StickyPrimaryButtonBox(
    text: String,
    onClick: () -> Unit,
    offset: Density.() -> IntOffset,
    buttonEnabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .offset(offset)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .imePadding()
    ) {
        PrimaryButtonBox(text = text, enabled = buttonEnabled, onClick = onClick)

        NavigationBarSpacer()
    }
}

@Composable
fun PrimaryButtonBox(
    text: String,
    enabled: Boolean = true,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: () -> Unit
) {
    Box(Modifier.padding(padding)) {
        PrimaryButton(text, enabled = enabled, onClick)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Text(text)
    }
}

@PreviewLightDark
@Composable
private fun PrimaryButtonPreview() {
    RutokenTechTheme {
        Surface {
            Column {
                PrimaryButtonBox("Text") {}
                PrimaryButtonBox(text = "Text disabled", enabled = false) {}
            }
        }
    }
}
