/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documents

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.AppIcons.Back
import ru.rutoken.tech.ui.components.AppIcons.Clear
import ru.rutoken.tech.ui.components.AppIcons.CollapseData
import ru.rutoken.tech.ui.components.AppIcons.DocumentToSign
import ru.rutoken.tech.ui.components.AppIcons.ExpandData
import ru.rutoken.tech.ui.components.AppIcons.NoFiles
import ru.rutoken.tech.ui.components.AppIcons.ResetData
import ru.rutoken.tech.ui.components.AppIcons.SelectedDocument
import ru.rutoken.tech.ui.components.AppIcons.SignedDocument
import ru.rutoken.tech.ui.components.RutokenTechLargeTopAppBar
import ru.rutoken.tech.ui.components.RutokenTechTopAppBar
import ru.rutoken.tech.ui.components.SecondaryButtonBox
import ru.rutoken.tech.ui.components.SegmentedButtonRow
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight
import ru.rutoken.tech.ui.utils.figmaPadding
import ru.rutoken.tech.utils.toDateString
import java.time.LocalDate

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    onNavigateBack: () -> Unit,
    isDocumentsToSignSelected: Boolean = true
) {
    val documents by viewModel.documents.observeAsState(mapOf())
    val signedDocuments by viewModel.signedDocuments.observeAsState(mapOf())
    val documentsToSign by viewModel.documentsToSign.observeAsState(emptyList())
    val signatoriesBottomSheetData by viewModel.documentsGroupSignatories.observeAsState(emptyList())

    if (signatoriesBottomSheetData.isNotEmpty()) {
        DocumentSignatoriesBottomSheet(
            signatories = signatoriesBottomSheetData,
            onDismissRequest = viewModel::onSignatoriesBottomSheetClose
        )
    }

    val onDocumentClicked = { document: Document ->
        if (documentsToSign.isEmpty()) {
            viewModel.onDocumentSelected(document)
            viewModel.onNavigateToPreview()
        } else {
            viewModel.onDocumentSelected(document)
        }
    }

    DocumentsScreen(
        documents = documents,
        signedDocuments = signedDocuments,
        documentsToSign = documentsToSign,
        onNavigateBack = onNavigateBack,
        onResetDocumentsClicked = viewModel::onResetDocumentsClicked,
        onDocumentClicked = onDocumentClicked,
        onShareClicked = viewModel::onShareClicked,
        onSignatoriesClicked = viewModel::onSignatoriesClicked,
        onLongClickDocument = viewModel::onDocumentSelected,
        onResetSelectedDocumentsClicked = viewModel::onResetSelectedDocumentsClicked,
        onNavigateToDocumentsPreview = viewModel::onNavigateToPreview,
        isDocumentsToSignSelected = isDocumentsToSignSelected,
    )
}

