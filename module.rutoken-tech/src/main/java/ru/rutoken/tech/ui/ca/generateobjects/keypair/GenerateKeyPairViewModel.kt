package ru.rutoken.tech.ui.ca.generateobjects.keypair

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType.CKU_USER
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.tech.R
import ru.rutoken.tech.pkcs11.CkaIdString
import ru.rutoken.tech.pkcs11.TokenContextStorage
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPair
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPairParams
import ru.rutoken.tech.pkcs11.createobjects.createGostKeyPair
import ru.rutoken.tech.pkcs11.createobjects.generateCkaId
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.utils.DialogData
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.callPkcs11Operation
import ru.rutoken.tech.ui.utils.toErrorDialogData
import ru.rutoken.tech.utils.BusinessRuleCase
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.loge

class GenerateKeyPairViewModel(
    private val tokenManager: TokenManager,
    private val contextStorage: TokenContextStorage
) : ViewModel() {
    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> = _showProgress

    private val _successDialogState = MutableLiveData<DialogState>()
    val successDialogState: LiveData<DialogState> = _successDialogState

    private val _errorDialogState = MutableLiveData<DialogState>()
    val errorDialogState: LiveData<DialogState> = _errorDialogState

    private val _keyPairId = MutableLiveData("")
    val keyPairId: LiveData<CkaIdString> = _keyPairId

    fun generateKeyPairId() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _keyPairId.postValue(generateCkaId().toString(Charsets.UTF_8))
            }
        }
    }

    fun generateGostKeyPair(ckaId: CkaIdString, keyPairParams: GostKeyPairParams) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val currentContext = contextStorage.requireCurrentContext()
                    val tokenSerial = currentContext.tokenSerial
                    // TODO: Add waiting for token logic
                    val token = tokenManager.getTokenBySerialNumber(tokenSerial)
                        ?: throw BusinessRuleException(BusinessRuleCase.WRONG_RUTOKEN)

                    callPkcs11Operation(_showProgress, tokenManager, tokenSerial) {
                        createGostKeyPair(token, currentContext.tokenUserPin, ckaId, keyPairParams)
                        currentContext.keyPairs.addFirst(ckaId)
                        _successDialogState.postValue(
                            DialogState(
                                showDialog = true,
                                data = DialogData(text = R.string.key_pair_generated)
                            )
                        )
                    }
                } catch (e: Exception) {
                    loge<GenerateKeyPairViewModel>(e) { "Key pair generation failed" }
                    _errorDialogState.postValue(DialogState(showDialog = true, data = e.toErrorDialogData()))
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _successDialogState.value = DialogState(showDialog = false)
    }

    fun dismissErrorDialog() {
        _errorDialogState.value = DialogState(showDialog = false)
    }

    private fun createGostKeyPair(
        token: Pkcs11Token,
        userPin: String,
        ckaId: CkaIdString,
        keyPairParams: GostKeyPairParams
    ): GostKeyPair {
        return token.openSession(true).use { session ->
            session.login(CKU_USER, userPin).use {
                session.createGostKeyPair(keyPairParams, ckaId.toByteArray())
            }
        }
    }
}
