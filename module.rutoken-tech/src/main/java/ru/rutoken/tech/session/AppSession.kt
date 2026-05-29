/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.session

import android.content.Context
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.pkcs11wrapper.rutoken.constant.RtPkcs11MechanismType
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_GOST28147
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_KUZNECHIK_CTR_ACPKM_OMAC
import ru.rutoken.rtpki.RtPkiCmsCipherAlgorithm.RT_PKI_CMS_CIPHER_MAGMA_CTR_ACPKM_OMAC
import ru.rutoken.tech.bank.biometry.canUseBiometry
import ru.rutoken.tech.rtpki.RtPkiEncryptionAlgorithm
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
    val keyPairs: MutableList<CkaID>,
) : AppSession()

data class BankUserAddingAppSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val certificates: List<Certificate>,
    val encryptionAlgorithm: BankEncryptionAlgorithm,
) : AppSession()

class BankUserLoginAppSession(
    val userId: Int,
    val tokenSerial: SerialHexString,
    val encryptionAlgorithm: BankEncryptionAlgorithm,
    val certificateCkaId: ByteArray,
    val certificate: ByteArray,
    val isBiometryActive: Boolean,
    var encryptedPinData: EncryptedPinData?,
    var payments: List<Payment>,
    var operationWithToken: (suspend (RtPkcs11Session) -> Unit)? = null,
) : AppSession() {
    fun hasPinToDecrypt(context: Context): Boolean {
        if (!isBiometryActive || encryptedPinData == null)
            return false

        return context.canUseBiometry()
    }

    class EncryptedPinData(val bytes: ByteArray, val cipherIv: ByteArray)
}

enum class BankEncryptionAlgorithm {
    GOST_28147_89,
    GOST_34_12_2018_MAGMA,
    GOST_34_12_2018_KUZNECHIK;

    @RtPkiEncryptionAlgorithm
    fun toRtPkiEncryptionAlgorithm(): Int {
        return when (this) {
            GOST_28147_89 -> RT_PKI_CMS_CIPHER_GOST28147
            GOST_34_12_2018_MAGMA -> RT_PKI_CMS_CIPHER_MAGMA_CTR_ACPKM_OMAC
            GOST_34_12_2018_KUZNECHIK -> RT_PKI_CMS_CIPHER_KUZNECHIK_CTR_ACPKM_OMAC
        }
    }

    companion object {
        fun fromSupportedMechanisms(token: Pkcs11Token): BankEncryptionAlgorithm {
            // Check that the token can derive keys of the CKK_KUZNYECHIK_TWIN_KEY and CKK_MAGMA_TWIN_KEY types.
            return if (token.mechanismList.any { it.asLong == RtPkcs11MechanismType.CKM_GOST_KEG.asLong }) {
                /*
                 * By default, GOST 34.12-2018 "Magma" is used. You can change the algorithm to GOST 34.12-2018
                 * "Kuznechik" or GOST 28147-89.
                 */
                GOST_34_12_2018_MAGMA
            } else {
                // GOST 28147-89 is supported by the entire Rutoken product line.
                GOST_28147_89
            }
        }
    }
}

data class ShiftUserAddingAppSession(
    val tokenUserPin: String,
    val tokenSerial: SerialHexString,
    val certificates: List<Certificate>,
) : AppSession()

class ShiftUserLoginAppSession(
    val userId: Int,
    val tokenSerial: SerialHexString,
    val certificateCkaId: ByteArray,
    val certificate: ByteArray,
    var documents: List<Document>,
    var signedDocuments: List<SignedDocumentsGroup>,
    var chosenDocuments: DocumentsPreviewInfo = DocumentsPreviewInfo(),
) : AppSession()

class DocumentsPreviewInfo(val documents: List<Document> = emptyList(), val startDocument: Int = 0)
