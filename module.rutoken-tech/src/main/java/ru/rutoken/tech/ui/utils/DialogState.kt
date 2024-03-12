package ru.rutoken.tech.ui.utils

import androidx.annotation.StringRes
import ru.rutoken.tech.R
import ru.rutoken.tech.utils.BusinessRuleCase.TOKEN_REMOVED
import ru.rutoken.tech.utils.BusinessRuleCase.WRONG_RUTOKEN
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
            WRONG_RUTOKEN -> ErrorDialogData(
                title = R.string.wrong_token_title,
                text = R.string.wrong_token_text,
            )

            TOKEN_REMOVED -> ErrorDialogData(
                title = R.string.connection_lost_title,
                text = R.string.connection_lost_text,
            )
        }

        else -> UNKNOWN_ERROR_DIALOG_DATA
    }
}
