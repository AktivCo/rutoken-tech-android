/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.tokenauth

import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import ru.rutoken.tech.ui.ca.CaLoginViewModel
import ru.rutoken.tech.ui.components.ConnectTokenDialog
import ru.rutoken.tech.ui.components.ErrorAlertDialog
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.errorDialogData

@Composable
fun TokenAuthScreen(
    enterPinViewModel: EnterPinViewModel,
    caLoginViewModel: CaLoginViewModel,
    onNavigateToTokenInfo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val enterPinSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEnterPinBottomSheet by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showEnterPinBottomSheet) {
        EnterPinBottomSheet(
            viewModel = enterPinViewModel,
            sheetState = enterPinSheetState,
            onNavigateBack = onNavigateBack,
            onButtonClick = { caLoginViewModel.login(it, enterPinViewModel::setPinErrorValue) }
        )
    }

    ConnectTokenDialog(caLoginViewModel)
    ProgressIndicatorDialog(caLoginViewModel)
    ErrorAlertDialog(caLoginViewModel)

    val isAuthDone by caLoginViewModel.authDoneEvent.observeAsState(false)

    LaunchedEffect(isAuthDone) {
        if (isAuthDone) {
            scope.launch { enterPinSheetState.hide() }.invokeOnCompletion {
                showEnterPinBottomSheet = false
                onNavigateToTokenInfo()
            }
        }
    }
}

@Composable
private fun EnterPinBottomSheet(
    viewModel: EnterPinViewModel,
    sheetState: SheetState,
    onNavigateBack: () -> Unit,
    onButtonClick: (String) -> Unit
) {
    val pinErrorText by viewModel.pinErrorText.observeAsState("")
    val isButtonEnabled by viewModel.isButtonEnabled.observeAsState(false)

    EnterPinBottomSheet(
        pinErrorText = pinErrorText,
        buttonEnabled = isButtonEnabled,
        onPinValueChanged = viewModel::onPinValueChanged,
        onButtonClicked = onButtonClick,
        sheetState = sheetState,
        onDismissRequest = onNavigateBack
    )
}

@Composable
private fun ConnectTokenDialog(viewModel: CaLoginViewModel) {
    val showDialog by viewModel.tokenConnector.showConnectTokenDialog.observeAsState(false)

    if (showDialog) {
        ConnectTokenDialog(onDismissRequest = { viewModel.tokenConnector.onDismissConnectTokenDialog() })
    }
}

@Composable
private fun ProgressIndicatorDialog(viewModel: CaLoginViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
private fun ErrorAlertDialog(viewModel: CaLoginViewModel) {
    val dialogState by viewModel.errorDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        ErrorAlertDialog(
            title = stringResource(id = dialogState.errorDialogData.title),
            text = stringResource(id = dialogState.errorDialogData.text),
            onDismissOrConfirm = { viewModel.onErrorDialogDismiss() }
        )
    }
}