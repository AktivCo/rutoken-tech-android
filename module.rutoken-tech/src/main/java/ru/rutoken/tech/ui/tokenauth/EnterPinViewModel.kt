package ru.rutoken.tech.ui.tokenauth

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EnterPinViewModel : ViewModel() {
    private val _isButtonEnabled = MutableLiveData(false)
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    private val _pinErrorText = MutableLiveData("")
    val pinErrorText: LiveData<String?> get() = _pinErrorText

    @MainThread
    fun onPinValueChanged(pinValue: String) {
        _pinErrorText.value = null
        _isButtonEnabled.value = pinValue.isNotEmpty()
    }

    fun setPinErrorValue(errorText: String) {
        _isButtonEnabled.postValue(false)
        _pinErrorText.postValue(errorText)
    }
}