package ru.rutoken.tech.ui.ca

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.rutoken.tech.ui.ca.tokeninfo.CaTokenInfoScreen
import ru.rutoken.tech.ui.ca.tokeninfo.CaTokenInfoViewModel
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.main.Destination
import ru.rutoken.tech.ui.main.composable
import ru.rutoken.tech.ui.main.navigateSingleTopTo
import ru.rutoken.tech.utils.logd

/**
 * CA section destinations.
 */
sealed class CaDestination(override val route: String) : Destination {
    data object Start : CaDestination("ca/start")
    data object TokenInfo : CaDestination("ca/tokenInfo")
}

fun NavGraphBuilder.addCaDestinations(navController: NavController, caLoginViewModel: CaLoginViewModel) {
    composable(CaDestination.Start) {
        // TODO: replace the following code with pin dialog
        val coroutineScope = rememberCoroutineScope()
        val isLoading by caLoginViewModel.isLoading.observeAsState(false)
        if (isLoading) {
            ProgressIndicatorDialog()
        }

        CaStartScreen(
            onClickConnectToken = {
                coroutineScope.launch {
                    // TODO: remove this in issue #44
                    caLoginViewModel.login("12345678").onSuccess {
                        navController.navigateSingleTopTo(CaDestination.TokenInfo, popUpToInclusive = true)
                    }.onFailure { e ->
                        // TODO: show error dialog
                        logd<CaLoginViewModel>(e) { "Failed to login to token" }
                    }
                }
            }
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