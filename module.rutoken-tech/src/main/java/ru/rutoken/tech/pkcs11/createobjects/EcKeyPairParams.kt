/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11.createobjects

import ru.rutoken.pkcs11wrapper.constant.IPkcs11KeyType
import ru.rutoken.pkcs11wrapper.constant.IPkcs11MechanismType
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11KeyType
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11MechanismType

enum class EcKeyPairParams(
    val curveName: String,
    val ecParams: ByteArray,
    val mechanismType: IPkcs11MechanismType = Pkcs11MechanismType.CKM_EC_KEY_PAIR_GEN,
    val keyType: IPkcs11KeyType = Pkcs11KeyType.CKK_EC,
) {
    /* OID 1.2.840.10045.3.1.7. */
    SECP256R1(
        "secp256r1",
        byteArrayOf(0x06, 0x08, 0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x03, 0x01, 0x07)
    ),
}
