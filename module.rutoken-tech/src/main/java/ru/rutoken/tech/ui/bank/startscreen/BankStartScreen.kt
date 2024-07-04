/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.startscreen

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rutoken.tech.R
import ru.rutoken.tech.ui.bank.BankUser
import ru.rutoken.tech.ui.bank.CertificateCard
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.components.alertdialog.ConfirmationAlertDialog
import ru.rutoken.tech.ui.components.PrimaryButtonBox
import ru.rutoken.tech.ui.components.ScreenTopAppBar
import ru.rutoken.tech.ui.theme.RutokenTechTheme
import ru.rutoken.tech.ui.theme.bodyMediumOnSurfaceVariant
import ru.rutoken.tech.ui.utils.PreviewDark
import ru.rutoken.tech.ui.utils.PreviewLight

@Composable
fun BankStartScreen(
    viewModel: BankStartScreenViewModel,
    onUserClicked: (Int) -> Unit,
    onAddUserClicked: () -> Unit,
    openDrawer: () -> Unit
) {
    val users by viewModel.users.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val showDeleteUsersDialog by viewModel.showDeleteUsersDialog.observeAsState(false)
    if (showDeleteUsersDialog) {
        ConfirmationAlertDialog(
            text = stringResource(id = R.string.delete_all_users),
            dismissText = stringResource(id = R.string.cancel),
            confirmText = stringResource(id = R.string.delete),
            onDismiss = viewModel::dismissDeleteUsersDialog,
            onConfirm = viewModel::deleteAllUsers
        )
    }

    BankStartScreen(
        users = users,
        onDeleteUsers = viewModel::showDeleteUsersDialog,
        onUserClicked = onUserClicked,
        onAddUserClicked = onAddUserClicked,
        openDrawer = openDrawer
    )
}

@Composable
private fun BankStartScreen(
    users: List<BankUser>,
    onDeleteUsers: () -> Unit,
    onUserClicked: (Int) -> Unit,
    onAddUserClicked: () -> Unit,
    openDrawer: () -> Unit
) {
    val trailingIcon = @Composable { AppIcons.Delete() }

    Scaffold(
        topBar = {
            ScreenTopAppBar(
                screenName = stringResource(id = R.string.tab_bank),
                openDrawer = openDrawer,
                trailingIcon = if (users.isNotEmpty()) trailingIcon else null,
                onTrailingIconClick = onDeleteUsers
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            UserList(modifier = Modifier.weight(1f, false), users = users, onUserClicked = onUserClicked)
            PrimaryButtonBox(text = stringResource(id = R.string.add_user)) {
                onAddUserClicked()
            }
        }
    }
}

@Composable
private fun UserList(modifier: Modifier, users: List<BankUser>, onUserClicked: (Int) -> Unit) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        val verticalAlignment = if (users.isEmpty()) Alignment.CenterVertically else Alignment.Top
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, verticalAlignment)
        ) {
            if (users.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_added_users),
                    style = bodyMediumOnSurfaceVariant,
                )
            } else {
                users.forEach {
                    CertificateCard(
                        name = it.name,
                        position = it.position ?: stringResource(R.string.not_set),
                        certificateExpirationDate = it.certificateExpirationDate,
                        errorText = it.errorText,
                        onClick = { onUserClicked(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun BankStartScreenWithUsersPreview() {
    val testUsers = listOf(
        BankUser(0, "Иванов Михаил Романович", "Дизайнер", "07.03.2024"),
        BankUser(1, "Иванов Михаил Романович", "Дизайнер", "08.03.2024"),
        BankUser(2, "Иванов Михаил Романович", "Дизайнер", "07.03.2024", "Срок действия сертификата истек"),
        BankUser(5, "Иванов Михаил Романович", "Дизайнер", "07.03.2024", "Срок действия сертификата ещё не наступил")
    )

    RutokenTechTheme {
        BankStartScreen(
            users = testUsers,
            onDeleteUsers = { },
            onUserClicked = {},
            onAddUserClicked = {},
            openDrawer = {}
        )
    }
}

@Composable
@PreviewLight
@PreviewDark
private fun BankStartScreenNoUsersPreview() {
    RutokenTechTheme {
        BankStartScreen(
            users = listOf(),
            onDeleteUsers = { },
            onUserClicked = {},
            onAddUserClicked = {},
            openDrawer = {}
        )
    }
}