/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.material.bottomSheet
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.AppSessionType
import ru.rutoken.tech.ui.main.Destination
import ru.rutoken.tech.ui.main.composable
import ru.rutoken.tech.ui.shift.choosecertificate.ChooseNewShiftCertificateScreen
import ru.rutoken.tech.ui.shift.choosecertificate.ChooseNewShiftCertificateViewModel
import ru.rutoken.tech.ui.shift.documents.DocumentsScreen
import ru.rutoken.tech.ui.shift.documents.DocumentsViewModel
import ru.rutoken.tech.ui.shift.documentspreview.DocumentsPreviewScreen
import ru.rutoken.tech.ui.shift.sign.DocumentsSignScreen
import ru.rutoken.tech.ui.shift.sign.DocumentsSignViewModel
import ru.rutoken.tech.ui.shift.startscreen.ShiftStartScreen
import ru.rutoken.tech.ui.shift.startscreen.ShiftStartScreenViewModel
import ru.rutoken.tech.ui.tokenauth.EnterPinViewModel
import ru.rutoken.tech.ui.tokenauth.LoginViewModel
import ru.rutoken.tech.ui.tokenauth.TokenAuthScreen

/**
 * Shift section destinations.
 */
sealed class ShiftDestination(override val route: String) : Destination {
    data object Start : ShiftDestination("shift/start")
    data object UserAddingTokenAuth : ShiftDestination("shift/userAddingTokenAuth")
    data object UserLoginTokenAuth : ShiftDestination("shift/userLoginTokenAuth")
    data object Certificates : ShiftDestination("shift/certificates")
    data object Documents : ShiftDestination("shift/documents")
    data object DocumentsPreview : ShiftDestination("shift/documents/preview")
    data object DocumentsSign : ShiftDestination("shift/documents/sign")
}

fun NavGraphBuilder.addShiftDestinations(
    navController: NavController,
    sessionHolder: AppSessionHolder,
    openDrawer: () -> Unit,
) {
    composable(ShiftDestination.Start) {
        LaunchedEffect(Unit) {
            sessionHolder.resetSession() // Clear session on Shift start screen every time it is opened
        }

        ShiftStartScreen(
            viewModel = koinViewModel<ShiftStartScreenViewModel>(),
            onNavigateToUserLogin = {
                navController.navigate(ShiftDestination.UserLoginTokenAuth.route) { launchSingleTop = true }
            },
            onNavigateToUserAdding = {
                navController.navigate(ShiftDestination.UserAddingTokenAuth.route) { launchSingleTop = true }
            },
            openDrawer = openDrawer
        )
    }

    bottomSheet(ShiftDestination.UserLoginTokenAuth.route) {
        TokenAuthScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            loginViewModel = koinViewModel<LoginViewModel>(),
            appSessionType = AppSessionType.SHIFT_USER_LOGIN_SESSION,
            onAuthDone = {
                navController.navigate(ShiftDestination.Documents.route)
            },
            onNavigateBack = navController::popBackStack
        )
    }

    bottomSheet(ShiftDestination.UserAddingTokenAuth.route) {
        TokenAuthScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            loginViewModel = koinViewModel<LoginViewModel>(),
            appSessionType = AppSessionType.SHIFT_USER_ADDING_SESSION,
            onAuthDone = { navController.navigate(ShiftDestination.Certificates.route) },
            onNavigateBack = navController::popBackStack
        )
    }

    bottomSheet(ShiftDestination.Certificates.route) {
        ChooseNewShiftCertificateScreen(
            viewModel = koinViewModel<ChooseNewShiftCertificateViewModel>(),
            onNavigateToDocumentsScreen = {
                navController.navigate(ShiftDestination.Documents.route)
            },
            onNavigateBack = { navController.popBackStack(ShiftDestination.Start.route, false) }
        )
    }

    composable(ShiftDestination.Documents) {
        DocumentsScreen(
            viewModel = koinViewModel<DocumentsViewModel>(
                parameters = { parametersOf({ navController.navigate(ShiftDestination.DocumentsPreview.route) }) }
            ),
            onNavigateBack = navController::popBackStack,
        )
    }

    composable(ShiftDestination.DocumentsPreview) {
        DocumentsPreviewScreen(
            onNavigateBack = navController::popBackStack,
            onSignClick = { navController.navigate(ShiftDestination.DocumentsSign.route) { launchSingleTop = true } }
        )
    }

    bottomSheet(ShiftDestination.DocumentsSign.route) {
        DocumentsSignScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            signViewModel = koinViewModel<DocumentsSignViewModel>(),
            onNavigateBack = navController::popBackStack
        )
    }
}
