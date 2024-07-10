/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R

object AppIcons {
    @Composable
    fun Logout() {
        Icon(
            painter = painterResource(id = R.drawable.ic_logout),
            contentDescription = "Logout icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    @Composable
    fun Delete() {
        Icon(
            painter = painterResource(id = R.drawable.ic_delete),
            contentDescription = "Delete icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    @Composable
    fun Menu() {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu Icon"
        )
    }

    @Composable
    fun MenuTitleLogo() {
        Icon(
            painter = painterResource(id = R.drawable.menu_logo),
            contentDescription = "Menu Title Logo",
            tint = MaterialTheme.colorScheme.tertiary
        )
    }

    @Composable
    fun BankMenuItem(selected: Boolean) {
        Icon(
            painter = painterResource(
                id = if (selected) R.drawable.ic_bank_menu_item_selected else R.drawable.ic_bank_menu_item
            ),
            contentDescription = "Bank Menu Icon",
            tint = getDrawerIconTint(selected = selected)
        )
    }

    @Composable
    fun CaMenuItem(selected: Boolean) {
        Icon(
            painter = painterResource(
                id = if (selected) R.drawable.ic_ca_menu_item_selected else R.drawable.ic_ca_menu_item
            ),
            contentDescription = "CA Menu Icon",
            tint = getDrawerIconTint(selected = selected)
        )
    }

    @Composable
    fun AboutMenuItem(selected: Boolean) {
        Icon(
            painter = painterResource(
                id = if (selected) R.drawable.ic_about_menu_item_selected else R.drawable.ic_about_menu_item
            ),
            contentDescription = "About Menu Icon",
            tint = getDrawerIconTint(selected = selected)
        )
    }

    @Composable
    fun Back() {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back Icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    @Composable
    fun ResetPayments() {
        Icon(
            painter = painterResource(id = R.drawable.reset_payments),
            contentDescription = "Reset Payments icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    @Composable
    fun PaymentToSign(isArchived: Boolean) {
        FilledCircleBackground(isArchived) {
            Icon(
                painter = painterResource(
                    id = if (isArchived) R.drawable.payment_to_verify else R.drawable.payment_to_sign
                ),
                contentDescription = "Payment To Sign icon",
                tint = getPaymentIconTint(isArchived)
            )
        }
    }

    @Composable
    fun PaymentToVerify(isArchived: Boolean) {
        FilledCircleBackground(isArchived) {
            Icon(
                painter = painterResource(
                    id = if (isArchived) R.drawable.payment_to_verify_archive else R.drawable.payment_to_verify
                ),
                contentDescription = "Payment To Verify icon",
                tint = getPaymentIconTint(isArchived)
            )
        }
    }

    @Composable
    fun PaymentToEncrypt(isArchived: Boolean) {
        FilledCircleBackground(isArchived) {
            Icon(
                painter = painterResource(
                    id = if (isArchived) R.drawable.payment_to_decrypt else R.drawable.payment_to_encrypt
                ),
                contentDescription = "Payment To Encrypt icon",
                tint = getPaymentIconTint(isArchived)
            )
        }
    }

    @Composable
    fun PaymentToDecrypt(isArchived: Boolean) {
        FilledCircleBackground(isArchived) {
            Icon(
                painter = painterResource(
                    id = if (isArchived) R.drawable.payment_to_encrypt else R.drawable.payment_to_decrypt
                ),
                contentDescription = "Payment To Decrypt icon",
                tint = getPaymentIconTint(isArchived)
            )
        }
    }

    @Composable
    private fun FilledCircleBackground(isArchived: Boolean, icon: @Composable () -> Unit) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .size(40.dp)
                .background(getPaymentIconBackground(isArchived)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }

    @Composable
    private fun getDrawerIconTint(selected: Boolean) =
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    private fun getPaymentIconBackground(isArchived: Boolean) =
        if (isArchived) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer

    @Composable
    private fun getPaymentIconTint(isArchived: Boolean) =
        if (isArchived) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
}