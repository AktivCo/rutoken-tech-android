/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.tokeninfo.model.usb

import ru.rutoken.tech.R
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3Usb.MarketingModel

/**
 * ECP 3.0 usb token.
 *
 * Covers the following marketing models:
 * - Rutoken ECP 3.0 3100
 * - Rutoken ECP 3.0 3120
 * - Rutoken ECP 3.0 3200
 * - Rutoken ECP 3.0 3220
 * - Rutoken ECP 3.0 3250
 * @see MarketingModel
 */
internal class Ecp3Usb(model: MarketingModel) : Usb {
    override val modelNameId = when (model) {
        MarketingModel.ECP3_USB_3100 -> R.string.rutoken_model_ecp_3_3100
        MarketingModel.ECP3_USB_3200 -> R.string.rutoken_model_ecp_3_3200
        MarketingModel.ECP3_USB_3220 -> R.string.rutoken_model_ecp_3_3220
        MarketingModel.ECP3_USB_3120 -> R.string.rutoken_model_ecp_3_3120
        MarketingModel.ECP3_USB_3250 -> R.string.rutoken_model_ecp_3_3250
    }

    enum class MarketingModel {
        ECP3_USB_3100,
        ECP3_USB_3120,
        ECP3_USB_3200,
        ECP3_USB_3220,
        ECP3_USB_3250
    }
}