package ru.rutoken.tech.ui.ca

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType
import ru.rutoken.tech.pkcs11.findobjects.findGost256KeyContainers
import ru.rutoken.tech.pkcs11.serialNumberTrimmed
import ru.rutoken.tech.session.CaRutokenTechSession
import ru.rutoken.tech.session.RutokenTechSessionHolder
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.utils.logd

class CaLoginViewModel(private val tokenManager: TokenManager, private val sessionHolder: RutokenTechSessionHolder) :
    ViewModel() {
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    suspend fun login(tokenUserPin: String): Result<CaRutokenTechSession> {
        return withContext(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                doLogin(tokenUserPin)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun doLogin(tokenUserPin: String): Result<CaRutokenTechSession> {
        return withContext(Dispatchers.IO) {
            runCatching {
                createRutokenTechSession(tokenUserPin).also { newSession ->
                    sessionHolder.setSession(newSession)
                    logd<CaLoginViewModel> { "New CA session created, found ${newSession.keyPairs.size} key pairs" }
                }
            }
        }
    }

    private suspend fun createRutokenTechSession(tokenUserPin: String): CaRutokenTechSession {
        return withContext(Dispatchers.IO) {
            val pkcs11TokenData = requireNotNull(tokenManager.getFirstTokenAsync().await())
            val pkcs11Token = pkcs11TokenData.pkcs11Token
            val tokenInfo = pkcs11Token.tokenInfo

            pkcs11Token.openSession(false).use { session ->
                session.login(Pkcs11UserType.CKU_USER, tokenUserPin).use {
                    CaRutokenTechSession(
                        tokenUserPin = tokenUserPin,
                        tokenSerial = tokenInfo.serialNumberTrimmed,
                        tokenModel = pkcs11TokenData.model,
                        tokenLabel = tokenInfo.label,
                        keyPairs = session.findGost256KeyContainers().map { it.ckaId.toString(Charsets.UTF_8) }
                            .toMutableList()
                    )
                }
            }
        }
    }
}