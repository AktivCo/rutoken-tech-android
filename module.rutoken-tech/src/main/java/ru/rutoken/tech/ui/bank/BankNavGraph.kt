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
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.AppSessionType
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
    data object TokenAuth : BankDestination("bank/tokenAuth")
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
            onDeleteUsers = { /*TODO*/ },
            onUserClicked = { /*TODO*/ },
            onAddUserClicked = { navController.navigate(BankDestination.TokenAuth.route) { launchSingleTop = true } },
            openDrawer = openDrawer
        )
    }

    bottomSheet(BankDestination.TokenAuth.route) {
        TokenAuthScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            loginViewModel = koinViewModel<LoginViewModel>(),
            appSessionType = AppSessionType.BANK_USER_ADDING_SESSION,
            onAuthDone = { navController.navigate(BankDestination.Start.route) /*TODO: navigate to certificates bottom sheet */ },
            onNavigateBack = navController::popBackStack
        )
    }
}