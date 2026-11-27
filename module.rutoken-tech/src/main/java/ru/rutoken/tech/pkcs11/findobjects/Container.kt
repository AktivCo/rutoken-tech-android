/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11.findobjects

import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11KeyPair
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11Gost256PrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11Gost256PublicKeyObject
import ru.rutoken.tech.pkcs11.createobjects.EcKeyPair

sealed class Container(val ckaId: ByteArray)

sealed class CertificateAndKeyContainer(ckaId: ByteArray, val certificate: X509CertificateHolder) : Container(ckaId)

class Gost256CertificateAndKeyContainer(
    ckaId: ByteArray,
    certificate: X509CertificateHolder,
    val keyPair: Pkcs11KeyPair<Pkcs11Gost256PublicKeyObject, Pkcs11Gost256PrivateKeyObject>,
) : CertificateAndKeyContainer(ckaId, certificate)

class EcCertificateAndKeyContainer(
    ckaId: ByteArray,
    certificate: X509CertificateHolder,
    val keyPair: EcKeyPair,
) : CertificateAndKeyContainer(ckaId, certificate)

class Gost256KeyContainer(
    ckaId: ByteArray,
    val keyPair: Pkcs11KeyPair<Pkcs11Gost256PublicKeyObject, Pkcs11Gost256PrivateKeyObject>,
) : Container(ckaId)