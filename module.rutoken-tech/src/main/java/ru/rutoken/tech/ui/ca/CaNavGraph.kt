/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.material.bottomSheet
import org.koin.androidx.compose.koinViewModel
import ru.rutoken.tech.session.RutokenTechSessionHolder
import ru.rutoken.tech.ui.ca.generateobjects.certificate.GenerateCertificateScreen
import ru.rutoken.tech.ui.ca.generateobjects.certificate.GenerateCertificateViewModel
import ru.rutoken.tech.ui.ca.generateobjects.keypair.GenerateKeyPairScreen
import ru.rutoken.tech.ui.ca.generateobjects.keypair.GenerateKeyPairViewModel
import ru.rutoken.tech.ui.ca.tokeninfo.CaTokenInfoScreen
import ru.rutoken.tech.ui.ca.tokeninfo.CaTokenInfoViewModel
import ru.rutoken.tech.ui.main.Destination
import ru.rutoken.tech.ui.main.composable
import ru.rutoken.tech.ui.tokenauth.EnterPinViewModel
import ru.rutoken.tech.ui.tokenauth.TokenAuthScreen

/**
 * CA section destinations.
 */
sealed class CaDestination(override val route: String) : Destination {
    data object Start : CaDestination("ca/start")
    data object TokenAuth : CaDestination("ca/tokenAuth")
    data object TokenInfo : CaDestination("ca/tokenInfo")
    data object KeyPairGeneration : CaDestination("ca/keyPairGeneration")
    data object CertificateGeneration : CaDestination("ca/certificateGeneration")
}

fun NavGraphBuilder.addCaDestinations(
    navController: NavController,
    sessionHolder: RutokenTechSessionHolder,
    openDrawer: () -> Unit
) {
    composable(CaDestination.Start) {
        LaunchedEffect(Unit) {
            sessionHolder.resetSession() // Clear session on CA start screen every time it is opened
        }
        CaStartScreen(
            onClickConnectToken = { navController.navigate(CaDestination.TokenAuth.route) { launchSingleTop = true } },
            openDrawer = openDrawer
        )
    }

    bottomSheet(CaDestination.TokenAuth.route) {
        TokenAuthScreen(
            enterPinViewModel = koinViewModel<EnterPinViewModel>(),
            caLoginViewModel = koinViewModel<CaLoginViewModel>(),
            onNavigateToTokenInfo = { navController.navigate(CaDestination.TokenInfo.route) },
            onNavigateBack = navController::popBackStack
        )
    }

    composable(CaDestination.TokenInfo) {
        CaTokenInfoScreen(
            viewModel = koinViewModel<CaTokenInfoViewModel>(),
            onNavigateToGenerateKeyPair = {
                navController.navigate(CaDestination.KeyPairGeneration.route) { launchSingleTop = true }
            },
            onNavigateToGenerateCertificate = {
                navController.navigate(CaDestination.CertificateGeneration.route) { launchSingleTop = true }
            },
            onLogout = { navController.popBackStack() },
            openDrawer = openDrawer
        )
    }

    bottomSheet(CaDestination.KeyPairGeneration.route) {
        GenerateKeyPairScreen(
            viewModel = koinViewModel<GenerateKeyPairViewModel>(),
            onNavigateBack = navController::popBackStack,
            onLogout = { navController.popBackStack(CaDestination.Start.route, false) }
        )
    }

    bottomSheet(CaDestination.CertificateGeneration.route) {
        GenerateCertificateScreen(
            viewModel = koinViewModel<GenerateCertificateViewModel>(),
            onNavigateBack = navController::popBackStack,
            onLogout = { navController.popBackStack(CaDestination.Start.route, false) }
        )
    }
}
