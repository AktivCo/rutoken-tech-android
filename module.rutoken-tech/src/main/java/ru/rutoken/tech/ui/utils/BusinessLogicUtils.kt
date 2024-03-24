package ru.rutoken.tech.ui.utils

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import ru.rutoken.pkcs11wrapper.main.Pkcs11Exception
import ru.rutoken.tech.session.SerialHexString
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.utils.BusinessRuleCase.TOKEN_REMOVED
import ru.rutoken.tech.utils.BusinessRuleException

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
        throw if (checkTokenRemoved(tokenManager, tokenSerial)) BusinessRuleException(TOKEN_REMOVED) else e
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
