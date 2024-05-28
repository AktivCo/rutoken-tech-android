package ru.rutoken.tech.ui.main

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import ru.rutoken.tech.pkcs11.Pkcs11Launcher
import ru.rutoken.tech.ui.about.AboutDestination
import ru.rutoken.tech.ui.ca.CaDestination
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.clearNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        expandAppColorsToSystemBars()
        lifecycle.addObserver(get<Pkcs11Launcher>())

        setContent {
            RutokenTechTheme {
                RootContent()
            }
        }
    }

    @Composable
    private fun RootContent() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navController = rememberNavController(bottomSheetNavigator)

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route
            ?: CaDestination.Start.route // TODO: change to Bank start screen

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val openDrawer = { scope.launch { drawerState.open() } }
        val closeDrawer = { scope.launch { drawerState.close() } }

        if (drawerState.isOpen) {
            BackHandler { closeDrawer() }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerState) {
                    MainDrawerContent(
                        currentDestination = currentDestination,
                        onCloseDrawer = { closeDrawer() },
                        onNavigateToCa = {
                            navController.navigate(CaDestination.Start.route) { clearNavGraph(navController) }
                        },
                        onNavigateToAbout = {
                            navController.navigate(AboutDestination.About.route) {
                                clearNavGraph(navController)
                            }
                        }
                    )
                }
            },
        ) {
            ModalBottomSheetLayout(bottomSheetNavigator) {
                MainNavHost(
                    navHostController = navController,
                    openDrawer = { openDrawer() }
                )
            }
        }
    }

    /**
     * We take this code from the [SystemBarStyle.auto] method, but we need to strictly set
     * isNavigationBarContrastEnforced to false so that we can't use the default implementation.
     */
    private fun expandAppColorsToSystemBars() {
        val navigationBarStyle = if ((resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(navigationBarStyle = navigationBarStyle)
    }
}
