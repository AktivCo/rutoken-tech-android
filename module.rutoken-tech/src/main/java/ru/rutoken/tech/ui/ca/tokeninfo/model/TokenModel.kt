/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.tokeninfo.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.rutoken.pkcs11jna.RtPkcs11Constants.INTERFACE_TYPE_ISO
import ru.rutoken.pkcs11jna.RtPkcs11Constants.INTERFACE_TYPE_NFC_TYPE_A
import ru.rutoken.pkcs11jna.RtPkcs11Constants.INTERFACE_TYPE_NFC_TYPE_B
import ru.rutoken.pkcs11jna.RtPkcs11Constants.INTERFACE_TYPE_USB
import ru.rutoken.pkcs11jna.RtPkcs11Constants.TOKEN_FLAGS_HAS_BUTTON
import ru.rutoken.pkcs11jna.RtPkcs11Constants.TOKEN_FLAGS_HAS_FLASH_DRIVE
import ru.rutoken.pkcs11jna.RtPkcs11Constants.TOKEN_TYPE_RUTOKEN
import ru.rutoken.pkcs11jna.RtPkcs11Constants.TOKEN_TYPE_RUTOKEN_ECP
import ru.rutoken.pkcs11jna.RtPkcs11Constants.TOKEN_TYPE_RUTOKEN_ECPDUAL_USB
import ru.rutoken.pkcs11jna.RtPkcs11Constants.TOKEN_TYPE_RUTOKEN_ECP_SC
import ru.rutoken.pkcs11jna.RtPkcs11Constants.TOKEN_TYPE_RUTOKEN_LITE
import ru.rutoken.pkcs11wrapper.attribute.Pkcs11StringAttribute
import ru.rutoken.pkcs11wrapper.attribute.longvalue.Pkcs11HardwareFeatureTypeAttribute
import ru.rutoken.pkcs11wrapper.attribute.longvalue.Pkcs11ObjectClassAttribute
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11TokenInfo
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.rutoken.constant.RtPkcs11AttributeType
import ru.rutoken.pkcs11wrapper.rutoken.constant.RtPkcs11HardwareFeatureType
import ru.rutoken.pkcs11wrapper.rutoken.datatype.TokenInfoExtended
import ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard.Ecp2SmartCard
import ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard.Ecp3SmartCard
import ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard.Ecp3SmartCard.MarketingModel.ECP3_SC_3100
import ru.rutoken.tech.ui.ca.tokeninfo.model.smartcard.Ecp3SmartCard.MarketingModel.ECP3_SC_3110
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.FlashMarketingModel.ECP2_FLASH_4500
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.FlashMarketingModel.ECP2_FLASH_4900
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.MarketingModel.ECP2_USB_2000
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.MarketingModel.ECP2_USB_2100
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.MarketingModel.ECP2_USB_2200
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.MarketingModel.ECP2_USB_3000
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.MarketingModel.ECP2_USB_4000
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp2Usb.MarketingModel.ECP2_USB_4400
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3Usb
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3Usb.MarketingModel.ECP3_USB_3100
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3Usb.MarketingModel.ECP3_USB_3120
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3Usb.MarketingModel.ECP3_USB_3200
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3Usb.MarketingModel.ECP3_USB_3220
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3Usb.MarketingModel.ECP3_USB_3250
import ru.rutoken.tech.ui.ca.tokeninfo.model.usb.Ecp3UsbDual

/**
 * An interface of token model containing information about UI representation of token.
 */
interface TokenModel {
    fun getModelName(context: Context): String

