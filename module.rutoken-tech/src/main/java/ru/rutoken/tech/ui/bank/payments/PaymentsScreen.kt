/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payments

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.components.ScreenTopAppBar
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.figmaPadding
import ru.rutoken.tech.utils.toDateString
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun PaymentsScreen(
    viewModel: PaymentsViewModel,
    onNavigateBack: () -> Unit,
    onResetPaymentsClicked: () -> Unit,
    onPaymentClicked: (Payment) -> Unit,
    isIncomingPaymentsSelected: Boolean = true
) {
    val payments by viewModel.payments.observeAsState(listOf())

    PaymentsScreen(
        payments = payments,
        onNavigateBack = onNavigateBack,
        onResetPaymentsClicked = onResetPaymentsClicked,
        onPaymentClicked = onPaymentClicked,
        isIncomingPaymentsSelected = isIncomingPaymentsSelected
    )
}

@Composable
private fun PaymentsScreen(
    payments: List<Payment>,
    onNavigateBack: () -> Unit,
    onResetPaymentsClicked: () -> Unit,
    onPaymentClicked: (Payment) -> Unit,
    isIncomingPaymentsSelected: Boolean = true
) {
    val incomingPayments = payments.filter { it.isIncoming() }
    val outgoingPayments = payments.filter { !it.isIncoming() }
    var showIncomingPayments by remember { mutableStateOf(isIncomingPaymentsSelected) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            ScreenTopAppBar(
                screenName = stringResource(id = R.string.payments_title),
                navigationIcon = { AppIcons.Back() },
                onNavigationIconClick = onNavigateBack,
                trailingIcon = { AppIcons.ResetPayments() },
                onTrailingIconClick = onResetPaymentsClicked
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            SegmentedButtonRow(
                onIncomingPaymentsClicked = {
                    showIncomingPayments = true
                    scope.launch { scrollState.scrollTo(0) }
                },
                onOutgoingPaymentsClicked = {
                    showIncomingPayments = false
                    scope.launch { scrollState.scrollTo(0) }
                },
                isIncomingPaymentsSelected = isIncomingPaymentsSelected
            )
            Spacer(Modifier.height(12.dp))
            PaymentsGroup(
                payments = if (showIncomingPayments) incomingPayments else outgoingPayments,
                scrollState = scrollState,
                onPaymentClicked = onPaymentClicked
            )
        }
    }
}

@Composable
private fun SegmentedButtonRow(
    onIncomingPaymentsClicked: () -> Unit,
    onOutgoingPaymentsClicked: () -> Unit,
    isIncomingPaymentsSelected: Boolean
) {
    var selectedIndex by remember { mutableIntStateOf(if (isIncomingPaymentsSelected) 0 else 1) }
    val options = listOf(stringResource(R.string.incoming_payments), stringResource(R.string.outgoing_payments))
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = {
                    selectedIndex = index
                    if (selectedIndex == 0) onIncomingPaymentsClicked() else onOutgoingPaymentsClicked()
                },
                selected = index == selectedIndex
            ) {
                Text(text = label)
            }
        }
    }
}

@Composable
private fun PaymentsGroup(payments: List<Payment>, scrollState: ScrollState, onPaymentClicked: (Payment) -> Unit) {
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            val archivedPayments = payments.filter { it.isArchived() }
            val activePayments = payments.filter { !it.isArchived() }

            if (activePayments.isNotEmpty())
                PaymentsGroup(payments = activePayments, forArchive = false, onPaymentClicked = onPaymentClicked)

            if (archivedPayments.isNotEmpty()) {
                if (activePayments.isNotEmpty())
                    Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.archive),
                    modifier = Modifier.figmaPadding(10.dp, 4.dp, 10.dp, 16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                PaymentsGroup(payments = archivedPayments, forArchive = true, onPaymentClicked = onPaymentClicked)
            }
        }
    }
}

@Composable
private fun PaymentsGroup(payments: List<Payment>, forArchive: Boolean, onPaymentClicked: (Payment) -> Unit) {
    val paymentsMap = payments.sortedByDescending { it.date }.groupBy { it.date }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        paymentsMap.keys.forEach { date ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = date.toDateString(),
                    modifier = Modifier.figmaPadding(8.dp, 16.dp, 0.dp, 16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                )
                paymentsMap[date]!!.forEach { payment ->
                    PaymentCard(
                        payment = payment,
                        icon = { payment.Icon(forArchive) },
                        onClick = { onPaymentClicked(payment) })
                }
            }
        }
    }
}

@Composable
private fun PaymentCard(payment: Payment, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = payment.amount,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = payment.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = payment.organization,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun PaymentsScreenPreview() {
    RutokenTechTheme {
        val payment1 = Payment(
            title = "Платежное поручение №121",
            date = LocalDate.now(),
            amount = "14 500 ₽",
            organization = "ОАО Нефтегаз",
            userActionType = UserActionType.VERIFY
        )
        val payment2 = Payment(
            title = "Инкассовое поручение №122",
            date = LocalDate.of(2023, 12, 10),
            amount = "4 500 ₽",
            organization = "ОАО Нефтегаз",
            userActionType = UserActionType.DECRYPT,
            actionTime = LocalDateTime.now()
        )

        PaymentsScreen(
            payments = listOf(payment1, payment2, payment2, payment1),
            onNavigateBack = {},
            onResetPaymentsClicked = {},
            onPaymentClicked = {}
        )
    }
}