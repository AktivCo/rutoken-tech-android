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
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.utils.BusinessRuleCase.WRONG_RUTOKEN
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
    suspend fun findFirstToken(tokenManager: TokenManager): Pkcs11Token {
        return withContext(findTokenJob) {
            tokenManager.waitForToken()
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
            tokenManager.getTokenBySerialNumber(tokenSerial)?.let { return@withContext it.pkcs11Token }

            val token = tokenManager.waitForToken()
            return@withContext if (token.getSerialNumber() == tokenSerial) {
                token
            } else {
                throw BusinessRuleException(WRONG_RUTOKEN)
            }
        }
    }

    private suspend fun TokenManager.waitForToken(): Pkcs11Token {
        try {
            _showConnectTokenDialog.postValue(true)
            return getFirstTokenAsync().await().pkcs11Token
        } finally {
            _showConnectTokenDialog.postValue(false)
        }
    }
}
