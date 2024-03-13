package ru.rutoken.tech.ui.utils

import androidx.annotation.StringRes
import ru.rutoken.tech.R
import ru.rutoken.tech.utils.BusinessRuleCase.IncorrectPin
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

open class DialogData(@StringRes val text: Int)

class ErrorDialogData(
    @StringRes val title: Int,
    @StringRes text: Int
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
        }

        else -> UNKNOWN_ERROR_DIALOG_DATA
    }
}
