package ru.rutoken.tech.ui.tokenauth

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.PrimaryButtonBox
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.expandedSheetState

@Composable
fun EnterPinBottomSheet(
    pinErrorText: String?,
    buttonEnabled: Boolean,
    onPinValueChanged: (String) -> Unit,
    onButtonClicked: (String) -> Unit,
    sheetState: SheetState,
    onDismissRequest: () -> Unit
) {
    val windowInsets = when {
        VERSION.SDK_INT >= VERSION_CODES.R -> WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
        else -> WindowInsets.Companion.ime // Fixing Keyboard overlapping on Android 10 and older
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        windowInsets = windowInsets
    )
    {
        BottomSheetTitle(stringResource(R.string.enter_pin))

        var pinValue by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        val isPinError = !pinErrorText.isNullOrEmpty()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val hideKeyboardAction = {
            keyboardController?.hide()
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU || VERSION.SDK_INT < VERSION_CODES.R)
                focusManager.clearFocus(true) // Focus clearing breaks sheet resize on Android 11-12
        }

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

        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU || VERSION.SDK_INT < VERSION_CODES.R) {
            PrimaryButtonBox(stringResource(id = R.string.proceed), buttonEnabled, onClick = onClickAction)
        } else { // fixing keyboard overlapping for Android 11, 12 and 12L
            val scope = rememberCoroutineScope()
            val view = LocalView.current
            val bringIntoViewRequester = remember { BringIntoViewRequester() }

            DisposableEffect(view) {
                val listener = ViewTreeObserver.OnGlobalLayoutListener {
                    scope.launch { bringIntoViewRequester.bringIntoView() }
                }
                view.viewTreeObserver.addOnGlobalLayoutListener(listener)
                onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
            }

            Box(Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
                PrimaryButtonBox(stringResource(id = R.string.proceed), buttonEnabled, onClick = onClickAction)
            }
        }

        NavigationBarSpacer()

        LaunchedEffect(Unit) {
            delay(150) // Some devices do not show keyboard on autofocus without this delay
            focusRequester.requestFocus()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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