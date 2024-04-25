package ru.rutoken.tech.ui.about

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import ru.rutoken.tech.session.RutokenTechSessionHolder
import ru.rutoken.tech.ui.main.Destination
import ru.rutoken.tech.ui.main.composable

/**
 * About section destinations.
 */
sealed class AboutDestination(override val route: String) : Destination {
    data object About : AboutDestination("about/about")
}

fun NavGraphBuilder.addAboutDestinations(sessionHolder: RutokenTechSessionHolder, openDrawer: () -> Unit) {
    composable(AboutDestination.About) {
        LaunchedEffect(Unit) {
            sessionHolder.resetSession() // Clear session on about screen every time it is opened
        }

        AboutScreen(openDrawer)
    }
}