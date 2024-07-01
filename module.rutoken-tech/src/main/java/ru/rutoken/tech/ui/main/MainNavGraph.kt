/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import org.koin.compose.koinInject
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.ui.about.AboutDestination
import ru.rutoken.tech.ui.about.addAboutDestinations
import ru.rutoken.tech.ui.bank.BankDestination
import ru.rutoken.tech.ui.bank.addBankDestinations
import ru.rutoken.tech.ui.ca.CaDestination
import ru.rutoken.tech.ui.ca.addCaDestinations

interface Destination {
    val route: String
}

/**
 * Destinations for application sections.
 */
sealed class AppSectionDestination(override val route: String) : Destination {
    data object Bank : AppSectionDestination("bank")
    data object Ca : AppSectionDestination("ca")
    data object About : AppSectionDestination("about")
}

@Composable
fun MainNavHost(navHostController: NavHostController, openDrawer: () -> Unit) {
    val session: AppSessionHolder = koinInject()

    NavHost(
        navController = navHostController,
        startDestination = AppSectionDestination.Bank.route,
        enterTransition = { slideInHorizontally(animationSpec = tween(500), initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(animationSpec = tween(500), targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(500), initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(500), targetOffsetX = { it }) }
    ) {
        navigation(
            route = AppSectionDestination.Bank.route,
            startDestination = BankDestination.Start.route
        ) {
            addBankDestinations(navHostController, session, openDrawer)
        }

        navigation(
            route = AppSectionDestination.Ca.route,
            startDestination = CaDestination.Start.route
        ) {
            addCaDestinations(navHostController, session, openDrawer)
        }

        navigation(
            route = AppSectionDestination.About.route,
            startDestination = AboutDestination.About.route
        ) {
            addAboutDestinations(session, openDrawer)
        }
    }
}

fun NavGraphBuilder.composable(
    destination: Destination,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.route,
        arguments = arguments,
        content = content
    )
}

fun NavController.navigateSingleTopTo(
    destination: Destination,
    popUpToInclusive: Boolean = false
) = navigateSingleTopTo(destination.route, popUpToInclusive)

fun NavController.navigateSingleTopTo(route: String, popUpToInclusive: Boolean) =
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        val startDestination = this@navigateSingleTopTo.graph.findStartDestination()

        popUpTo(startDestination.id) {
            inclusive = popUpToInclusive
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }