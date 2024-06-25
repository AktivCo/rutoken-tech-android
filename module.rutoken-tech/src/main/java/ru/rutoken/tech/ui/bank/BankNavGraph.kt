/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import org.koin.androidx.compose.koinViewModel
import ru.rutoken.tech.session.RutokenTechSessionHolder
import ru.rutoken.tech.ui.bank.startscreen.BankStartScreen
import ru.rutoken.tech.ui.bank.startscreen.BankStartScreenViewModel
import ru.rutoken.tech.ui.main.Destination
import ru.rutoken.tech.ui.main.composable

/**
 * Bank section destinations.
 */
sealed class BankDestination(override val route: String) : Destination {
    data object Start : BankDestination("bank/start")
}

fun NavGraphBuilder.addBankDestinations(sessionHolder: RutokenTechSessionHolder, openDrawer: () -> Unit) {
    composable(BankDestination.Start) {
        LaunchedEffect(Unit) {
            sessionHolder.resetSession() // Clear session on Bank start screen every time it is opened
        }

        BankStartScreen(
            viewModel = koinViewModel<BankStartScreenViewModel>(),
            onDeleteUsers = { /*TODO*/ },
            onUserClicked = { /*TODO*/ },
            onAddUserClicked = { /*TODO*/ },
            openDrawer = openDrawer
        )
    }
}