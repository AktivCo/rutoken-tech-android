/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payments

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.bank.payments.UserActionType.DECRYPT
import ru.rutoken.tech.ui.bank.payments.UserActionType.ENCRYPT
import ru.rutoken.tech.ui.bank.payments.UserActionType.SIGN
import ru.rutoken.tech.ui.bank.payments.UserActionType.VERIFY
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.utils.decoded
import ru.rutoken.tech.utils.toBase64String
import ru.rutoken.tech.utils.toDateTimeString
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

typealias Base64String = String

enum class UserActionType {
    SIGN,
    VERIFY,
    ENCRYPT,
    DECRYPT
}

data class Payment(
    val title: String,
    val date: LocalDate,
    val amount: String,
    val organization: String,
    val userActionType: UserActionType,
    var actionTime: LocalDateTime? = null,
    var initialActionData: Base64String? = null,
    var actionResultData: Base64String? = null
) {
    private val fileName = "$title.pdf"
    private val signedFileName = "$title.sig"
    private val encryptedFileName = "$title.enc"

    @Composable
    fun Icon(isArchived: Boolean) = when (userActionType) {
        SIGN -> AppIcons.PaymentToSign(isArchived)
        VERIFY -> AppIcons.PaymentToVerify(isArchived)
        ENCRYPT -> AppIcons.PaymentToEncrypt(isArchived)
        DECRYPT -> AppIcons.PaymentToDecrypt(isArchived)
    }

    fun isIncoming() = userActionType == VERIFY || userActionType == DECRYPT

    fun isArchived() = actionTime != null

    fun readFile(context: Context) = context.assets.open(fileName).use { it.readBytes() }

    fun getActionData(context: Context): Base64String = initialActionData ?: readFile(context).toBase64String()

    fun getRenderData(context: Context): Base64String = when (userActionType) {
        SIGN, VERIFY -> readFile(context).toBase64String()
        ENCRYPT -> if (isArchived()) actionResultData!! else readFile(context).toBase64String()
        DECRYPT -> if (isArchived()) actionResultData!! else initialActionData!!
    }

    fun getSharedData(context: Context): List<File> {
        val result = mutableListOf<File>()

        when (userActionType) {
            SIGN -> {
                result.add(makeFileInCacheDir(context, fileName, readFile(context)))
                if (isArchived())
                    result.add(makeFileInCacheDir(context, signedFileName, actionResultData!!))
            }

            VERIFY -> {
                result.add(makeFileInCacheDir(context, fileName, readFile(context)))
            }

            ENCRYPT -> {
                if (isArchived())
                    result.add(makeFileInCacheDir(context, encryptedFileName, actionResultData!!))
                else
                    result.add(makeFileInCacheDir(context, fileName, readFile(context)))
            }

            DECRYPT -> {
                if (isArchived())
                    result.add(makeFileInCacheDir(context, fileName, actionResultData!!.decoded))
                else
                    result.add(makeFileInCacheDir(context, encryptedFileName, initialActionData!!))
            }
        }

        return result
    }

    @Composable
    fun getActionButtonText() = when (userActionType) {
        SIGN -> stringResource(R.string.sign)
        VERIFY -> stringResource(R.string.verify)
        ENCRYPT -> stringResource(R.string.encrypt)
        DECRYPT -> stringResource(R.string.decrypt)
    }

    @Composable
    fun getArchivedActionText(): String {
        val actionTimeString = actionTime!!.toDateTimeString()
        return when (userActionType) {
            SIGN -> stringResource(R.string.signed, actionTimeString)
            VERIFY -> stringResource(R.string.verified, actionTimeString)
            ENCRYPT -> stringResource(R.string.encrypted, actionTimeString)
            DECRYPT -> stringResource(R.string.decrypted, actionTimeString)
        }
    }

    fun hasEncryptedRenderData() =
        userActionType == DECRYPT && !isArchived() || userActionType == ENCRYPT && isArchived()
}

private fun makeFileInCacheDir(context: Context, fileName: String, fileContent: ByteArray) =
    File(context.cacheDir, "/$fileName").also { it.writeBytes(fileContent) }

private fun makeFileInCacheDir(context: Context, fileName: String, fileContent: Base64String) =
    File(context.cacheDir, "/$fileName").also { it.writeText(fileContent) }