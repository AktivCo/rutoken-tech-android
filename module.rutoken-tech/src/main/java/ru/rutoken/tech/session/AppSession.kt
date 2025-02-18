/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.session

import android.content.Context
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.bank.biometry.canUseBiometry
import ru.rutoken.tech.ui.Certificate
import ru.rutoken.tech.ui.bank.payments.Payment
import ru.rutoken.tech.ui.ca.generateobjects.keypair.CkaID
import ru.rutoken.tech.ui.ca.tokeninfo.model.TokenModel
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.ui.shift.documents.SignedDocumentsGroup

typealias SerialHexString = String

enum class AppSessionType {
    CA_SESSION,
    BANK_USER_ADDING_SESSION,
    BANK_USER_LOGIN_SESSION,
    SHIFT_USER_ADDING_SESSION,
    SHIFT_USER_LOGIN_SESSION
}

abstract class AppSession

data class CaAppSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val tokenModel: TokenModel,
    val tokenLabel: String,
    val keyPairs: MutableList<CkaID>
) : AppSession()

data class BankUserAddingAppSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val certificates: List<Certificate>
) : AppSession()

class BankUserLoginAppSession(
    val userId: Int,
    val tokenSerial: SerialHexString,
    val certificateCkaId: ByteArray,
    val certificate: ByteArray,
    val isBiometryActive: Boolean,
    var encryptedPinData: EncryptedPinData?,
    var payments: List<Payment>,
    var operationWithToken: (suspend (RtPkcs11Session) -> Unit)? = null
) : AppSession() {
    fun hasPinToDecrypt(context: Context): Boolean {
        if (!isBiometryActive || encryptedPinData == null)
            return false

        return context.canUseBiometry()
    }

    class EncryptedPinData(val bytes: ByteArray, val cipherIv: ByteArray)
}

data class ShiftUserAddingAppSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val certificates: List<Certificate>
) : AppSession()

class ShiftUserLoginAppSession(
    val userId: Int,
    val tokenSerial: SerialHexString,
    val certificateCkaId: ByteArray,
    val certificate: ByteArray,
    var documents: List<Document>,
    var signedDocuments: List<SignedDocumentsGroup>,
    var operationWithToken: (suspend (RtPkcs11Session) -> Unit)? = null,
    var documentsToSign: Set<Document> = emptySet()
) : AppSession()
