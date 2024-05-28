/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard

import ru.rutoken.tech.R
import ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard.Ecp3SmartCard.MarketingModel

/**
 * ECP 3.0 Smart card.
 *
 * Covers the following marketing models:
 * - Rutoken ECP 3.0 NFC 3100
 * - Rutoken ECP 3.0 NFC MF 3110
 * @see MarketingModel
 */
internal class Ecp3SmartCard(model: MarketingModel) : SmartCard {
    override val modelNameId = when (model) {
        MarketingModel.ECP3_SC_3100 -> R.string.rutoken_model_ecp_3_nfc_3100
        MarketingModel.ECP3_SC_3110 -> R.string.rutoken_model_ecp_3_nfc_mf_3110
    }

    enum class MarketingModel {
        ECP3_SC_3100,
        ECP3_SC_3110
    }
}