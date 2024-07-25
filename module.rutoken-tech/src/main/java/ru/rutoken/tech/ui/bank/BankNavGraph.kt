/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.material.bottomSheet
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.AppSessionType
import ru.rutoken.tech.ui.bank.choosecertificate.ChooseNewCertificateScreen
import ru.rutoken.tech.ui.bank.choosecertificate.ChooseNewCertificateViewModel
import ru.rutoken.tech.ui.bank.payment.PaymentScreen
import ru.rutoken.tech.ui.bank.payment.PaymentViewModel
import ru.rutoken.tech.ui.bank.payments.PaymentsScreen
import ru.rutoken.tech.ui.bank.payments.PaymentsViewModel
import ru.rutoken.tech.ui.bank.startscreen.BankStartScreen
import ru.rutoken.tech.ui.bank.startscreen.BankStartScreenViewModel
import ru.rutoken.tech.ui.main.Destination
import ru.rutoken.tech.ui.main.composable
import ru.rutoken.tech.ui.tokenauth.EnterPinViewModel
import ru.rutoken.tech.ui.tokenauth.LoginViewModel
import ru.rutoken.tech.ui.tokenauth.TokenAuthScreen

/**
 * Bank section destinations.
 */
sealed class BankDestination(override val route: String) : Destination {
    data object Start : BankDestination("bank/start")
    data object UserAddingTokenAuth : BankDestination("bank/userAddingTokenAuth")
    data object UserLoginTokenAuth : BankDestination("bank/userLoginTokenAuth")
    data object UserOperationTokenAuth : BankDestination("bank/UserOperationTokenAuth")
    data object Certificates : BankDestination("bank/certificates")
    data object Payments : BankDestination("bank/payments")
    data object Payment : BankDestination("bank/payments/{paymentTitle}")
}

fun NavGraphBuilder.addBankDestinations(
    navController: NavController,
    sessionHolder: AppSessionHolder,
    openDrawer: () -> Unit
) {
    composable(BankDestination.Start) {
        LaunchedEffect(Unit) {
            sessionHolder.resetSession() // Clear session on Bank start screen every time it is opened
        }

        BankStartScreen(
            viewModel = koinViewModel<BankStartScreenViewModel>(),
            onNavigateToUserLogin = {
                navController.navigate(BankDestination.UserLoginTokenAuth.route) { launchSingleTop = true }
            },
            onNavigateToUserAdding = {
                navController.navigate(BankDestination.UserAddingTokenAuth.route) { launchSingleTop = true }
            },
            openDrawer = openDrawer
        )
    }

    bottomSheet(BankDestination.UserLoginTokenAuth.route) {
        TokenAuthScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            loginViewModel = koinViewModel<LoginViewModel>(),
            appSessionType = AppSessionType.BANK_USER_LOGIN_SESSION,
            onAuthDone = { navController.navigate(BankDestination.Payments.route) },
            onNavigateBack = navController::popBackStack
        )
    }

    bottomSheet(BankDestination.UserAddingTokenAuth.route) {
        TokenAuthScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            loginViewModel = koinViewModel<LoginViewModel>(),
            appSessionType = AppSessionType.BANK_USER_ADDING_SESSION,
            onAuthDone = { navController.navigate(BankDestination.Certificates.route) },
            onNavigateBack = navController::popBackStack
        )
    }

    bottomSheet(BankDestination.UserOperationTokenAuth.route) {
        TokenAuthScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            loginViewModel = koinViewModel<LoginViewModel>(),
            appSessionType = AppSessionType.BANK_USER_LOGIN_SESSION,
            onAuthDone = navController::popBackStack,
            onNavigateBack = navController::popBackStack
        )
    }

    bottomSheet(BankDestination.Certificates.route) {
        ChooseNewCertificateScreen(
            viewModel = koinViewModel<ChooseNewCertificateViewModel>(),
            onNavigateToPaymentsScreen = { navController.navigate(BankDestination.Payments.route) },
            onNavigateBack = { navController.popBackStack(BankDestination.Start.route, false) }
        )
    }

    composable(BankDestination.Payments) { backStackEntry ->
        PaymentsScreen(
            viewModel = koinViewModel<PaymentsViewModel>(),
            onNavigateBack = navController::popBackStack,
            onResetPaymentsClicked = { /*TODO*/ },
            onPaymentClicked = { navController.navigate(BankDestination.Payments.route + "/${it.title}") },
            isIncomingPaymentsSelected = backStackEntry.savedStateHandle.get<Boolean>("isIncomingPaymentsSelected")
                ?: true
        )
    }

    composable(BankDestination.Payment) { backStackEntry ->
        PaymentScreen(
            viewModel = koinViewModel<PaymentViewModel>(
                parameters = { parametersOf(backStackEntry.arguments!!.getString("paymentTitle")!!) }
            ),
            onNavigateBack = {
                navController.popBackStack()
                navController.currentBackStackEntry?.savedStateHandle?.set("isIncomingPaymentsSelected", it)
            },
            onSharePaymentClicked = { /*TODO*/ },
            onNavigateToTokenAuth = {
                navController.navigate(BankDestination.UserOperationTokenAuth.route) { launchSingleTop = true }
            }
        )
    }
}