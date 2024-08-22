/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.rule

import com.sun.jna.Native
import kotlinx.coroutines.runBlocking
import org.junit.rules.ExternalResource
import ru.rutoken.pkcs11jna.Pkcs11
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11InitializeArgs
import ru.rutoken.pkcs11wrapper.lowlevel.jna.Pkcs11JnaLowLevelApi
import ru.rutoken.pkcs11wrapper.lowlevel.jna.Pkcs11JnaLowLevelFactory
import ru.rutoken.pkcs11wrapper.main.IPkcs11Module
import ru.rutoken.pkcs11wrapper.main.Pkcs11Api
import ru.rutoken.pkcs11wrapper.main.Pkcs11BaseModule
import ru.rutoken.tech.pkcs11.Pkcs11CallScope.closePkcs11CallContext
import ru.rutoken.tech.pkcs11.Pkcs11CallScope.initPkcs11CallContext

private val pkcs11Module = Module("rtpkcs11ecp")

open class ModuleRule : ExternalResource() {
    open val value: IPkcs11Module = pkcs11Module

    override fun before() {
        value.initializeModule(Pkcs11InitializeArgs.Builder().setOsLockingOk(true).build())
        initPkcs11CallContext()
    }

    override fun after() {
        runBlocking { closePkcs11CallContext() }
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
