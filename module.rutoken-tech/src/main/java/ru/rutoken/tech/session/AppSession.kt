/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.session

import android.content.Context
import ru.rutoken.tech.bank.biometry.canUseBiometry
import ru.rutoken.tech.ui.bank.BankCertificate
import ru.rutoken.tech.ui.bank.payments.Payment
import ru.rutoken.tech.ui.ca.tokeninfo.model.TokenModel

typealias SerialHexString = String
typealias CkaIdString = String

enum class AppSessionType {
    CA_SESSION,
    BANK_USER_ADDING_SESSION,
    BANK_USER_LOGIN_SESSION
}

abstract class AppSession

data class CaAppSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val tokenModel: TokenModel,
    val tokenLabel: String,
    val keyPairs: MutableList<CkaIdString>
) : AppSession()

data class BankUserAddingAppSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val certificates: List<BankCertificate>
) : AppSession()

class BankUserLoginAppSession(
    val userId: Int,
    val tokenSerial: SerialHexString,
    val certificateCkaId: ByteArray,
    val certificate: ByteArray,
    val isBiometryActive: Boolean,
    var encryptedPinData: EncryptedPinData?,
    var payments: List<Payment>
) : AppSession() {
    fun hasPinToDecrypt(context: Context): Boolean {
        if (!isBiometryActive || encryptedPinData == null)
            return false

        return context.canUseBiometry()
    }

    class EncryptedPinData(val bytes: ByteArray, val cipherIv: ByteArray)
}
