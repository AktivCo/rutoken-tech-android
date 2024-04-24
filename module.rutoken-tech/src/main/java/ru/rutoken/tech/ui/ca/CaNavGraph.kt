package ru.rutoken.tech.ui.ca

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.material.bottomSheet
import org.koin.androidx.compose.koinViewModel
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
            onNavigateToGenerateKeyPair = { /* TODO: navigate to bottom sheet dialog */ },
            onNavigateToGenerateCertificate = { /* TODO: navigate to bottom sheet dialog */ },
            onLogout = { /* TODO: logout (clear session) */ }
        )
    }
}