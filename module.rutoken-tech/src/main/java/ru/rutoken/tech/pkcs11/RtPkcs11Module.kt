package ru.rutoken.tech.pkcs11

import com.sun.jna.Native
import ru.rutoken.pkcs11jna.RtPkcs11
import ru.rutoken.pkcs11wrapper.main.Pkcs11BaseModule
import ru.rutoken.pkcs11wrapper.rutoken.attribute.RtPkcs11AttributeFactory
import ru.rutoken.pkcs11wrapper.rutoken.lowlevel.jna.RtPkcs11JnaLowLevelApi
import ru.rutoken.pkcs11wrapper.rutoken.lowlevel.jna.RtPkcs11JnaLowLevelFactory
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Api
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11HighLevelFactory

/**
 * Loads native pkcs11 library and initializes pkcs11wrapper.
 * Use this class as entry point to pkcs11wrapper.
 */
class RtPkcs11Module(name: String = "rtpkcs11ecp") : Pkcs11BaseModule(
    RtPkcs11Api(RtPkcs11JnaLowLevelApi(Native.load(name, RtPkcs11::class.java), RtPkcs11JnaLowLevelFactory())),
    RtPkcs11HighLevelFactory(),
    RtPkcs11AttributeFactory()
)
