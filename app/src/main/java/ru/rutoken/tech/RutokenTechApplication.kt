package ru.rutoken.tech

import android.app.Application
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.rutoken.rtpcscbridge.RtPcscBridge
import ru.rutoken.tech.koin.koinModule
import ru.rutoken.tech.pkcs11.Pkcs11Launcher
import ru.rutoken.tech.tokenmanager.TokenManager

class RutokenTechApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RutokenTechApplication)
            modules(koinModule)
        }

        RtPcscBridge.setAppContext(this)
        RtPcscBridge.getTransportExtension().attachToLifecycle(this)

        get<Pkcs11Launcher>().addListener(get<TokenManager>())
    }
}
