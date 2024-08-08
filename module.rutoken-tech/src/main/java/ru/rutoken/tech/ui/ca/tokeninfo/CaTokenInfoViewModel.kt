/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.tokeninfo

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.rutoken.tech.R
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.CaAppSession
import ru.rutoken.tech.session.requireCaSession
import ru.rutoken.tech.ui.ca.tokeninfo.model.VendorDefinedTokenModel
import ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard.SmartCard
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3UsbDual
import ru.rutoken.tech.ui.utils.DialogData
import ru.rutoken.tech.ui.utils.DialogState

data class CaTokenInfoUiState(
    val tokenType: TokenType,
    val label: String,
    val model: String,
    val serial: String
)

enum class TokenType {
    Usb, IsoNfc, UsbNfcDual
}

class CaTokenInfoViewModel(
    private val applicationContext: Context,
    private val sessionHolder: AppSessionHolder
) : ViewModel() {
    // CaAppSession instance MUST exist by the time this ViewModel is instantiated
    private val caAppSession: CaAppSession get() = sessionHolder.requireCaSession()

    private val _uiState = MutableLiveData(caAppSession.toUiState())
    val uiState: LiveData<CaTokenInfoUiState> get() = _uiState

    private val _navigateToCertGenerationEvent = MutableLiveData<Boolean>()
    val navigateToCertGenerationEvent: LiveData<Boolean> get() = _navigateToCertGenerationEvent

    private val _noKeyPairsOnTokenDialogState = MutableLiveData<DialogState>()
    val noKeyPairsOnTokenDialogState: LiveData<DialogState> get() = _noKeyPairsOnTokenDialogState

    @MainThread
    fun onNavigateToGenerateCertificate() {
        if (caAppSession.keyPairs.isNotEmpty()) {
            _navigateToCertGenerationEvent.value = true
        } else {
            _noKeyPairsOnTokenDialogState.value =
                DialogState(showDialog = true, data = DialogData(R.string.no_key_pairs_on_token))
        }
    }

    @MainThread
    fun resetNavigateToCertGenerationEvent() {
        _navigateToCertGenerationEvent.value = false
    }

    @MainThread
    fun onNoKeyPairsOnTokenDialogDismiss() {
        _noKeyPairsOnTokenDialogState.value = DialogState()
    }

    private fun CaAppSession.toUiState(): CaTokenInfoUiState {
        val tokenType = when (tokenModel) {
            is VendorDefinedTokenModel.SmartCard, is SmartCard -> TokenType.IsoNfc
            is VendorDefinedTokenModel.UsbDual, is Ecp3UsbDual -> TokenType.UsbNfcDual
            else -> TokenType.Usb
        }
        val label = if (isDefaultTokenLabel(tokenLabel)) applicationContext.getString(R.string.not_set) else tokenLabel

        return CaTokenInfoUiState(
            tokenType = tokenType,
            label = label,
            model = tokenModel.getModelName(applicationContext),
            serial = tokenSerial.toLong(16).toString()
        )
    }
}

private fun isDefaultTokenLabel(label: String) =
    arrayOf("<no label>", "<corrupted label!>").any { label.endsWith(it) }
