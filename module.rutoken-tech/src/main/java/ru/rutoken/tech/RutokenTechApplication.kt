package ru.rutoken.tech

import android.app.Application
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.rutoken.rtpcscbridge.RtPcscBridge
import ru.rutoken.tech.bouncycastle.Gost2012KeyFactorySpi
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
        bcProvider.addKeyInfoConverter(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256, Gost2012KeyFactorySpi())
        // Remove system Bouncy Castle provider
        Security.removeProvider(bcProvider.name)
        Security.insertProviderAt(bcProvider, 1)

        RtPcscBridge.setAppContext(this)
        RtPcscBridge.getTransportExtension().attachToLifecycle(this)

        get<Pkcs11Launcher>().addListener(get<TokenManager>())
    }
}
