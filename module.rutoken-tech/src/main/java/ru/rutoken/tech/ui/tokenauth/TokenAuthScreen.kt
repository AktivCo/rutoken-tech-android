package ru.rutoken.tech.ui.tokenauth

import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO make this Composable a complete navigation node
@Composable
fun TokenAuthScreen(enterPinViewModel: EnterPinViewModel, onTokenAuthDone: () -> Unit) {
    var showDialog by remember { mutableStateOf(true) }
    val dialogState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pinErrorText by enterPinViewModel.pinErrorText.observeAsState("")
    val isButtonEnabled by enterPinViewModel.isButtonEnabled.observeAsState(false)

    val dismissEffect = {
        enterPinViewModel.onPinValueChanged("")
        onTokenAuthDone()
    }

    if (showDialog) {
        EnterPinBottomSheet(
            pinErrorText = pinErrorText,
            buttonEnabled = isButtonEnabled,
            onPinValueChanged = enterPinViewModel::onPinValueChanged,
            onButtonClicked = {
                // TODO
            },
            sheetState = dialogState,
            onDismissRequest = dismissEffect
        )
    }
}