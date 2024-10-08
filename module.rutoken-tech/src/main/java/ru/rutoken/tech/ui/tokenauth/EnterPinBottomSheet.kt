/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

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
import androidx.compose.runtime.remember
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
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.expandedSheetState
import ru.rutoken.tech.ui.utils.getHideKeyboardAction

@Composable
fun EnterPinBottomSheet(
    pinValue: String,
    pinErrorText: String?,
    buttonEnabled: Boolean,
    onPinValueChanged: (String) -> Unit,
    onButtonClicked: (String) -> Unit,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    hasBiometricPin: Boolean,
    onDecryptBiometricPin: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = { bottomSheetWindowInsets() }
    ) {
        BottomSheetTitle(stringResource(R.string.enter_pin))

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
                onPinValueChanged(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge,
            label = { Text(text = stringResource(id = R.string.pin_code)) },
            supportingText = { Text(if (isPinError) pinErrorText!! else "") },
            isError = isPinError,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { hideKeyboardAction() }),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(Modifier.height(16.dp))

        PrimaryButtonBox(stringResource(id = R.string.proceed), buttonEnabled, onClick = onClickAction)

        NavigationBarSpacer()

        LaunchedEffect(Unit) {
            if (hasBiometricPin) {
                onDecryptBiometricPin()
            } else {
                delay(150) // Some devices do not show keyboard on autofocus without this delay
                focusRequester.requestFocus()
            }
        }
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun EnterPinBottomSheetSuccess() {
    RutokenTechTheme {
        EnterPinBottomSheet(
            pinValue = "12345678",
            pinErrorText = null,
            buttonEnabled = true,
            onPinValueChanged = {},
            onButtonClicked = {},
            sheetState = expandedSheetState(),
            onDismissRequest = {},
            hasBiometricPin = false,
            onDecryptBiometricPin = {}
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun EnterPinBottomSheetError() {
    RutokenTechTheme {
        EnterPinBottomSheet(
            pinValue = "1234567",
            pinErrorText = stringResource(id = R.string.invalid_pin_supporting, 5),
            buttonEnabled = false,
            onPinValueChanged = {},
            onButtonClicked = {},
            sheetState = expandedSheetState(),
            onDismissRequest = {},
            hasBiometricPin = false,
            onDecryptBiometricPin = {}
        )
    }
}
