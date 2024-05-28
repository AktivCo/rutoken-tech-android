/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.tokeninfo.model

import android.content.Context

interface VendorDefinedTokenModel : TokenModel {
    val vendorModelName: String
    override fun getModelName(context: Context) = vendorModelName

    class Usb(override val vendorModelName: String) : VendorDefinedTokenModel

    class UsbDual(override val vendorModelName: String) : VendorDefinedTokenModel

    class SmartCard(override val vendorModelName: String) : VendorDefinedTokenModel

    class Unknown(override val vendorModelName: String) : VendorDefinedTokenModel
}