/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.choosecertificate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.bank.BankCertificate
import ru.rutoken.tech.ui.bank.CertificateCard
import ru.rutoken.tech.ui.components.BottomSheetDragHandle
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.alertdialog.ConfirmationAlertDialog
import ru.rutoken.tech.ui.components.alertdialog.ErrorAlertDialog
import ru.rutoken.tech.ui.components.bottomSheetCornerShape
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.errorDialogData
import ru.rutoken.tech.ui.utils.expandedSheetState

@Composable
fun ChooseNewCertificateScreen(
    viewModel: ChooseNewCertificateViewModel,
    onNavigateToPaymentsScreen: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val certificates by viewModel.certificates.observeAsState(listOf())

    if (certificates.isEmpty()) {
        ErrorAlertDialog(
            title = stringResource(id = R.string.rutoken_has_no_certificates),
            text = stringResource(id = R.string.use_ca_to_create_certificate),
            onDismissOrConfirm = onNavigateBack
        )
    } else {
        if (showBottomSheet) {
            ChooseNewCertificateBottomSheet(
                certificates = certificates,
                sheetState = sheetState,
                onCertificateClicked = viewModel::onCertificateClicked,
                onDismissRequest = onNavigateBack
            )
        }
    }

    AskBiometryDialog(viewModel)
    BiometryActivationFailedDialog(viewModel)
    ProgressIndicatorDialog(viewModel)

    val userAdded by viewModel.isUserAdded.observeAsState(false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(userAdded) {
        if (userAdded) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                showBottomSheet = false
                onNavigateToPaymentsScreen()
            }
        }
    }
}

@Composable
private fun ChooseNewCertificateBottomSheet(
    certificates: List<BankCertificate>,
    sheetState: SheetState,
    onCertificateClicked: (BankCertificate) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = { bottomSheetWindowInsets() },
        dragHandle = { BottomSheetDragHandle(sheetState = sheetState) },
        shape = bottomSheetCornerShape(sheetState = sheetState),
    ) {
        BottomSheetTitle(title = stringResource(id = R.string.choose_certificate))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
        ) {
            certificates.forEach { certificate ->
                CertificateCard(
                    name = certificate.name,
                    position = certificate.position ?: stringResource(R.string.not_set),
                    certificateExpirationDate = certificate.certificateExpirationDate,
                    organization = certificate.organization ?: stringResource(R.string.not_set),
                    algorithm = stringResource(certificate.algorithm),
                    errorText = certificate.errorText,
                    onClick = { onCertificateClicked(certificate) }
                )
            }
            NavigationBarSpacer()
        }
    }
}

@Composable
private fun AskBiometryDialog(viewModel: ChooseNewCertificateViewModel) {
    val showDialog by viewModel.askBiometryDialog.observeAsState(false)
    val activity = LocalContext.current as FragmentActivity

    if (showDialog) {
        ConfirmationAlertDialog(
            title = stringResource(R.string.ask_biometry_title),
            text = stringResource(R.string.ask_biometry_text),
            dismissText = stringResource(R.string.skip),
            confirmText = stringResource(R.string.activate),
            onDismiss = viewModel::onDismissBiometryDialog,
            onConfirm = { viewModel.onConfirmBiometryUsage(activity) }
        )
    }
}

@Composable
private fun BiometryActivationFailedDialog(viewModel: ChooseNewCertificateViewModel) {
    val dialogState by viewModel.biometryActivationFailedDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        ErrorAlertDialog(
            title = stringResource(id = dialogState.errorDialogData.title),
            text = stringResource(id = dialogState.errorDialogData.text),
            onDismissOrConfirm = viewModel::onDismissBiometryActivationFailedDialog
        )
    }
}

@Composable
private fun ProgressIndicatorDialog(viewModel: ChooseNewCertificateViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun ChooseNewCertificateBottomSheetPreview() {
    RutokenTechTheme {
        val name = "Иванов Михаил Романович"
        val algorithm = R.string.gost256_algorithm
        val error = "Срок действия сертификата ещё не наступил"
        val derBytes = byteArrayOf(0, 0, 0, 0)
        val ckaIdBytes = byteArrayOf(1, 2, 3)
        val certificates = listOf(
            BankCertificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm),
            BankCertificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm, error),
            BankCertificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm),
            BankCertificate(ckaIdBytes, derBytes, name, "Дизайнер", "07.03.2024", "Рутокен", algorithm, error)

        )
        ChooseNewCertificateBottomSheet(
            certificates = certificates,
            sheetState = expandedSheetState(),
            onCertificateClicked = {},
            onDismissRequest = {}
        )
    }
}