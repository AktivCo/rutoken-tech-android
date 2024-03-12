package ru.rutoken.tech.ui.ca.generateobjects.keypair

import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.rutoken.tech.R
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPairParams
import ru.rutoken.tech.ui.components.BottomSheetTitle
import ru.rutoken.tech.ui.components.ErrorAlertDialog
import ru.rutoken.tech.ui.components.PrimaryButtonBox
import ru.rutoken.tech.ui.components.ProgressIndicatorDialog
import ru.rutoken.tech.ui.components.SimpleAlertDialog
import ru.rutoken.tech.ui.components.TextGroupBox
import ru.rutoken.tech.ui.components.TextGroupItem
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.errorDialogData
import ru.rutoken.tech.ui.utils.figmaPaddingValues

/**
 * TODO: Convert this to navigation node
 */
@Composable
fun GenerateKeyPairBottomSheet() {
    val viewModel = koinViewModel<GenerateKeyPairViewModel>()
    var showBottomSheet by remember { mutableStateOf(false) }
    GenerateKeyPairBottomSheetAdapter(
        viewModel = viewModel,
        showBottomSheet = showBottomSheet,
        onDismiss = { showBottomSheet = false }
    )

    // TODO: Call this when need to show GenerateKeyPairBottomSheet
    viewModel.generateKeyPairId()
    showBottomSheet = true
}

@Composable
private fun GenerateKeyPairBottomSheetAdapter(
    viewModel: GenerateKeyPairViewModel,
    showBottomSheet: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val keyPairId by viewModel.keyPairId.observeAsState("")

    ProgressIndicatorDialogAdapter(viewModel)
    SuccessDialogAdapter(
        viewModel = viewModel,
        onDismissOrConfirm = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismiss()
                }
            }
        }
    )
    ErrorDialogAdapter(viewModel)

    if (keyPairId.isNotEmpty() && showBottomSheet) {
        GenerateKeyPairBottomSheet(
            keyPairId = keyPairId,
            sheetState = sheetState,
            onDismiss = onDismiss,
            onGenerationButtonClicked = { viewModel.generateGostKeyPair(keyPairId, GostKeyPairParams.GOST_2012_256) }
        )
    }
}

@Composable
fun GenerateKeyPairBottomSheet(
    keyPairId: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onGenerationButtonClicked: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
    ) {
        BottomSheetTitle(stringResource(id = R.string.key_pair_title))

        TextGroupBox(
            items = listOf(
                TextGroupItem(
                    title = stringResource(R.string.key_pair_id),
                    value = keyPairId
                ),
                TextGroupItem(
                    title = stringResource(R.string.key_pair_algorithm),
                    value = stringResource(R.string.gost256_algorithm)
                )
            ),
            padding = figmaPaddingValues(0.dp, 16.dp, 16.dp, 16.dp)
        )

        PrimaryButtonBox(stringResource(R.string.generate)) { onGenerationButtonClicked() }

        Spacer(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                Modifier.height(WindowInsets.systemBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding())
            } else {
                Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            }
        )
    }
}

@Composable
private fun ProgressIndicatorDialogAdapter(viewModel: GenerateKeyPairViewModel) {
    val showProgress by viewModel.showProgress.observeAsState(false)

    if (showProgress) {
        ProgressIndicatorDialog()
    }
}

@Composable
private fun SuccessDialogAdapter(
    viewModel: GenerateKeyPairViewModel,
    onDismissOrConfirm: () -> Unit
) {
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
private fun ErrorDialogAdapter(viewModel: GenerateKeyPairViewModel) {
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
fun GenerateKeyPairBottomSheetPreview() {
    RutokenTechTheme {
        GenerateKeyPairBottomSheet(
            keyPairId = "12345678-90abcdef",
            sheetState = SheetState(
                skipPartiallyExpanded = true,
                density = LocalDensity.current,
                initialValue = SheetValue.Expanded
            ),
            onDismiss = { /* Nothing to do */ },
            onGenerationButtonClicked = { /* Nothing to do */ }
        )
    }
}
