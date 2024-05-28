/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.tokenconnector

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.withContext
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.tech.pkcs11.getSerialNumber
import ru.rutoken.tech.session.SerialHexString
import ru.rutoken.tech.tokenmanager.RtPkcs11TokenData
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.utils.BusinessRuleCase
import ru.rutoken.tech.utils.BusinessRuleException

class TokenConnector {
    private val findTokenJob = Job() + Dispatchers.IO

    private val _showConnectTokenDialog = MutableLiveData<Boolean>()
    val showConnectTokenDialog: LiveData<Boolean> = _showConnectTokenDialog

    @MainThread
    fun onDismissConnectTokenDialog() {
        findTokenJob.cancelChildren()
        _showConnectTokenDialog.value = false
    }

    /**
     * @throws [kotlinx.coroutines.CancellationException] if the [onDismissConnectTokenDialog] method is called before the
     * token is connected.
     */
    suspend fun findFirstToken(tokenManager: TokenManager): RtPkcs11TokenData {
        return withContext(findTokenJob) {
            tokenManager.waitForTokenData()
        }
    }

    /**
     * Tries to get a token by serial number. If it doesn't find it, method waits for the first token to be connected
     * and compares its serial number with [tokenSerial] parameter.
     *
     * @throws kotlinx.coroutines.CancellationException if the [onDismissConnectTokenDialog] method is called before the
     * token is connected.
     * @throws BusinessRuleException if the connected token has a serial number that doesn't match the [tokenSerial]
     * parameter
     */
    suspend fun findTokenBySerialNumber(tokenManager: TokenManager, tokenSerial: SerialHexString): Pkcs11Token {
        return withContext(findTokenJob) {
            tokenManager.getTokenBySerialNumber(tokenSerial)?.let { return@withContext it.token }

            val token = tokenManager.waitForTokenData().token
            return@withContext if (token.getSerialNumber() == tokenSerial) {
                token
            } else {
                throw BusinessRuleException(BusinessRuleCase.WrongRutoken)
            }
        }
    }

    private suspend fun TokenManager.waitForTokenData(): RtPkcs11TokenData {
        try {
            _showConnectTokenDialog.postValue(true)
            return getFirstTokenAsync().await()
        } finally {
            _showConnectTokenDialog.postValue(false)
        }
    }
}
