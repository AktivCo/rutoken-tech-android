package ru.rutoken.tech.ui.ca.generateobjects.keypair

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rutoken.tech.pkcs11.createobjects.generateCkaId

class GenerateKeyPairViewModel : ViewModel() {
    private val _keyPairId = MutableLiveData("")
    val keyPairId: LiveData<String> = _keyPairId

    fun generateKeyPairId() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _keyPairId.postValue(generateCkaId().toString(Charsets.UTF_8))
            }
        }
    }
}