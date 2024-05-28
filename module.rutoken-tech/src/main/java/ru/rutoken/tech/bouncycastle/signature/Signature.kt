/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.signature

import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11MechanismType.CKM_GOSTR3410
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.tech.bouncycastle.digest.Gost256Digest
import ru.rutoken.tech.bouncycastle.digest.Pkcs11Digest
import ru.rutoken.tech.pkcs11.GostOids.GOSTR3411_2012_256_OID

sealed class Signature(protected val session: Pkcs11Session) {
    private var privateKey: Pkcs11GostPrivateKeyObject? = null

    abstract fun sign(data: ByteArray): ByteArray

    abstract fun makeDigest(): Pkcs11Digest

    fun signInit(key: Pkcs11GostPrivateKeyObject) {
        privateKey = key
    }

    fun innerSign(mechanism: Pkcs11Mechanism, data: ByteArray): ByteArray =
        session.signManager.signAtOnce(data, mechanism, privateKey)
}

class Gost256Signature(session: Pkcs11Session) : Signature(session) {
    override fun sign(data: ByteArray) = innerSign(Pkcs11Mechanism.make(CKM_GOSTR3410), data)

    override fun makeDigest() = Gost256Digest(session)
}

fun makeSignatureByHashOid(hashOid: ByteArray, session: Pkcs11Session) = when {
    hashOid.contentEquals(GOSTR3411_2012_256_OID) -> Gost256Signature(session)
    else -> throw IllegalStateException("Unsupported hash OID: " + hashOid.joinToString("") { "%02x".format(it) })
}
