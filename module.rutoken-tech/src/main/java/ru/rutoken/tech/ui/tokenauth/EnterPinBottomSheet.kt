package ru.rutoken.tech.ui.tokenauth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.PrimaryButtonBox
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.ImeFocusHelper
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.expandedSheetState
import ru.rutoken.tech.ui.utils.getHideKeyboardAction

@Composable
fun EnterPinBottomSheet(
    pinErrorText: String?,
    buttonEnabled: Boolean,
    onPinValueChanged: (String) -> Unit,
    onButtonClicked: (String) -> Unit,
    sheetState: SheetState,
    onDismissRequest: () -> Unit
) {
    val windowInsets = bottomSheetWindowInsets()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        windowInsets = windowInsets
    ) {
        BottomSheetTitle(stringResource(R.string.enter_pin))

        var pinValue by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        val isPinError = !pinErrorText.isNullOrEmpty()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val hideKeyboardAction =
            getHideKeyboardAction(focusManager = focusManager, keyboardController = keyboardController)

        val onClickAction = {
            hideKeyboardAction()
            onButtonClicked(pinValue)
        }

        OutlinedTextField(
            value = pinValue,
            onValueChange = {
                pinValue = it
                onPinValueChanged(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge,
            label = { Text(text = stringResource(id = R.string.pin_code)) },
            placeholder = { Text(text = stringResource(id = R.string.pin_code)) },
            supportingText = { Text(if (isPinError) pinErrorText!! else "") },
            isError = isPinError,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { hideKeyboardAction() }),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(Modifier.height(16.dp))

        ImeFocusHelper {
            PrimaryButtonBox(stringResource(id = R.string.proceed), buttonEnabled, onClick = onClickAction)
        }

        NavigationBarSpacer()

        LaunchedEffect(Unit) {
            delay(150) // Some devices do not show keyboard on autofocus without this delay
            focusRequester.requestFocus()
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun EnterPinBottomSheetSuccess() {
    RutokenTechTheme {
        EnterPinBottomSheet(
            pinErrorText = null,
            buttonEnabled = true,
            onPinValueChanged = {},
            onButtonClicked = {},
            sheetState = expandedSheetState(),
            onDismissRequest = {}
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun EnterPinBottomSheetError() {
    RutokenTechTheme {
        EnterPinBottomSheet(
            pinErrorText = stringResource(id = R.string.invalid_pin_supporting, 5),
            buttonEnabled = false,
            onPinValueChanged = {},
            onButtonClicked = {},
            sheetState = expandedSheetState(),
            onDismissRequest = {}
        )
    }
}
