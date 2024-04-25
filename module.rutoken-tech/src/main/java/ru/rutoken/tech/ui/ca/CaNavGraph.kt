package ru.rutoken.tech.ui.ca

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.material.bottomSheet
import org.koin.androidx.compose.koinViewModel
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
}

fun NavGraphBuilder.addCaDestinations(navController: NavController) {
    composable(CaDestination.Start) {
        CaStartScreen(
            onClickConnectToken = { navController.navigate(CaDestination.TokenAuth.route) { launchSingleTop = true } }
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
            onNavigateToGenerateKeyPair = { navController.navigate(CaDestination.KeyPairGeneration.route) },
            onNavigateToGenerateCertificate = { /* TODO: navigate to bottom sheet dialog */ },
            onLogout = { navController.popBackStack()/* TODO: logout (clear session) */ }
        )
    }

    bottomSheet(CaDestination.KeyPairGeneration.route) {
        GenerateKeyPairScreen(
            viewModel = koinViewModel<GenerateKeyPairViewModel>(),
            onNavigateBack = navController::popBackStack,
            onLogout = { navController.popBackStack(CaDestination.Start.route, false) }
        )
    }
}