@Composable
private fun DocumentsScreen(
    documents: Map<LocalDate, List<Document>>,
    signedDocuments: Map<LocalDate, List<SignedDocumentsGroup>>,
    documentsToSign: List<Document>,
    onNavigateBack: () -> Unit,
    onResetDocumentsClicked: () -> Unit,
    onDocumentClicked: (Document) -> Unit,
    onShareClicked: (SignedDocumentsGroup) -> Unit,
    onSignatoriesClicked: (SignedDocumentsGroup) -> Unit,
    onLongClickDocument: (Document) -> Unit,
    onResetSelectedDocumentsClicked: () -> Unit,
    onNavigateToDocumentsPreview: () -> Unit,
    isDocumentsToSignSelected: Boolean = true
) {
    var showDocumentsToSign by rememberSaveable { mutableStateOf(isDocumentsToSignSelected) }

    Scaffold(
        topBar = {
            if (documentsToSign.isEmpty()) {
                RutokenTechLargeTopAppBar(
                    titleText = stringResource(id = R.string.documents_title),
                    navigationIcon = { Back() },
                    onNavigationIconClick = onNavigateBack,
                    trailingIcon = { ResetData() },
                    onTrailingIconClick = onResetDocumentsClicked
                )
            } else {
                RutokenTechTopAppBar(
                    titleText = stringResource(id = R.string.documents_selected_state_title, documentsToSign.size),
                    navigationIcon = { Clear() },
                    onNavigationIconClick = onResetSelectedDocumentsClicked,
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(paddingValues)
        ) {
            if (documentsToSign.isEmpty()) {
                SegmentedButtonRow(
                    onLeftSectionClicked = { showDocumentsToSign = true },
                    onRightSectionClicked = { showDocumentsToSign = false },
                    isLeftSectionSelected = showDocumentsToSign,
                    leftSectionText = stringResource(R.string.documents_to_sign),
                    rightSectionText = stringResource(R.string.signed_documents),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
            if (showDocumentsToSign && documents.isEmpty()) {
                NoDocumentsEmptyState(stringResource(R.string.documents_to_sign_empty_state_title))
            } else if (!showDocumentsToSign && signedDocuments.isEmpty()) {
                NoDocumentsEmptyState(stringResource(R.string.signed_documents_empty_state_title))
            } else {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        if (showDocumentsToSign) {
                            documentsToSignItems(documents, documentsToSign, onDocumentClicked, onLongClickDocument)
                        } else {
                            signedDocumentsItems(
                                signedDocuments,
                                onDocumentClicked,
                                onShareClicked,
                                onSignatoriesClicked
                            )
                        }

                        if (documentsToSign.isEmpty()) {
                            item {
                                Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                            }
                        }
                    }
                    if (documentsToSign.isNotEmpty()) {
                        SecondaryButtonBox(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .fillMaxWidth()
                                .padding(bottom = paddingValues.calculateBottomPadding()),
                            text = stringResource(R.string.documents_selected_state_preview),
                            onClick = onNavigateToDocumentsPreview
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.documentsToSignItems(
    documents: Map<LocalDate, List<Document>>,
    documentsToSign: List<Document>,
    onDocumentClicked: (Document) -> Unit,
    onLongClickDocument: (Document) -> Unit
) {
    documents.forEach { (date, docsInDate) ->
        item {
            Text(
                text = date.toDateString(),
                modifier = Modifier.padding(16.dp, 8.dp, 0.dp, 16.dp),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )
        }
        items(docsInDate) { document ->
            val isDocumentSelected = documentsToSign.contains(document)
            DocumentCard(
                document = document,
                backgroundColor = if (isDocumentSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                icon = {
                    if (isDocumentSelected) {
                        SelectedDocument()
                    } else {
                        DocumentToSign()
                    }
                },
                onClick = { onDocumentClicked(document) },
                onLongClick = { onLongClickDocument(document) }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun LazyListScope.signedDocumentsItems(
    signedDocuments: Map<LocalDate, List<SignedDocumentsGroup>>,
    onDocumentClicked: (Document) -> Unit,
    onShareClicked: (SignedDocumentsGroup) -> Unit,
    onSignatoriesClicked: (SignedDocumentsGroup) -> Unit
) {
    signedDocuments.forEach { (date, sections) ->
        item {
            Text(
                text = date.toDateString(),
                modifier = Modifier.figmaPadding(8.dp, 16.dp, 0.dp, 16.dp),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )
        }
        items(sections) { signedDocs ->
            ExpandableDocumentsSection(signedDocs, onDocumentClicked, onShareClicked, onSignatoriesClicked)
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExpandableDocumentsSection(
    signedDocumentsGroup: SignedDocumentsGroup,
    onDocumentClicked: (Document) -> Unit,
    onShareClicked: (SignedDocumentsGroup) -> Unit,
    onSignatoriesClicked: (SignedDocumentsGroup) -> Unit
) {
    val isExpandable = (signedDocumentsGroup.documents.count() != 1)
    var isExpanded by rememberSaveable { mutableStateOf(!isExpandable) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (isExpandable) isExpanded = !isExpanded }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.signed_documents_count, signedDocumentsGroup.documents.size),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            )
            if (isExpandable) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    if (isExpanded) CollapseData() else ExpandData()
                }
            }
        }
        if (isExpanded) {
            signedDocumentsGroup.documents.forEachIndexed { index, doc ->
                DocumentCard(document = doc, icon = { SignedDocument() }, onClick = { onDocumentClicked(doc) })
                if (index != signedDocumentsGroup.documents.lastIndex)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            }
        }
    }
    SignedDocumentsActions(
        signedDocumentsGroup,
        onShareClicked,
        onSignatoriesClicked,
    )
}

@Composable
private fun SignedDocumentsActions(
    signedDocumentsGroup: SignedDocumentsGroup,
    onShareClicked: (SignedDocumentsGroup) -> Unit,
    onSignatoriesClicked: (SignedDocumentsGroup) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
        Box(modifier = Modifier.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            OutlinedButton(
                onClick = { onSignatoriesClicked(signedDocumentsGroup) },
                modifier = Modifier.height(40.dp)
            ) {
                Text(text = stringResource(R.string.signatories_button), style = MaterialTheme.typography.labelLarge)
            }
        }
        SecondaryButtonBox(
            modifier = Modifier,
            text = stringResource(R.string.share_document),
            padding = PaddingValues(start = 8.dp, top = 16.dp),
            onClick = { onShareClicked(signedDocumentsGroup) }
        )
    }
}

@Composable
private fun DocumentCard(
    document: Document,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    onLongClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(backgroundColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = document.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = document.organization,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NoDocumentsEmptyState(text: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NoFiles()
        Text(
            text = text,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun DocumentsScreenPreview() {
    RutokenTechTheme {
        val document1 = Document(
            title = "Инструктаж по ТБ №4",
            date = LocalDate.now(),
            organization = "ООО “МосЭнерго”",
        )
        val document2 = Document(
            title = "Инструктаж по ТБ №3",
            date = LocalDate.of(2023, 12, 10),
            organization = "ООО “МосЭнерго”"
        )
        DocumentsScreen(
            documents = mapOf(Pair(LocalDate.now(), listOf(document1, document2, document2, document1))),
            signedDocuments = emptyMap(),
            documentsToSign = emptyList(),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = true,
            onShareClicked = {},
            onLongClickDocument = {},
            onResetSelectedDocumentsClicked = {},
            onNavigateToDocumentsPreview = {},
            onSignatoriesClicked = {}
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun EmptyDocumentsScreenPreview() {
    RutokenTechTheme {
        DocumentsScreen(
            documents = emptyMap(),
            signedDocuments = emptyMap(),
            documentsToSign = emptyList(),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = true,
            onShareClicked = {},
            onLongClickDocument = {},
            onResetSelectedDocumentsClicked = {},
            onNavigateToDocumentsPreview = {},
            onSignatoriesClicked = {}
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun SignedDocumentsScreenPreview() {
    RutokenTechTheme {
        val document1 = Document(
            title = "Инструктаж по ТБ №4",
            date = LocalDate.now(),
            organization = "ООО “МосЭнерго”",
        )
        val document2 = Document(
            title = "Инструктаж по ТБ №3",
            date = LocalDate.of(2023, 12, 10),
            organization = "ООО “МосЭнерго”"
        )
        val signedDocumentsGroup1 = SignedDocumentsGroup(
            documents = listOf(document1, document1, document2),
            date = LocalDate.now(),
            signatories = emptyList()
        )
        val signedDocumentsGroup2 = SignedDocumentsGroup(
            documents = listOf(document2),
            date = LocalDate.of(2023, 12, 10),
            signatories = emptyList()
        )
        DocumentsScreen(
            documents = emptyMap(),
            signedDocuments = mapOf(
                Pair(
                    LocalDate.now(),
                    listOf(signedDocumentsGroup1, signedDocumentsGroup1, signedDocumentsGroup2)
                )
            ),
            documentsToSign = emptyList(),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = false,
            onShareClicked = {},
            onLongClickDocument = {},
            onResetSelectedDocumentsClicked = {},
            onNavigateToDocumentsPreview = {},
            onSignatoriesClicked = {}
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun EmptySignedDocumentsScreenPreview() {
    RutokenTechTheme {
        DocumentsScreen(
            documents = emptyMap(),
            signedDocuments = emptyMap(),
            documentsToSign = emptyList(),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = false,
            onShareClicked = {},
            onLongClickDocument = {},
            onResetSelectedDocumentsClicked = {},
            onNavigateToDocumentsPreview = {},
            onSignatoriesClicked = {}
        )
    }
}

@PreviewLight
@PreviewDark
@Composable
private fun SelectedDocumentsScreenPreview() {
    RutokenTechTheme {
        val document1 = Document(
            title = "Инструктаж по ТБ №4",
            date = LocalDate.now(),
            organization = "ООО “МосЭнерго”",
        )
        val document2 = Document(
            title = "Инструктаж по ТБ №3",
            date = LocalDate.of(2023, 12, 10),
            organization = "ООО “МосЭнерго”"
        )
        DocumentsScreen(
            documents = mapOf(Pair(LocalDate.now(), listOf(document1, document2, document2, document1))),
            signedDocuments = emptyMap(),
            documentsToSign = listOf(document1),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = true,
            onShareClicked = {},
            onLongClickDocument = {},
            onResetSelectedDocumentsClicked = {},
            onNavigateToDocumentsPreview = {},
            onSignatoriesClicked = {}
        )
    }
}
