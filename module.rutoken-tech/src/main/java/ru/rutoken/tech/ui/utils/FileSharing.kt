/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import ru.rutoken.tech.R
import ru.rutoken.tech.utils.loge
import java.io.File

private const val PROVIDER_AUTHORITY = "ru.rutoken.tech.fileprovider"

fun Context.startShareChooser(sharedFiles: List<File>) {
    val shareIntentBuilder = ShareCompat.IntentBuilder(this)
        .setType("*/*")
        .setChooserTitle(R.string.share_document)

    for (file in sharedFiles) {
        val uri: Uri = try {
            FileProvider.getUriForFile(this, PROVIDER_AUTHORITY, file)
        } catch (e: IllegalArgumentException) {
            loge<FileProvider>(e) { "The selected file can't be shared: $file" }
            continue
        }

        shareIntentBuilder.addStream(uri)
    }

    shareIntentBuilder.startChooser()
}
