/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.digest

import org.bouncycastle.crypto.Digest
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.mechanism.parameter.Pkcs11ByteArrayMechanismParams
import ru.rutoken.pkcs11wrapper.rutoken.constant.RtPkcs11MechanismType.CKM_GOSTR3411_12_256
import ru.rutoken.tech.pkcs11.GostOids.GOSTR3411_2012_256_OID

sealed class Pkcs11Digest(private val session: Pkcs11Session) : Digest {
    abstract val mechanism: Pkcs11Mechanism

    private var isOperationInitialized = false

    override fun update(input: Byte) = update(byteArrayOf(input))

    override fun update(input: ByteArray, inOff: Int, len: Int) = update(input.copyOfRange(inOff, inOff + len))

    private fun update(chunk: ByteArray) {
        if (!isOperationInitialized) {
            session.digestManager.digestInit(mechanism)
            isOperationInitialized = true
        }

        session.digestManager.digestUpdate(chunk)
    }

    override fun doFinal(out: ByteArray, outOff: Int): Int {
        val digest = session.digestManager.digestFinal().apply { copyInto(out, outOff) }
        isOperationInitialized = false
        return digest.size
    }

    override fun reset() {
        if (isOperationInitialized)
            doFinal(ByteArray(digestSize), 0)
    }
}

class Gost256Digest(session: Pkcs11Session) : Pkcs11Digest(session) {
    override val mechanism: Pkcs11Mechanism =
        Pkcs11Mechanism.make(CKM_GOSTR3411_12_256, Pkcs11ByteArrayMechanismParams(GOSTR3411_2012_256_OID))

    override fun getAlgorithmName() = "PKCS11-GOSTR3411-2012-256"

    override fun getDigestSize() = 32
}
