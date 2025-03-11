/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.sign

import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import org.koin.androidx.compose.koinViewModel
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.ButtonContent
import ru.rutoken.tech.ui.components.OptionSelectionDialog
import ru.rutoken.tech.ui.shift.components.SignatoriesBottomSheet
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.expandedSheetState

@Composable
fun DocumentsSignScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    viewModel: DocumentsSignViewModel = koinViewModel()
) {
    val signatories by viewModel.signatories.observeAsState(emptyList())
    val showFinishSigningDialog by viewModel.showFinishSigningDialog.observeAsState(false)

    if (showFinishSigningDialog)
        OptionSelectionDialog(
            text = stringResource(R.string.signing_operation_completed),
            firstOptionText = stringResource(R.string.share_document),
            onFirstOptionClick = viewModel::onDocumentShareClick,
            secondOptionText = stringResource(R.string.navigate_to_documents),
            onSecondOptionClick = onNavigateToDocuments
        )

    DocumentsSignScreen(
        signatories = signatories,
        onDismissRequest = onNavigateBack,
        onSignClick = viewModel::onSignClick,
        onFinishSigningClick = viewModel::onFinishSigningClick
    )
}

@Composable
private fun DocumentsSignScreen(
    signatories: List<String>,
    onDismissRequest: () -> Unit,
    onSignClick: () -> Unit,
    onFinishSigningClick: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    SignatoriesBottomSheet(
        signatories = signatories,
        onDismissRequest = onDismissRequest,
        titleButton = ButtonContent(
            text = stringResource(R.string.finish_documents_signing),
            onClick = onFinishSigningClick
        ),
        actionButton = ButtonContent(text = stringResource(R.string.sign), onClick = onSignClick),
        sheetState = sheetState,
    )
}

@Composable
@PreviewLight
@PreviewDark
private fun DocumentSignatoriesPartiallyExpandedBottomSheetPreview() {
    RutokenTechTheme {
        DocumentsSignScreen(
            signatories = listOf(
                "Петров Петр Петрович",
                "Марьева Мария Михайловна",
                "Иванов Иван Иванович",
                "Аннова Анна Алексеевна",
                "Михайлов Михаил Михайлович"
            ),
            onDismissRequest = { /* Nothing to do */ },
            onSignClick = { /* Nothing to do */ },
            onFinishSigningClick = { /* Nothing to do */ },
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded),
        )
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun DocumentsSignScreenPreview() {
    RutokenTechTheme {
        DocumentsSignScreen(
            signatories = listOf(
                "Петров Петр Петрович",
                "Марьева Мария Михайловна",
                "Иванов Иван Иванович",
                "Аннова Анна Алексеевна",
                "Михайлов Михаил Михайлович"
            ),
            onDismissRequest = { /* Nothing to do */ },
            onSignClick = { /* Nothing to do */ },
            onFinishSigningClick = { /* Nothing to do */ },
            sheetState = expandedSheetState(),
        )
    }
}
