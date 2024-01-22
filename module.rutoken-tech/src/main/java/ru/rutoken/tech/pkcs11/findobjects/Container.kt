package ru.rutoken.tech.pkcs11.findobjects

import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11KeyPair
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11Gost256PrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11Gost256PublicKeyObject

sealed class Container(val ckaId: ByteArray)

class Gost256CertificateAndKeyContainer(
    ckaId: ByteArray,
    val certificate: X509CertificateHolder,
    val keyPair: Pkcs11KeyPair<Pkcs11Gost256PublicKeyObject, Pkcs11Gost256PrivateKeyObject>
) : Container(ckaId)

class Gost256KeyContainer(
    ckaId: ByteArray,
    val keyPair: Pkcs11KeyPair<Pkcs11Gost256PublicKeyObject, Pkcs11Gost256PrivateKeyObject>
) : Container(ckaId)