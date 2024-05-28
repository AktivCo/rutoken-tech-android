/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.generateobjects.certificate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.BottomSheetDefaults.ExpandedShape
import androidx.compose.material3.BottomSheetDefaults.HiddenShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.session.CkaIdString
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.ConnectTokenDialog
import ru.rutoken.tech.ui.components.ErrorAlertDialog
import ru.rutoken.tech.ui.components.NavigationBarSpacer
import ru.rutoken.tech.ui.components.PrimaryButtonBox
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.SimpleAlertDialog
import ru.rutoken.tech.ui.components.TextGroup
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.bottomSheetWindowInsets
import ru.rutoken.tech.ui.utils.errorDialogData
import ru.rutoken.tech.ui.utils.expandedSheetState
import ru.rutoken.tech.ui.utils.getHideKeyboardAction
import ru.rutoken.tech.ui.utils.statusBarsPaddingHeight

@Composable
fun GenerateCertificateScreen(
    viewModel: GenerateCertificateViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val keyPairs by viewModel.keyPairs.observeAsState(emptyList())
    val shouldLogout by viewModel.shouldLogout.observeAsState(false)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (shouldLogout) onLogout()

    if (keyPairs.isNotEmpty()) {
        GenerateCertificateBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onNavigateBack,
            keyPairs = keyPairs,
            onGenerationButtonClicked = viewModel::generateGostCertificate
        )
    }

    ConnectTokenDialog(viewModel)
    ProgressIndicatorDialog(viewModel)
    SuccessDialog(
        viewModel = viewModel,
        onDismissOrConfirm = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onNavigateBack()
                }
            }
        }
    )
    ErrorDialog(viewModel)
}

@Composable
fun GenerateCertificateBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    keyPairs: List<CkaIdString>,
    onGenerationButtonClicked: (CkaIdString, String) -> Unit
) {
    val dragHandle: @Composable (() -> Unit) = {
        if (sheetState.targetValue != Expanded) {
            DragHandle()
        } else {
            Spacer(Modifier.statusBarsPaddingHeight())
        }
    }
    val shape = if (sheetState.targetValue != Expanded) ExpandedShape else HiddenShape

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = { bottomSheetWindowInsets() },
        dragHandle = dragHandle,
        shape = shape,
    ) {
        var selectedKeyPair by remember { mutableStateOf(keyPairs.first()) }
        var owner by remember { mutableStateOf("") }
        val buttonEnabled = owner.isNotEmpty()

        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val hideKeyboardAction =
            getHideKeyboardAction(focusManager = focusManager, keyboardController = keyboardController)

        BottomSheetTitle(stringResource(id = R.string.test_certificate_title))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .weight(weight = 1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KeyPairSelectionDropdownMenu(
                keyPairs = keyPairs,
                selectedKeyPair = selectedKeyPair,
                onItemClick = { selectedKeyPair = it }
            )

            Spacer(Modifier.height(16.dp))

            OwnerTextField(value = owner, onValueChange = { owner = it }, hideKeyboardAction = hideKeyboardAction)

            Spacer(Modifier.height(24.dp))

            CertificateAttributesList()
        }

        StickyGenerationButton(
            offset = { IntOffset(x = 0, y = -sheetState.requireOffset().toInt()) },
            buttonEnabled = buttonEnabled,
            onGenerationButtonClicked = {
                hideKeyboardAction()
                onGenerationButtonClicked(selectedKeyPair, owner)
            }
        )
    }
}

@Composable
private fun KeyPairSelectionDropdownMenu(
    keyPairs: List<CkaIdString>,
    selectedKeyPair: CkaIdString,
    onItemClick: (keyPair: CkaIdString) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = menuExpanded,
        onExpandedChange = { menuExpanded = !menuExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedKeyPair,
            onValueChange = { /* Nothing to do */ },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text(stringResource(id = R.string.select_key_pair)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuExpanded) },
            singleLine = true,
            shape = RoundedCornerShape(size = 12.dp),
        )

        ExposedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            matchTextFieldWidth = false,
            shape = RoundedCornerShape(size = 12.dp)
        ) {
            keyPairs.forEach { keyPair ->
                DropdownMenuItem(
                    text = { Text(text = keyPair, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onItemClick(keyPair)
                        menuExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun OwnerTextField(value: String, onValueChange: (String) -> Unit, hideKeyboardAction: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(id = R.string.enter_owner)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { hideKeyboardAction() }),
        singleLine = true,
        shape = RoundedCornerShape(size = 12.dp)
    )
}

@Composable
private fun CertificateAttributesList() {
    val titles = stringArrayResource(id = R.array.certificate_attributes_titles)
    val values = stringArrayResource(id = R.array.certificate_attributes_values)

    TextGroup(items = titles.zip(values).map { TextGroupItem(title = it.first, value = it.second) })
}

@Composable
private fun StickyGenerationButton(
    offset: Density.() -> IntOffset,
    buttonEnabled: Boolean,
    onGenerationButtonClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .offset(offset)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .imePadding()
    ) {
        PrimaryButtonBox(
            text = stringResource(R.string.generate),
            enabled = buttonEnabled,
            onClick = onGenerationButtonClicked
        )

        NavigationBarSpacer()
    }
}

@Composable
private fun ConnectTokenDialog(viewModel: GenerateCertificateViewModel) {
    val showDialog by viewModel.tokenConnector.showConnectTokenDialog.observeAsState(false)

    if (showDialog) {
        ConnectTokenDialog(onDismissRequest = { viewModel.tokenConnector.onDismissConnectTokenDialog() })
    }
}

@Composable
private fun ProgressIndicatorDialog(viewModel: GenerateCertificateViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
private fun SuccessDialog(viewModel: GenerateCertificateViewModel, onDismissOrConfirm: () -> Unit) {
    val dialogState by viewModel.successDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        SimpleAlertDialog(
            text = stringResource(id = dialogState.data.text),
            onDismissOrConfirm = {
                viewModel.dismissSuccessDialog()
                onDismissOrConfirm()
            }
        )
    }
}

@Composable
private fun ErrorDialog(viewModel: GenerateCertificateViewModel) {
    val dialogState by viewModel.errorDialogState.observeAsState(DialogState())

    if (dialogState.showDialog) {
        ErrorAlertDialog(
            title = stringResource(id = dialogState.errorDialogData.title),
            text = stringResource(id = dialogState.errorDialogData.text),
            onDismissOrConfirm = { viewModel.dismissErrorDialog() }
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun GenerateCertificateBottomSheetPreview() {
    RutokenTechTheme {
        GenerateCertificateBottomSheet(
            sheetState = expandedSheetState(),
            onDismissRequest = {},
            keyPairs = listOf(
                "f15da113-ce87b7d6",
                "bf5b3861-5af06f67",
                "5a2cc4ba-cd64bef4",
                "0af7e1f8-5c0972cb",
                "b15cabee-0d86845d"
            ),
            onGenerationButtonClicked = { _, _ -> }
        )
    }
}
