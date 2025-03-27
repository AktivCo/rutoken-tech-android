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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.ButtonContent
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.alertdialog.ConnectTokenDialog
import ru.rutoken.tech.ui.components.alertdialog.ErrorAlertDialog
import ru.rutoken.tech.ui.shift.components.SignatoriesBottomSheet
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.tokenauth.EnterPinBottomSheet
import ru.rutoken.tech.ui.tokenauth.EnterPinViewModel
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.errorDialogData
import ru.rutoken.tech.ui.utils.expandedSheetState

@Composable
fun DocumentsSignScreen(
    signViewModel: DocumentsSignViewModel,
    enterPinViewModel: EnterPinViewModel,
    onNavigateBack: () -> Unit,
) {
    val signatories by signViewModel.signatoriesInfo.observeAsState(emptyList())
    val showEnterPinBottomSheet by signViewModel.showEnterPinBottomSheet.observeAsState(false)
    val navigateBack by signViewModel.navigateBack.observeAsState(false)
    val enterPinSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val signatoriesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    DocumentsSignScreen(
        signatories = signatories,
        onDismissRequest = signViewModel::onFinishSigningClick,
        onSignClick = signViewModel::onSignClicked,
        onFinishSigningClick = signViewModel::onFinishSigningClick,
        sheetState = signatoriesSheetState
    )


    LaunchedEffect(showEnterPinBottomSheet) {
        if (showEnterPinBottomSheet) {
            scope.launch { enterPinSheetState.show() }
        } else {
            scope.launch { enterPinSheetState.hide() }
            enterPinViewModel.clearPinValue()
        }
    }

    LaunchedEffect(navigateBack) {
        if (navigateBack) scope.launch { signatoriesSheetState.hide() }.invokeOnCompletion { onNavigateBack() }
    }

    if (showEnterPinBottomSheet || enterPinSheetState.isVisible) {
        EnterPinBottomSheet(
            viewModel = enterPinViewModel,
            sheetState = enterPinSheetState,
            onNavigateBack = signViewModel::onClosePincodeBottomSheet,
            onButtonClick = { pin -> signViewModel.onPinCodeEntered(pin, enterPinViewModel::onInvalidPin) }
        )
    }

    ConnectTokenDialog(signViewModel)
    ProgressIndicatorDialog(signViewModel)
    ErrorDialog(signViewModel)
}

@Composable
private fun DocumentsSignScreen(
    signatories: List<String>,
    onDismissRequest: () -> Unit,
    onSignClick: () -> Unit,
    onFinishSigningClick: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    if (signatories.isNotEmpty()) {
        SignatoriesBottomSheet(
            signatories = signatories,
            onDismissRequest = onDismissRequest,
            titleButton = ButtonContent(
                text = stringResource(R.string.finish_documents_signing),
                onClick = onFinishSigningClick
            ),
            actionButton = ButtonContent(text = stringResource(R.string.sign), onClick = onSignClick),
            shouldDismissOnBackPress = false,
            sheetState = sheetState,
        )
    }
}

@Composable
private fun ConnectTokenDialog(viewModel: DocumentsSignViewModel) {
    val showDialog by viewModel.connectTokenDelegate.showConnectTokenDialog.observeAsState(false)

    if (showDialog) {
        ConnectTokenDialog(onDismissRequest = { viewModel.connectTokenDelegate.onDismissConnectTokenDialog() })
    }
}

@Composable
private fun ProgressIndicatorDialog(viewModel: DocumentsSignViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
private fun ErrorDialog(viewModel: DocumentsSignViewModel) {
    val dialogState by viewModel.errorDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        ErrorAlertDialog(
            title = stringResource(id = dialogState.errorDialogData.title),
            text = stringResource(id = dialogState.errorDialogData.text!!),
            onDismissOrConfirm = viewModel::dismissErrorDialog
        )
    }
}

@Composable
private fun EnterPinBottomSheet(
    viewModel: EnterPinViewModel,
    sheetState: SheetState,
    onNavigateBack: () -> Unit,
    onButtonClick: (String) -> Unit,
) {
    val pinErrorText by viewModel.pinErrorText.observeAsState("")
    val isButtonEnabled by viewModel.isButtonEnabled.observeAsState(false)
    val pinValue by viewModel.pinValue.observeAsState("")

    EnterPinBottomSheet(
        pinValue = pinValue,
        pinErrorText = pinErrorText,
        buttonEnabled = isButtonEnabled,
        onPinValueChanged = viewModel::onPinValueChanged,
        onButtonClicked = onButtonClick,
        sheetState = sheetState,
        onDismissRequest = onNavigateBack,
        hasBiometricPin = false,
        onDecryptBiometricPin = {}
    )
}

@Composable
@PreviewLightDark
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
@PreviewLightDark
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
