/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import ru.rutoken.tech.R
import ru.rutoken.tech.utils.BusinessRuleCase.IncorrectPin
import ru.rutoken.tech.utils.BusinessRuleCase.NoSuchCertificate
import ru.rutoken.tech.utils.BusinessRuleCase.NoSuchKeyPair
import ru.rutoken.tech.utils.BusinessRuleCase.PinLocked
import ru.rutoken.tech.utils.BusinessRuleCase.TokenRemoved
import ru.rutoken.tech.utils.BusinessRuleCase.WrongRutoken
import ru.rutoken.tech.utils.BusinessRuleException

private val UNKNOWN_ERROR_DIALOG_DATA =
    ErrorDialogData(title = R.string.unknown_error_title, text = R.string.unknown_error_text)

class DialogState(
    val showDialog: Boolean = false,
    val data: DialogData = UNKNOWN_ERROR_DIALOG_DATA
)

open class DialogData(@StringRes val text: Int?)

class ErrorDialogData(
    @StringRes val title: Int,
    @StringRes text: Int
) : DialogData(text)

class DialogDataWithIcon(
    val icon: @Composable () -> Unit,
    @StringRes val title: Int,
    @StringRes text: Int?
) : DialogData(text)

val DialogState.errorDialogData get() = data as? ErrorDialogData ?: UNKNOWN_ERROR_DIALOG_DATA

fun Throwable.toErrorDialogData(): ErrorDialogData {
    return when (this) {
        is BusinessRuleException -> when (case) {
            WrongRutoken -> ErrorDialogData(
                title = R.string.wrong_token_title,
                text = R.string.wrong_token_text,
            )

            TokenRemoved -> ErrorDialogData(
                title = R.string.connection_lost_title,
                text = R.string.connection_lost_text,
            )

            PinLocked -> ErrorDialogData(
                title = R.string.pin_locked_title,
                text = R.string.pin_locked_text
            )

            is IncorrectPin -> ErrorDialogData(
                title = R.string.pin_changed_title,
                text = R.string.pin_changed_text
            )

            is NoSuchKeyPair -> ErrorDialogData(
                title = R.string.no_such_key_pair_title,
                text = R.string.no_such_key_pair_text
            )

            is NoSuchCertificate -> ErrorDialogData(
                title = R.string.no_such_certificate_title,
                text = if (case.isBankUser)
                    R.string.no_such_certificate_bank_text
                else
                    R.string.no_such_certificate_shift_text
            )
        }

        else -> UNKNOWN_ERROR_DIALOG_DATA
    }
}
