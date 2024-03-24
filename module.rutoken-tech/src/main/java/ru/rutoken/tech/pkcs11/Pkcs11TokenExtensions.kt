package ru.rutoken.tech.pkcs11

import androidx.annotation.WorkerThread
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11TokenInfo
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Token
import ru.rutoken.tech.session.SerialHexString
import ru.rutoken.tech.ui.ca.tokeninfo.model.TokenModel
import ru.rutoken.tech.ui.ca.tokeninfo.model.defineTokenModel

val Pkcs11TokenInfo.serialNumberTrimmed: SerialHexString
    get() = serialNumber.trimEnd()

@WorkerThread
fun Pkcs11Token.getSerialNumber(): SerialHexString = tokenInfo.serialNumberTrimmed

suspend fun Pkcs11Token.getTokenModel(): TokenModel {
    return this.openSession(false).use {
        defineTokenModel(it, tokenInfo, (this as RtPkcs11Token).tokenInfoExtended)
    }
}