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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.components.AppIcons.Back
import ru.rutoken.tech.ui.components.AppIcons.ExpandData
import ru.rutoken.tech.ui.components.AppIcons.CollapseData
import ru.rutoken.tech.ui.components.AppIcons.DocumentToSign
import ru.rutoken.tech.ui.components.AppIcons.NoFiles
import ru.rutoken.tech.ui.components.AppIcons.ResetData
import ru.rutoken.tech.ui.components.AppIcons.SignedDocument
import ru.rutoken.tech.ui.components.ScreenTopAppBar
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
    onDocumentClicked: (Document) -> Unit,
    isDocumentsToSignSelected: Boolean = true
) {
    val documents by viewModel.documents.observeAsState(listOf())
    val signedDocuments by viewModel.signedDocuments.observeAsState(listOf())

    DocumentsScreen(
        documents = documents,
        signedDocuments = signedDocuments,
        onNavigateBack = onNavigateBack,
        onResetDocumentsClicked = viewModel::onResetDocumentsClicked,
        onDocumentClicked = onDocumentClicked,
        onShareClicked = viewModel::onShareClicked,
        onSignatoriesClicked = viewModel::onSignatoriesClicked,
        isDocumentsToSignSelected = isDocumentsToSignSelected,
    )
}

@Composable
private fun DocumentsScreen(
    documents: List<Document>,
    signedDocuments: List<SignedDocumentsGroup>,
    onNavigateBack: () -> Unit,
    onResetDocumentsClicked: () -> Unit,
    onDocumentClicked: (Document) -> Unit,
    onShareClicked: (SignedDocumentsGroup) -> Unit,
    onSignatoriesClicked: (SignedDocumentsGroup) -> Unit,
    isDocumentsToSignSelected: Boolean = true
) {
    var showDocumentsToSign by rememberSaveable { mutableStateOf(isDocumentsToSignSelected) }

    Scaffold(
        topBar = {
            ScreenTopAppBar(
                screenName = stringResource(id = R.string.documents_title),
                navigationIcon = { Back() },
                onNavigationIconClick = onNavigateBack,
                trailingIcon = { ResetData() },
                onTrailingIconClick = onResetDocumentsClicked
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            SegmentedButtonRow(
                onLeftSectionClicked = { showDocumentsToSign = true },
                onRightSectionClicked = { showDocumentsToSign = false },
                isLeftSectionSelected = showDocumentsToSign,
                leftSectionText = stringResource(R.string.documents_to_sign),
                rightSectionText = stringResource(R.string.signed_documents)
            )
            Spacer(Modifier.height(12.dp))

            if (showDocumentsToSign && documents.isEmpty()) {
                NoDocumentsEmptyState(stringResource(R.string.documents_to_sign_empty_state_title))
            } else if (!showDocumentsToSign && signedDocuments.isEmpty()) {
                NoDocumentsEmptyState(stringResource(R.string.signed_documents_empty_state_title))
            } else {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (showDocumentsToSign) {
                            documentsToSignItems(documents, onDocumentClicked)
                        } else {
                            signedDocumentsItems(
                                signedDocuments,
                                onDocumentClicked,
                                onShareClicked,
                                onSignatoriesClicked
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.documentsToSignItems(
    documents: List<Document>,
    onDocumentClicked: (Document) -> Unit
) {
    val documentsMap = documents
        .sortedByDescending { it.date }
        .groupBy { it.date }
    documentsMap.forEach { (date, docsInDate) ->
        item {
            Text(
                text = date.toDateString(),
                modifier = Modifier.padding(16.dp, 8.dp, 0.dp, 16.dp),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )
        }
        items(docsInDate) { document ->
            DocumentCard(
                document = document,
                icon = { DocumentToSign() },
                onClick = { onDocumentClicked(document) }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun LazyListScope.signedDocumentsItems(
    signedDocuments: List<SignedDocumentsGroup>,
    onDocumentClicked: (Document) -> Unit,
    onShareClicked: (SignedDocumentsGroup) -> Unit,
    onSignatoriesClicked: (SignedDocumentsGroup) -> Unit
) {
    val signedDocumentsMap = signedDocuments
        .sortedByDescending { it.date }
        .groupBy { it.date }
    signedDocumentsMap.forEach { (date, sections) ->
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
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
            documents = listOf(document1, document2, document2, document1),
            signedDocuments = emptyList(),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = true,
            onShareClicked = {},
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
            documents = emptyList(),
            signedDocuments = emptyList(),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = true,
            onShareClicked = {},
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
            date = LocalDate.now()
        )
        val signedDocumentsGroup2 = SignedDocumentsGroup(
            documents = listOf(document2),
            date = LocalDate.of(2023, 12, 10)
        )
        DocumentsScreen(
            documents = listOf(document1, document2, document2, document1),
            signedDocuments = listOf(signedDocumentsGroup1, signedDocumentsGroup1, signedDocumentsGroup2),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = false,
            onShareClicked = {},
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
            documents = emptyList(),
            signedDocuments = emptyList(),
            onNavigateBack = {},
            onResetDocumentsClicked = {},
            onDocumentClicked = {},
            isDocumentsToSignSelected = false,
            onShareClicked = {},
            onSignatoriesClicked = {}
        )
    }
}


