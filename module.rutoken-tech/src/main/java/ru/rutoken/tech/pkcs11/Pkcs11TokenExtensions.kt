/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11

import ru.rutoken.pkcs11wrapper.datatype.Pkcs11TokenInfo
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Token
import ru.rutoken.tech.pkcs11.Pkcs11CallScope.withPkcs11CallContext
import ru.rutoken.tech.session.SerialHexString
import ru.rutoken.tech.ui.ca.tokeninfo.model.TokenModel
import ru.rutoken.tech.ui.ca.tokeninfo.model.defineTokenModel

val Pkcs11TokenInfo.serialNumberTrimmed: SerialHexString
    get() = serialNumber.trimEnd()

suspend fun Pkcs11Token.getSerialNumber(): SerialHexString =
    withPkcs11CallContext { tokenInfo.serialNumberTrimmed }

suspend fun Pkcs11Token.getTokenModel(): TokenModel =
    withPkcs11CallContext {
        openSession(false).use {
            defineTokenModel(it, tokenInfo, (this@getTokenModel as RtPkcs11Token).tokenInfoExtended)
        }
    }
