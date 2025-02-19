/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.BottomSheetDragHandle
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.TextGroupBox
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.components.bottomSheetCornerShape
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.expandedSheetState

@Composable
fun DocumentSignatoriesBottomSheet(
    signatories: List<String>,
    onDismissRequest: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            BottomSheetTitle(title = stringResource(id = R.string.documents_signatories_title, signatories.size))
            TextGroupBox(
                items = signatories.map { TextGroupItem(it) },
                padding = PaddingValues(horizontal = 16.dp),
            )
        }
        NavigationBarSpacer()
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun DocumentSignatoriesBottomSheetPreview() {
    RutokenTechTheme {
        DocumentSignatoriesBottomSheet(
            signatories = listOf(
                "Петров Петр Петрович",
                "Марьева Мария Михайловна",
                "Иванов Иван Иванович",
                "Аннова Анна Алексеевна",
                "Михайлов Михаил Михайлович"
            ),
            sheetState = expandedSheetState(),
            onDismissRequest = {}
        )
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun DocumentSignatoriesPartiallyExpandedBottomSheetPreview() {
    RutokenTechTheme {
        DocumentSignatoriesBottomSheet(
            signatories = listOf(
                "Петров Петр Петрович",
                "Марьева Мария Михайловна",
                "Иванов Иван Иванович",
                "Аннова Анна Алексеевна",
                "Михайлов Михаил Михайлович"
            ),
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded),
            onDismissRequest = {}
        )
    }
}

