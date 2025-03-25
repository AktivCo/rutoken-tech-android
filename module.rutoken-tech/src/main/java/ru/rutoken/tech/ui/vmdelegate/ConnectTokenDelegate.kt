/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.vmdelegate

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rutoken.tech.pkcs11.getSerialNumber
import ru.rutoken.tech.session.SerialHexString
import ru.rutoken.tech.tokenmanager.RtPkcs11TokenData
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.utils.BusinessRuleCase
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.SingleThreadCoroutineDispatcherWrapper

class ConnectTokenDelegate(override val delegateScope: CoroutineScope) : ViewModelDelegate {
    private var dispatcher = SingleThreadCoroutineDispatcherWrapper()

    private val _showConnectTokenDialog = MutableLiveData<Boolean>()
    val showConnectTokenDialog: LiveData<Boolean> = _showConnectTokenDialog

    @MainThread
    fun onDismissConnectTokenDialog() {
        delegateScope.launch {
            dispatcher.closeAndWaitForTerminating()
            dispatcher = SingleThreadCoroutineDispatcherWrapper()
            _showConnectTokenDialog.value = false
        }
    }

    /**
     * @throws [kotlinx.coroutines.CancellationException] if the [onDismissConnectTokenDialog] method is called
     * before the token is connected.
     */
    suspend fun findFirstToken(tokenManager: TokenManager): RtPkcs11TokenData =
        withContext(dispatcher.context) { tokenManager.waitForTokenData() }

    /**
     * Tries to get a token by serial number. If it doesn't find it, method waits for the first token to be connected
     * and compares its serial number with [tokenSerial] parameter.
     *
     * @throws kotlinx.coroutines.CancellationException if the [onDismissConnectTokenDialog] method is called before the
     * token is connected.
     * @throws BusinessRuleException if the connected token has a serial number that doesn't match the [tokenSerial]
     * parameter
     */
    suspend fun findTokenBySerialNumber(tokenManager: TokenManager, tokenSerial: SerialHexString): RtPkcs11TokenData {
        return withContext(dispatcher.context) {
            tokenManager.getTokenBySerialNumber(tokenSerial)?.let { return@withContext it }

            val tokenData = tokenManager.waitForTokenData()
            return@withContext if (tokenData.token.getSerialNumber() == tokenSerial) {
                tokenData
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
