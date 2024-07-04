/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.generateobjects.keypair

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPairParams
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.alertdialog.ConnectTokenDialog
import ru.rutoken.tech.ui.components.alertdialog.ErrorAlertDialog
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.PrimaryButtonBox
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.alertdialog.SimpleAlertDialog
import ru.rutoken.tech.ui.components.TextGroupBox
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.errorDialogData
import ru.rutoken.tech.ui.utils.expandedSheetState
import ru.rutoken.tech.ui.utils.figmaPaddingValues

@Composable
fun GenerateKeyPairScreen(viewModel: GenerateKeyPairViewModel, onNavigateBack: () -> Unit, onLogout: () -> Unit) {
    val keyPairId by viewModel.keyPairId.observeAsState("")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.generateKeyPairId()
    }

    if (keyPairId.isNotEmpty()) {
        GenerateKeyPairBottomSheet(
            keyPairId = keyPairId,
            sheetState = sheetState,
            onDismiss = onNavigateBack,
            onGenerationButtonClicked = { viewModel.generateGostKeyPair(keyPairId, GostKeyPairParams.GOST_2012_256) }
        )
    }

    ConnectTokenDialog(viewModel)
    ProgressIndicatorDialog(viewModel)
    SuccessDialog(
        viewModel = viewModel,
        onDismissOrConfirm = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onNavigateBack()
                }
            }
        }
    )
    ErrorDialog(viewModel)

    val shouldLogout by viewModel.shouldLogout.observeAsState(false)
    if (shouldLogout) onLogout()
}

@Composable
fun GenerateKeyPairBottomSheet(
    keyPairId: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onGenerationButtonClicked: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = { bottomSheetWindowInsets() },
    ) {
        BottomSheetTitle(stringResource(id = R.string.key_pair_title))

        TextGroupBox(
            items = listOf(
                TextGroupItem(
                    title = stringResource(R.string.key_pair_id),
                    value = keyPairId
                ),
                TextGroupItem(
                    title = stringResource(R.string.key_pair_algorithm),
                    value = stringResource(R.string.gost256_algorithm)
                )
            ),
            padding = figmaPaddingValues(0.dp, 16.dp, 16.dp, 16.dp)
        )

        PrimaryButtonBox(stringResource(R.string.generate)) { onGenerationButtonClicked() }

        NavigationBarSpacer()
    }
}

@Composable
private fun ConnectTokenDialog(viewModel: GenerateKeyPairViewModel) {
    val showDialog by viewModel.tokenConnector.showConnectTokenDialog.observeAsState(false)

    if (showDialog) {
        ConnectTokenDialog(onDismissRequest = { viewModel.tokenConnector.onDismissConnectTokenDialog() })
    }
}

@Composable
private fun ProgressIndicatorDialog(viewModel: GenerateKeyPairViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
private fun SuccessDialog(viewModel: GenerateKeyPairViewModel, onDismissOrConfirm: () -> Unit) {
    val dialogState by viewModel.successDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        SimpleAlertDialog(
            text = stringResource(id = dialogState.data.text),
            onDismissOrConfirm = {
                viewModel.dismissSuccessDialog()
                onDismissOrConfirm()
            }
        )
    }
}

@Composable
private fun ErrorDialog(viewModel: GenerateKeyPairViewModel) {
    val dialogState by viewModel.errorDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        ErrorAlertDialog(
            title = stringResource(id = dialogState.errorDialogData.title),
            text = stringResource(id = dialogState.errorDialogData.text),
            onDismissOrConfirm = viewModel::dismissErrorDialog
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun GenerateKeyPairBottomSheetPreview() {
    RutokenTechTheme {
        GenerateKeyPairBottomSheet(
            keyPairId = "12345678-90abcdef",
            sheetState = expandedSheetState(),
            onDismiss = {}
        ) {}
    }
}
