/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.rtpki

import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import ru.rutoken.pkcs11jna.RtPkcs11
import ru.rutoken.rtpki.RtPki

object RtPkiLibrary {
    val INSTANCE: RtPki by lazy {
        NativeLibrary.getInstance("crypto")
        NativeLibrary.getInstance("rtengine")
        Native.load("rtpki", RtPki::class.java)
    }
}