    companion object {
        internal fun fromTokenInfo(
            tokenInfo: Pkcs11TokenInfo,
            tokenInfoEx: TokenInfoExtended,
            tokenInfoHwFeatures: TokenInfoHwFeatures?
        ): TokenModel {
            tokenInfoHwFeatures?.getModelByVendorDefinedName()?.let { model ->
                // Identified TokenModel by vendor model name and current interface from hardware features
                return model
            }

            val tokenType = tokenInfoEx.tokenType

            // `aa.bb.cc.dd` is a version identifier (`aa.bb` for hardware and `cc.dd` for firmware)
            val aa = tokenInfo.hardwareVersion.major.toUInt()
            val cc = tokenInfo.firmwareVersion.major.toUInt()
            val dd = tokenInfo.firmwareVersion.minor.toUInt()

            val isSt31 = aa == 60U && cc in arrayOf(28U, 30U, 31U)
            val hasFlash = tokenInfoEx.flags has TOKEN_FLAGS_HAS_FLASH_DRIVE
            val hasTouch = tokenInfoEx.flags has TOKEN_FLAGS_HAS_BUTTON

            if (isSt31 && tokenType != TOKEN_TYPE_RUTOKEN_LITE) {
                return when (tokenInfoHwFeatures!!.currentInterface) {
                    INTERFACE_TYPE_ISO -> when (cc) {
                        31U -> Ecp3SmartCard(ECP3_SC_3110)
                        28U, 30U -> Ecp3SmartCard(ECP3_SC_3100)
                        else -> UnknownRutoken
                    }

                    INTERFACE_TYPE_USB -> if (tokenInfoHwFeatures.isNfcTypeBSupported) {
                        Ecp3UsbDual
                    } else {
                        Ecp3Usb(ECP3_USB_3100)
                    }

                    INTERFACE_TYPE_NFC_TYPE_A -> Ecp3SmartCard(ECP3_SC_3110)

                    INTERFACE_TYPE_NFC_TYPE_B -> when {
                        tokenInfoHwFeatures.isUsbSupported -> Ecp3UsbDual
                        tokenInfoHwFeatures.isIsoSupported -> Ecp3SmartCard(ECP3_SC_3100)
                        else -> UnknownRutoken
                    }

                    else -> UnknownRutoken
                }
            }

            return when (tokenType) {
                TOKEN_TYPE_RUTOKEN -> UnknownRutoken
                TOKEN_TYPE_RUTOKEN_ECP -> when {
                    hasFlash -> when {
                        hasTouch && (cc == 27U && aa in arrayOf(55U, 59U)
                                || cc == 24U && aa == 55U) -> Ecp2Usb(ECP2_FLASH_4900)

                        cc == 24U && aa == 55U
                                || cc == 26U && aa == 59U
                                || cc == 27U && aa in arrayOf(55U, 58U, 59U) -> Ecp2Usb(ECP2_FLASH_4500)

                        else -> UnknownRutoken
                    }

                    cc < 20U -> UnknownRutoken

                    cc == 23U && aa == 54U && dd == 0U -> UnknownRutoken
                    cc == 30U && aa == 59U -> Ecp3Usb(ECP3_USB_3200)
                    cc == 30U && aa == 65U -> Ecp3Usb(ECP3_USB_3220)
                    cc == 31U && aa == 65U -> Ecp3Usb(ECP3_USB_3250)
                    cc == 31U && aa == 67U -> Ecp3Usb(ECP3_USB_3120)
                    else -> when { // ECP 2
                        (cc == 23U && aa == 20U) || (cc == 26U && aa == 59U) -> ECP2_USB_2000
                        cc == 23U && aa == 54U && dd == 2U -> ECP2_USB_2100
                        cc == 24U && aa == 20U -> ECP2_USB_2200
                        (cc == 26U && aa == 20U) || (cc == 27U && aa == 59U) -> ECP2_USB_3000
                        cc == 24U && aa == 55U -> if (hasTouch) ECP2_USB_4400 else ECP2_USB_4000
                        else -> null
                    }?.let { Ecp2Usb(it) } ?: UnknownRutoken
                }

                TOKEN_TYPE_RUTOKEN_ECPDUAL_USB, TOKEN_TYPE_RUTOKEN_LITE -> UnknownRutoken

                TOKEN_TYPE_RUTOKEN_ECP_SC -> when {
                    cc == 23U && aa == 54U && dd == 2U -> Ecp2SmartCard
                    else -> UnknownRutoken
                }

                else -> UnknownRutoken
            }
        }

        private fun TokenInfoHwFeatures.getModelByVendorDefinedName(): TokenModel? {
            val modelName = vendorModelName ?: return null
            return when (currentInterface) {
                INTERFACE_TYPE_ISO -> VendorDefinedTokenModel.SmartCard(modelName)

                INTERFACE_TYPE_USB -> if (isNfcSupported) {
                    VendorDefinedTokenModel.UsbDual(modelName)
                } else {
                    VendorDefinedTokenModel.Usb(modelName)
                }

                INTERFACE_TYPE_NFC_TYPE_A, INTERFACE_TYPE_NFC_TYPE_B -> when {
                    isUsbSupported -> VendorDefinedTokenModel.UsbDual(modelName)

                    isIsoSupported -> VendorDefinedTokenModel.SmartCard(modelName)

                    else -> VendorDefinedTokenModel.Unknown(modelName)
                }

                // current interface is undefined
                else -> VendorDefinedTokenModel.Unknown(modelName)
            }
        }
    }
}

