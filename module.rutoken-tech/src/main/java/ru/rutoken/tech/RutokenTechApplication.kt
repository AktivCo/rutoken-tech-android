/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech

import android.app.Application
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.rutoken.rtpcscbridge.RtPcscBridge
import ru.rutoken.tech.koin.koinModule
import ru.rutoken.tech.pkcs11.Pkcs11Launcher
import ru.rutoken.tech.tokenmanager.TokenManager
import java.security.Security

class RutokenTechApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@RutokenTechApplication)
            modules(koinModule)
        }

        val bcProvider = BouncyCastleProvider()
        // Remove system Bouncy Castle provider
        Security.removeProvider(bcProvider.name)
        Security.insertProviderAt(bcProvider, 1)

        RtPcscBridge.setAppContext(this)
        RtPcscBridge.getTransportExtension().attachToLifecycle(this)

        get<Pkcs11Launcher>().addListener(get<TokenManager>())
    }
}
