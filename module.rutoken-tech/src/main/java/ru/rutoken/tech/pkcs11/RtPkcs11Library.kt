/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11

import com.sun.jna.Native
import ru.rutoken.pkcs11jna.RtPkcs11

object RtPkcs11Library {
    val INSTANCE: RtPkcs11 by lazy { Native.load("rtpkcs11ecp", RtPkcs11::class.java) }
}
