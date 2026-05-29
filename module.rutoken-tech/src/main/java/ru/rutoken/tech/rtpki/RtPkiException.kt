/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.rtpki

import ru.rutoken.rtpki.RtPkiResult

class RtPkiException(message: String) : Exception(message) {
    companion object {
        fun throwIfNotOk(function: String, code: Int) {
            if (code != RtPkiResult.RT_PKI_OK)
                throw RtPkiException(function + " failed, error code: 0x" + Integer.toHexString(code))
        }
    }
}
