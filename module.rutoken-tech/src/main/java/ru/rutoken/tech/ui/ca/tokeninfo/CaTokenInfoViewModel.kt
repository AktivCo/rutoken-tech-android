package ru.rutoken.tech.ui.ca.tokeninfo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.rutoken.tech.session.CaRutokenTechSession
import ru.rutoken.tech.session.RutokenTechSessionHolder
import ru.rutoken.tech.session.requireCaSession
import ru.rutoken.tech.ui.ca.tokeninfo.model.VendorDefinedTokenModel
import ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard.SmartCard
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3UsbDual
import ru.rutoken.tech.utils.loge

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
    sessionHolder: RutokenTechSessionHolder
) : ViewModel() {
    private val _uiState = MutableLiveData<CaTokenInfoUiState>()
    val uiState: LiveData<CaTokenInfoUiState> = _uiState

    init {
        runCatching {
            _uiState.postValue(sessionHolder.requireCaSession().toUiState())
        }.onFailure { e ->
            loge(e) { "Failed to create token info UI state from current session" }
        }
    }

    private fun CaRutokenTechSession.toUiState(): CaTokenInfoUiState {
        val tokenType = when (tokenModel) {
            is VendorDefinedTokenModel.SmartCard, is SmartCard -> TokenType.IsoNfc
            is VendorDefinedTokenModel.UsbDual, is Ecp3UsbDual -> TokenType.UsbNfcDual
            else -> TokenType.Usb
        }

        return CaTokenInfoUiState(
            tokenType = tokenType,
            label = tokenLabel,
            model = tokenModel.getModelName(applicationContext),
            serial = tokenSerial.toLong(16).toString()
        )
    }
}
