package ru.rutoken.tech.ui.main

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.android.ext.android.get
import ru.rutoken.tech.pkcs11.Pkcs11Launcher
import ru.rutoken.tech.ui.ca.CaStartScreen
import ru.rutoken.tech.ui.theme.RutokenTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(get<Pkcs11Launcher>())

        expandAppColorsToSystemBars()
        setContent {
            RutokenTechTheme {
                CaStartScreen()
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
