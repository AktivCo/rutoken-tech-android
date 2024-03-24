package ru.rutoken.tech.ui.ca.tokeninfo.model.usb

import ru.rutoken.tech.R
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.FlashMarketingModel
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.MarketingModel

/**
 * ECP 2.0 usb token.
 *
 * Covers the following marketing models:
 * - Rutoken ECP 2.0 2000
 * - Rutoken ECP 2.0 2100
 * - Rutoken ECP 2.0 2200
 * - Rutoken ECP 2.0 3000
 * - Rutoken ECP 2.0 4000
 * - Rutoken ECP 2.0 4400
 * - Rutoken ECP 2.0 4500 (with flash)
 * - Rutoken ECP 2.0 4900 (with flash)
 * @see MarketingModel
 * @see FlashMarketingModel
 */
internal class Ecp2Usb private constructor(override val modelNameId: Int) : Usb {
    constructor(model: MarketingModel) : this(
        when (model) {
            MarketingModel.ECP2_USB_2000 -> R.string.rutoken_model_ecp_2_2000
            MarketingModel.ECP2_USB_2100 -> R.string.rutoken_model_ecp_2_2100
            MarketingModel.ECP2_USB_2200 -> R.string.rutoken_model_ecp_2_2200
            MarketingModel.ECP2_USB_3000 -> R.string.rutoken_model_ecp_2_3000
            MarketingModel.ECP2_USB_4000 -> R.string.rutoken_model_ecp_2_4000
            MarketingModel.ECP2_USB_4400 -> R.string.rutoken_model_ecp_2_4400
        }
    )

    constructor(model: FlashMarketingModel) : this(
        when (model) {
            FlashMarketingModel.ECP2_FLASH_4500 -> R.string.rutoken_model_ecp_2_4500
            FlashMarketingModel.ECP2_FLASH_4900 -> R.string.rutoken_model_ecp_2_4900
        }
    )

    enum class MarketingModel {
        ECP2_USB_2000,
        ECP2_USB_2100,
        ECP2_USB_2200,
        ECP2_USB_3000,
        ECP2_USB_4000,
        ECP2_USB_4400
    }

    enum class FlashMarketingModel {
        ECP2_FLASH_4500,
        ECP2_FLASH_4900
    }
}