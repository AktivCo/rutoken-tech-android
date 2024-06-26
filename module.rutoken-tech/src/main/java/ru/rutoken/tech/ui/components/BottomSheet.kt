/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.statusBarsPaddingHeight

data class ButtonContent(val text: String, val onClick: () -> Unit)

@Composable
fun BottomSheetTitle(title: String, buttonContent: ButtonContent? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        buttonContent?.let {
            TextButton(
                onClick = it.onClick,
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(it.text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }

    }
}

@Composable
fun BottomSheetDragHandle(sheetState: SheetState) {
    if (sheetState.targetValue != SheetValue.Expanded) {
        BottomSheetDefaults.DragHandle()
    } else {
        Spacer(Modifier.statusBarsPaddingHeight())
    }
}

@Composable
fun bottomSheetCornerShape(sheetState: SheetState) =
    if (sheetState.targetValue != SheetValue.Expanded)
        BottomSheetDefaults.ExpandedShape
    else
        BottomSheetDefaults.HiddenShape

@PreviewLight
@PreviewDark
@Composable
private fun BottomSheetTitlePreview() {
    RutokenTechTheme {
        Surface(Modifier.fillMaxWidth()) {
            BottomSheetTitle(stringResource(id = R.string.key_pair_title))
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun BottomSheetTitleButtonPreview() {
    RutokenTechTheme {
        Surface(Modifier.fillMaxWidth()) {
            BottomSheetTitle(stringResource(id = R.string.key_pair_title), ButtonContent("Close") {})
        }
    }
}