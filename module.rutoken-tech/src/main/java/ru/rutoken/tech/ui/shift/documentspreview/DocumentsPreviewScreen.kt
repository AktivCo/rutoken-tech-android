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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.components.RutokenTechLargeTopAppBar
import ru.rutoken.tech.ui.components.RutokenTechTopAppBar
import ru.rutoken.tech.ui.components.SecondaryButtonBox
import ru.rutoken.tech.ui.shift.documents.Document

@Composable
fun DocumentsPreviewScreen(onNavigateBack: () -> Unit, viewModel: DocumentsPreviewViewModel = koinViewModel()) {
    val documentsToSign by viewModel.documentsToSign.observeAsState(emptyList())

    if (documentsToSign.isNotEmpty()) {
        DocumentsPreviewScreen(
            documentsToSign = documentsToSign,
            onNavigateBack = onNavigateBack,
            onSignClick = viewModel::onSignClicked
        )
    }
}

@Composable
private fun DocumentsPreviewScreen(
    documentsToSign: List<Document>,
    onNavigateBack: () -> Unit,
    onSignClick: () -> Unit
) {
    if (documentsToSign.size == 1) {
        SingleDocumentPreviewScreen(
            documentToSign = documentsToSign.first(),
            onNavigateBack = onNavigateBack,
            onSignClick = onSignClick
        )
    } else {
        MultipleDocumentsPreviewScreen(
            documentsToSign = documentsToSign,
            onNavigateBack = onNavigateBack,
            onSignClick = onSignClick
        )
    }
}

@Composable
private fun SingleDocumentPreviewScreen(
    documentToSign: Document,
    onNavigateBack: () -> Unit,
    onSignClick: () -> Unit
) {
    Scaffold(
        topBar = {
            RutokenTechLargeTopAppBar(
                titleText = documentToSign.title,
                navigationIcon = { AppIcons.Back() },
                onNavigationIconClick = onNavigateBack,
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
                document = documentToSign,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

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

@Composable
private fun MultipleDocumentsPreviewScreen(
    documentsToSign: List<Document>,
    onNavigateBack: () -> Unit,
    onSignClick: () -> Unit
) {
    Scaffold(topBar = {
        RutokenTechTopAppBar(
            titleText = stringResource(R.string.documents_count, documentsToSign.size),
            navigationIcon = { AppIcons.Back() },
            onNavigationIconClick = onNavigateBack,
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
            val pagerState = rememberPagerState(pageCount = { documentsToSign.size })
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
                documentsToSign.forEachIndexed { index, document ->
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
                    document = documentsToSign[pageIndex],
                    modifier = Modifier.fillMaxSize()
                )
            }

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
