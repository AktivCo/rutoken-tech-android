/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documentspreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.largeTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.rutoken.tech.R
import ru.rutoken.tech.session.DocumentsPreviewInfo
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.components.OptionSelectionDialog
import ru.rutoken.tech.ui.components.RutokenTechLargeTopAppBar
import ru.rutoken.tech.ui.components.RutokenTechTopAppBar
import ru.rutoken.tech.ui.components.SecondaryButtonBox
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.ui.utils.startShareChooser

@Composable
fun DocumentsPreviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: DocumentsPreviewViewModel = koinViewModel(),
    onSignClick: () -> Unit,
) {
    val documentsInfo by viewModel.documents.observeAsState(DocumentsPreviewInfo())
    val areDocumentsSigned by viewModel.areDocumentsSigned.observeAsState(false)
    val showFinishSigningDialog by viewModel.showFinishSigningDialog.observeAsState(false)
    val navigateBack by viewModel.navigateBack.observeAsState(false)

    val context = LocalContext.current
    val onShareClick = { viewModel.onShareClicked(context::startShareChooser) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.updateSignState() }

    if (documentsInfo.documents.isNotEmpty()) {
        DocumentsPreviewScreen(
            documentsInfo = documentsInfo.documents,
            startPreviewIndex = documentsInfo.startDocument,
            areDocumentsSigned = areDocumentsSigned,
            onNavigateBack = onNavigateBack,
            onSignClick = onSignClick,
            onShareClick = onShareClick,
        )
    }

    if (showFinishSigningDialog) {
        OptionSelectionDialog(
            text = stringResource(R.string.signing_operation_completed),
            firstOptionText = stringResource(R.string.share_document),
            onFirstOptionClick = onShareClick,
            secondOptionText = stringResource(R.string.navigate_to_documents),
            onSecondOptionClick = viewModel::hideFinishSigningDialog,
        )
    }

    LaunchedEffect(navigateBack) {
        if (navigateBack) onNavigateBack()
    }
}

@Composable
private fun DocumentsPreviewScreen(
    documentsInfo: List<Document>,
    startPreviewIndex: Int,
    areDocumentsSigned: Boolean,
    onNavigateBack: () -> Unit,
    onSignClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    if (documentsInfo.size == 1) {
        SingleDocumentPreviewScreen(
            document = documentsInfo.first(),
            areDocumentsSigned = areDocumentsSigned,
            onNavigateBack = onNavigateBack,
            onSignClick = onSignClick,
            onShareClick = onShareClick,
        )
    } else {
        MultipleDocumentsPreviewScreen(
            documents = documentsInfo,
            startPreviewIndex = startPreviewIndex,
            areDocumentsSigned = areDocumentsSigned,
            onNavigateBack = onNavigateBack,
            onSignClick = onSignClick,
            onShareClick = onShareClick,
        )
    }
}

@Composable
private fun SingleDocumentPreviewScreen(
    document: Document,
    areDocumentsSigned: Boolean,
    onNavigateBack: () -> Unit,
    onSignClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            RutokenTechLargeTopAppBar(
                titleText = document.title,
                navigationIcon = { AppIcons.Back() },
                onNavigationIconClick = onNavigateBack,
                trailingIcon = if (areDocumentsSigned) {
                    { AppIcons.Share() }
                } else {
                    null
                },
                onTrailingIconClick = if (areDocumentsSigned) {
                    onShareClick
                } else {
                    { /* Nothing to do */ }
                },
                colors = largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            PDFViewer(
                document = document,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            if (!areDocumentsSigned) {
                SecondaryButtonBox(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    text = stringResource(R.string.sign),
                    onClick = onSignClick
                )
            }
        }
    }
}

@Composable
private fun MultipleDocumentsPreviewScreen(
    documents: List<Document>,
    startPreviewIndex: Int,
    areDocumentsSigned: Boolean,
    onNavigateBack: () -> Unit,
    onSignClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Scaffold(topBar = {
        RutokenTechTopAppBar(
            titleText = stringResource(R.string.documents_count, documents.size),
            navigationIcon = { AppIcons.Back() },
            onNavigationIconClick = onNavigateBack,
            trailingIcon = if (areDocumentsSigned) {
                { AppIcons.Share() }
            } else {
                null
            },
            onTrailingIconClick = if (areDocumentsSigned) {
                onShareClick
            } else {
                { /* Nothing to do */ }
            },
            colors = largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            val scope = rememberCoroutineScope()
            val pagerState = rememberPagerState(initialPage = startPreviewIndex, pageCount = { documents.size })
            PrimaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .zIndex(1f)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                documents.forEachIndexed { index, document ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = { Text(document.title) },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { pageIndex ->
                PDFViewer(
                    document = documents[pageIndex],
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (!areDocumentsSigned) {
                SecondaryButtonBox(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    text = stringResource(R.string.sign),
                    onClick = onSignClick
                )
            }
        }
    }
}

@Composable
private fun PDFViewer(document: Document, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PDFView(context, null).apply {
                fromAsset(document.assetName)
                    .pages(document.displayPageIndex)
                    .enableSwipe(false)
                    .enableDoubletap(false)
                    .load()
            }
        }
    )
}
