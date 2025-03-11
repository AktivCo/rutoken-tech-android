/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.BottomSheetDragHandle
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.ButtonContent
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.StickyPrimaryButtonBox
import ru.rutoken.tech.ui.components.TextGroupBox
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.components.bottomSheetCornerShape
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets

@Composable
fun SignatoriesBottomSheet(
    signatories: List<String>,
    onDismissRequest: () -> Unit,
    titleButton: ButtonContent? = null,
    actionButton: ButtonContent? = null,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        sheetState = sheetState,
        dragHandle = { BottomSheetDragHandle(sheetState = sheetState) },
        shape = bottomSheetCornerShape(sheetState = sheetState),
        contentWindowInsets = { bottomSheetWindowInsets() },
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.documents_signatories_title, signatories.size),
            buttonContent = titleButton
        )

        TextGroupBox(
            items = signatories.map { TextGroupItem(it) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            padding = PaddingValues(horizontal = 16.dp)
        )

        if (actionButton != null) {
            StickyPrimaryButtonBox(
                text = actionButton.text,
                offset = { IntOffset(x = 0, y = -sheetState.requireOffset().toInt()) },
                onClick = actionButton.onClick
            )
        } else {
            NavigationBarSpacer()
        }
    }
}
