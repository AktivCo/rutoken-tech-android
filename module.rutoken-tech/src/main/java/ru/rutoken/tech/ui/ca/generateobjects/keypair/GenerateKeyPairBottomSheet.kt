/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.generateobjects.keypair

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.map
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.PrimaryButtonBox
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.SegmentedButtonRow
import ru.rutoken.tech.ui.components.TextGroupBox
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.components.alertdialog.ConnectTokenDialog
import ru.rutoken.tech.ui.components.alertdialog.ErrorAlertDialog
import ru.rutoken.tech.ui.components.alertdialog.SimpleAlertDialog
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.errorDialogData
import ru.rutoken.tech.ui.utils.expandedSheetState
import ru.rutoken.tech.ui.utils.figmaPaddingValues

@Composable
fun GenerateKeyPairScreen(viewModel: GenerateKeyPairViewModel, onNavigateBack: () -> Unit, onLogout: () -> Unit) {
    val keyPairId by viewModel.keyPairId.map { convertToDefaultString(it) }.observeAsState("")
    val algorithm by viewModel.algorithm.observeAsState(KeyPairAlgorithm.GOST_2012_256)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (keyPairId.isNotEmpty()) {
        GenerateKeyPairBottomSheet(
            keyPairId = keyPairId,
            algorithm = algorithm,
            sheetState = sheetState,
            onDismiss = onNavigateBack,
            onAlgorithmSelected = viewModel::selectAlgorithm,
            onGenerationButtonClicked = viewModel::generateKeyPair
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
    algorithm: KeyPairAlgorithm,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAlgorithmSelected: (KeyPairAlgorithm) -> Unit,
    onGenerationButtonClicked: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = { bottomSheetWindowInsets() },
    ) {
        BottomSheetTitle(stringResource(id = R.string.key_pair_title))

        SegmentedButtonRow(
            onLeftSectionClicked = { onAlgorithmSelected(KeyPairAlgorithm.GOST_2012_256) },
            onRightSectionClicked = { onAlgorithmSelected(KeyPairAlgorithm.ECDSA_SECP256R1) },
            isLeftSectionSelected = algorithm == KeyPairAlgorithm.GOST_2012_256,
            leftSectionText = stringResource(R.string.gost_short),
            rightSectionText = stringResource(R.string.ecdsa_short),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        TextGroupBox(
            items = listOf(
                TextGroupItem(
                    title = stringResource(R.string.key_pair_id),
                    value = keyPairId
                ),
                TextGroupItem(
                    title = stringResource(R.string.key_pair_algorithm),
                    value = stringResource(algorithm.title)
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
    val showDialog by viewModel.connectTokenDelegate.showConnectTokenDialog.observeAsState(false)

    if (showDialog) {
        ConnectTokenDialog(onDismissRequest = { viewModel.connectTokenDelegate.onDismissConnectTokenDialog() })
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
            text = stringResource(id = dialogState.data.text!!),
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
            text = stringResource(id = dialogState.errorDialogData.text!!),
            onDismissOrConfirm = viewModel::dismissErrorDialog
        )
    }
}

@PreviewLightDark
@Composable
private fun GenerateKeyPairBottomSheetPreview() {
    RutokenTechTheme {
        GenerateKeyPairBottomSheet(
            keyPairId = "5a:8b:22:34:11",
            algorithm = KeyPairAlgorithm.ECDSA_SECP256R1,
            sheetState = expandedSheetState(),
            onDismiss = {},
            onAlgorithmSelected = {}
        ) {}
    }
}
