package ru.rutoken.tech.usecasestests.rule

import com.sun.jna.Native
import org.junit.rules.ExternalResource
import ru.rutoken.pkcs11jna.Pkcs11
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11InitializeArgs
import ru.rutoken.pkcs11wrapper.lowlevel.jna.Pkcs11JnaLowLevelApi
import ru.rutoken.pkcs11wrapper.lowlevel.jna.Pkcs11JnaLowLevelFactory
import ru.rutoken.pkcs11wrapper.main.IPkcs11Module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Api
import ru.rutoken.pkcs11wrapper.main.Pkcs11BaseModule

private val pkcs11Module = Module("rtpkcs11ecp")

open class ModuleRule : ExternalResource() {
    open val value: IPkcs11Module = pkcs11Module

    override fun before() {
        value.initializeModule(Pkcs11InitializeArgs.Builder().setOsLockingOk(true).build())
    }

    override fun after() {
        value.finalizeModule()
    }
}

private class Module(name: String) : Pkcs11BaseModule(
    Pkcs11Api(
        Pkcs11JnaLowLevelApi(
            Native.load(name, Pkcs11::class.java),
            Pkcs11JnaLowLevelFactory.Builder().build()
        )
    )
)
