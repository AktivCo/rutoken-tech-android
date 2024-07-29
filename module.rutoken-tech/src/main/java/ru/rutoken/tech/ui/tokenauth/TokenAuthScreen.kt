/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.tokenauth

import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import ru.rutoken.tech.session.AppSessionType
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.alertdialog.ConnectTokenDialog
import ru.rutoken.tech.ui.components.alertdialog.ErrorAlertDialog
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.errorDialogData

@Composable
fun TokenAuthScreen(
    enterPinViewModel: EnterPinViewModel,
    loginViewModel: LoginViewModel,
    appSessionType: AppSessionType,
    onAuthDone: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val enterPinSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEnterPinBottomSheet by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val activity = LocalContext.current as FragmentActivity

    if (showEnterPinBottomSheet) {
        EnterPinBottomSheet(
            viewModel = enterPinViewModel,
            sheetState = enterPinSheetState,
            onNavigateBack = onNavigateBack,
            onButtonClick = { loginViewModel.login(appSessionType, it, enterPinViewModel::onInvalidPin) },
            onDecryptBiometricPin = { enterPinViewModel.fillPinWithBiometricPrompt(activity) }
        )
    }

    ConnectTokenDialog(loginViewModel)
    ProgressIndicatorDialog(loginViewModel)
    ErrorAlertDialog(loginViewModel)

    BiometryErrorAlertDialog(enterPinViewModel)

    val isAuthDone by loginViewModel.authDoneEvent.observeAsState(false)
    LaunchedEffect(isAuthDone) {
        if (isAuthDone)
            enterPinViewModel.onValidPinLogin(activity)
    }

    val biometryUpdateDone by enterPinViewModel.biometryUpdateDone.observeAsState(false)

    LaunchedEffect(biometryUpdateDone) {
        if (biometryUpdateDone) {
            scope.launch { enterPinSheetState.hide() }.invokeOnCompletion {
                showEnterPinBottomSheet = false
                onAuthDone()
            }
        }
    }
}

@Composable
private fun EnterPinBottomSheet(
    viewModel: EnterPinViewModel,
    sheetState: SheetState,
    onNavigateBack: () -> Unit,
    onButtonClick: (String) -> Unit,
    onDecryptBiometricPin: () -> Unit
) {
    val pinErrorText by viewModel.pinErrorText.observeAsState("")
    val isButtonEnabled by viewModel.isButtonEnabled.observeAsState(false)
    val hasBiometricPin by viewModel.hasBiometricPin.observeAsState(false)
    val pinValue by viewModel.pinValue.observeAsState("")

    EnterPinBottomSheet(
        pinValue = pinValue,
        pinErrorText = pinErrorText,
        buttonEnabled = isButtonEnabled,
        onPinValueChanged = viewModel::onPinValueChanged,
        onButtonClicked = onButtonClick,
        sheetState = sheetState,
        onDismissRequest = onNavigateBack,
        hasBiometricPin = hasBiometricPin,
        onDecryptBiometricPin = onDecryptBiometricPin
    )
}

@Composable
private fun BiometryErrorAlertDialog(viewModel: EnterPinViewModel) {
    val dialogState by viewModel.biometryErrorDialogState.observeAsState(DialogState())
    if (dialogState.showDialog)
        ErrorAlertDialog(
            title = stringResource(dialogState.errorDialogData.title),
            text = stringResource(dialogState.errorDialogData.text!!),
            onDismissOrConfirm = { viewModel.dismissBiometryErrorDialog(dialogState.errorDialogData) }
        )
}

@Composable
private fun ConnectTokenDialog(viewModel: LoginViewModel) {
    val showDialog by viewModel.tokenConnector.showConnectTokenDialog.observeAsState(false)

    if (showDialog) {
        ConnectTokenDialog(onDismissRequest = { viewModel.tokenConnector.onDismissConnectTokenDialog() })
    }
}

@Composable
private fun ProgressIndicatorDialog(viewModel: LoginViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
private fun ErrorAlertDialog(viewModel: LoginViewModel) {
    val dialogState by viewModel.errorDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        ErrorAlertDialog(
            title = stringResource(id = dialogState.errorDialogData.title),
            text = stringResource(id = dialogState.errorDialogData.text!!),
            onDismissOrConfirm = { viewModel.onErrorDialogDismiss() }
        )
    }
}