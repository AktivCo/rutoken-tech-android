/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.largeTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import ru.rutoken.tech.ui.bank.payments.Base64String
import ru.rutoken.tech.ui.bank.payments.Payment
import ru.rutoken.tech.ui.bank.payments.UserActionType
import ru.rutoken.tech.ui.components.*
import ru.rutoken.tech.ui.components.alertdialog.AlertDialogWithIcon
import ru.rutoken.tech.ui.components.alertdialog.ConnectTokenDialog
import ru.rutoken.tech.ui.components.alertdialog.ErrorAlertDialog
import ru.rutoken.tech.ui.components.alertdialog.SimpleAlertDialog
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.theme.bodyMediumOnSurfaceVariant
import ru.rutoken.tech.ui.utils.*
import ru.rutoken.tech.utils.decoded
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    onNavigateBack: (Boolean) -> Unit,
    onSharePaymentClicked: () -> Unit,
    onNavigateToTokenAuth: () -> Unit
) {
    val payment by viewModel.payment.observeAsState()
    val operationCompleted by viewModel.operationCompleted.observeAsState(false)
    val navigateToTokenAuth by viewModel.navigateToTokenAuth.observeAsState(false)

    payment?.let {
        PaymentScreen(
            payment = it,
            operationCompleted = operationCompleted,
            onNavigateBack = { onNavigateBack(it.isIncoming()) },
            onSharePaymentClicked = onSharePaymentClicked,
            onUserActionButtonClicked = viewModel::onUserActionButtonClicked
        )

        BackHandler { onNavigateBack(it.isIncoming()) }
    }

    if (navigateToTokenAuth) {
        onNavigateToTokenAuth()
        viewModel.resetNavigateToTokenAuthEvent()
    }

    ConnectTokenDialog(viewModel)
    ProgressIndicatorDialog(viewModel)
    OperationCompletedDialog(viewModel)
    ErrorDialog(viewModel)
}

@Composable
private fun PaymentScreen(
    payment: Payment,
    operationCompleted: Boolean,
    onNavigateBack: () -> Unit,
    onSharePaymentClicked: () -> Unit,
    onUserActionButtonClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            ScreenTopAppBar(
                screenName = payment.title,
                navigationIcon = { AppIcons.Back() },
                onNavigationIconClick = onNavigateBack,
                trailingIcon = { AppIcons.Share() },
                onTrailingIconClick = onSharePaymentClicked,
                colors = largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            PaymentView(modifier = Modifier.weight(1f, false), payment = payment)
            Footer(
                payment = payment,
                operationCompleted = operationCompleted,
                onUserActionButtonClicked = onUserActionButtonClicked
            )
            NavigationBarSpacer()
        }
    }
}

@Composable
private fun PaymentView(modifier: Modifier, payment: Payment) {
    val renderData = payment.getRenderData(LocalContext.current)
    if (payment.hasEncryptedRenderData()) {
        EncryptedPaymentView(modifier, renderData)
    } else {
        PaymentPdfView(modifier, renderData.decoded)
    }
}

@Composable
private fun EncryptedPaymentView(modifier: Modifier, renderData: Base64String) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize()
    ) {
        val renderDataList = renderData.chunked(5000)
        LazyColumn(contentPadding = PaddingValues(24.dp)) {
            items(renderDataList) { data ->
                Text(
                    text = data,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PaymentPdfView(modifier: Modifier, renderData: ByteArray) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { context ->
                PDFView(context, null).apply {
                    fromBytes(renderData).scrollHandle(DefaultScrollHandle(context)).spacing(4).load()
                }
            }
        )
    }
}

@Composable
private fun Footer(payment: Payment, operationCompleted: Boolean, onUserActionButtonClicked: () -> Unit) {
    if (payment.isArchived() || operationCompleted) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = payment.getArchivedActionText(),
                style = bodyMediumOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        SecondaryButtonBox(
            modifier = Modifier.fillMaxWidth(),
            text = payment.getActionButtonText(),
            onClick = onUserActionButtonClicked
        )
    }
}

@Composable
private fun ConnectTokenDialog(viewModel: PaymentViewModel) {
    val showDialog by viewModel.tokenConnector.showConnectTokenDialog.observeAsState(false)

    if (showDialog)
        ConnectTokenDialog(onDismissRequest = viewModel.tokenConnector::onDismissConnectTokenDialog)
}

@Composable
private fun ProgressIndicatorDialog(viewModel: PaymentViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
private fun OperationCompletedDialog(viewModel: PaymentViewModel) {
    val dialogState by viewModel.operationCompletedDialogState.observeAsState(DialogState())

    if (!dialogState.showDialog) return

    if (dialogState.data is DialogDataWithIcon) {
        val dialogData = dialogState.data as DialogDataWithIcon
        AlertDialogWithIcon(
            icon = dialogData.icon,
            title = stringResource(dialogData.title),
            text = if (dialogData.text != null) stringResource(dialogData.text) else null,
            onDismissOrConfirm = viewModel::onDismissOperationCompletedDialog
        )
    } else {
        SimpleAlertDialog(
            text = stringResource(id = dialogState.data.text!!),
            onDismissOrConfirm = viewModel::onDismissOperationCompletedDialog
        )
    }
}

@Composable
private fun ErrorDialog(viewModel: PaymentViewModel) {
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
@PreviewLight
@PreviewDark
private fun ActivePaymentScreenPreview() {
    RutokenTechTheme {
        val payment = Payment(
            title = "Платежное поручение №121",
            date = LocalDate.now(),
            amount = "14 500 ₽",
            organization = "ОАО Нефтегаз",
            userActionType = UserActionType.DECRYPT,
            initialActionData = "MIIDrTCCA1ygAwIBAgIKc522PB4SUNntVzAIBgYqhQMCAgMwgbMxCzAJBgNVBAYTAlJVMSAwHgYDVQQK"
        )
        PaymentScreen(
            payment = payment,
            operationCompleted = false,
            onNavigateBack = {},
            onSharePaymentClicked = {},
            onUserActionButtonClicked = {}
        )
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun ArchivedPaymentScreenPreview() {
    RutokenTechTheme {
        val payment = Payment(
            title = "Платежное поручение №121",
            date = LocalDate.now(),
            amount = "14 500 ₽",
            organization = "ОАО Нефтегаз",
            userActionType = UserActionType.ENCRYPT,
            actionResultData = "MIIDrTCCA1ygAwIBAgIKc522PB4SUNntVzAIBgYqhQMCAgMwgbMxCzAJBgNVBAYTAlJVMSAwHgYDVQQK",
            actionTime = LocalDateTime.now()
        )
        PaymentScreen(
            payment = payment,
            operationCompleted = true,
            onNavigateBack = {},
            onSharePaymentClicked = {},
            onUserActionButtonClicked = {}
        )
    }
}