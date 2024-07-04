/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.utils

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ReturnValue.CKR_PIN_INCORRECT
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ReturnValue.CKR_PIN_LEN_RANGE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ReturnValue.CKR_PIN_LOCKED
import ru.rutoken.pkcs11wrapper.main.Pkcs11Exception
import ru.rutoken.tech.R
import ru.rutoken.tech.session.SerialHexString
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.utils.BusinessRuleCase.IncorrectPin
import ru.rutoken.tech.utils.BusinessRuleCase.PinLocked
import ru.rutoken.tech.utils.BusinessRuleCase.TokenRemoved
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.toDateString
import java.util.Date

suspend fun callPkcs11Operation(
    showProgress: MutableLiveData<Boolean>,
    tokenManager: TokenManager,
    tokenSerial: SerialHexString,
    block: suspend () -> Unit
) {
    try {
        showProgress.postValue(true)
        block()
    } catch (e: Pkcs11Exception) {
        val userRetryCountLeft = try {
            tokenManager.getTokenBySerialNumber(tokenSerial)?.token?.tokenInfoExtended?.userRetryCountLeft
        } catch (_: Throwable) {
            null
        }

        throw e.asBusinessRuleExceptionOrNull(tokenManager, tokenSerial, userRetryCountLeft) ?: e
    } finally {
        showProgress.postValue(false)
    }
}

private suspend fun checkTokenRemoved(tokenManager: TokenManager, tokenSerial: SerialHexString): Boolean {
    val isTokenRemoved = withTimeoutOrNull(200L) {
        while (true) {
            delay(50L)
            tokenManager.getTokenBySerialNumber(tokenSerial) ?: return@withTimeoutOrNull true
        }
    }

    return isTokenRemoved == true
}

private suspend fun Pkcs11Exception.asBusinessRuleExceptionOrNull(
    tokenManager: TokenManager,
    tokenSerial: SerialHexString,
    retryCountLeft: Long?
): BusinessRuleException? {
    if (checkTokenRemoved(tokenManager, tokenSerial)) return BusinessRuleException(TokenRemoved)

    val businessRuleCase = when (code) {
        CKR_PIN_INCORRECT, CKR_PIN_LEN_RANGE -> {
            if (retryCountLeft!! != 0L) IncorrectPin(retryCountLeft) else PinLocked
        }

        CKR_PIN_LOCKED -> PinLocked
        else -> null
    }

    return if (businessRuleCase != null) BusinessRuleException(businessRuleCase, this) else null
}

fun Context.getCertificateErrorText(certificateNotBefore: Date, certificateNotAfter: Date): String? {
    val currentDate = Date()
    return if (currentDate.after(certificateNotAfter))
        getString(R.string.certificate_is_expired)
    else if (certificateNotBefore.after(currentDate))
        getString(R.string.certificate_not_yet_valid, certificateNotBefore.toDateString())
    else
        null
}