suspend fun defineTokenModel(
    pkcs11Session: Pkcs11Session,
    tokenInfo: Pkcs11TokenInfo,
    tokenInfoExtended: TokenInfoExtended
): TokenModel {
    return withContext(Dispatchers.IO) {
        TokenModel.fromTokenInfo(tokenInfo, tokenInfoExtended, pkcs11Session.getTokenInfoHwFeatures())
    }
}

private val TokenInfoHwFeatures.isIsoSupported: Boolean
    get() = supportedInterfaces has INTERFACE_TYPE_ISO

private val TokenInfoHwFeatures.isUsbSupported: Boolean
    get() = supportedInterfaces has INTERFACE_TYPE_USB

private val TokenInfoHwFeatures.isNfcSupported: Boolean
    get() = isNfcTypeASupported || isNfcTypeBSupported

private val TokenInfoHwFeatures.isNfcTypeASupported: Boolean
    get() = supportedInterfaces has INTERFACE_TYPE_NFC_TYPE_A

private val TokenInfoHwFeatures.isNfcTypeBSupported: Boolean
    get() = supportedInterfaces has INTERFACE_TYPE_NFC_TYPE_B

private infix fun Long.has(flag: Long) = this and flag != 0L

private fun Pkcs11Session.getTokenInfoHwFeatures(): TokenInfoHwFeatures {
    val hwFeature = objectManager.findSingleObject(
        listOf(
            Pkcs11ObjectClassAttribute(Pkcs11AttributeType.CKA_CLASS, Pkcs11ObjectClass.CKO_HW_FEATURE),
            Pkcs11HardwareFeatureTypeAttribute(
                Pkcs11AttributeType.CKA_HW_FEATURE_TYPE,
                RtPkcs11HardwareFeatureType.CKH_VENDOR_TOKEN_INFO
            )
        )
    )
    return TokenInfoHwFeatures(
        hwFeature.getLongAttributeValue(this, RtPkcs11AttributeType.CKA_VENDOR_CURRENT_TOKEN_INTERFACE).longValue,
        hwFeature.getLongAttributeValue(this, RtPkcs11AttributeType.CKA_VENDOR_SUPPORTED_TOKEN_INTERFACE).longValue,
        hwFeature.getStringAttributeValue(this, RtPkcs11AttributeType.CKA_VENDOR_MODEL_NAME).getValueOrNull()
    )
}

private fun Pkcs11StringAttribute.getValueOrNull() = if (this.isPresent && !this.isEmpty) this.stringValue else null

data class TokenInfoHwFeatures(
    val currentInterface: Long,
    val supportedInterfaces: Long,
    val vendorModelName: String?
)

val TokenModel.isSupported: Boolean
    get() {
        return when (this) {
            is UnknownRutoken, is VendorDefinedTokenModel.Unknown -> false
            else -> true
        }
    }