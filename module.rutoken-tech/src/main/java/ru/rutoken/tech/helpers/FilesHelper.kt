/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.helpers

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FilesHelper(private val context: Context) {
    suspend fun copyAssetToCache(assetName: String, fileName: String): File =
        withContext(Dispatchers.IO) {
            context.assets.open(assetName).use { createCacheFile(fileName, it.readBytes()) }
        }

    suspend fun createSignatureFile(fileName: String, signatureBytes: ByteArray): File =
        withContext(Dispatchers.IO) {
            createCacheFile(fileName, signatureBytes)
        }

    suspend fun createZipFile(
        files: List<File>,
        @StringRes zipNameResID: Int,
        vararg zipNameFormatArgs: Any,
    ): File = withContext(Dispatchers.IO) {
        val outputZipFile = File(context.cacheDir, "/${context.getString(zipNameResID, *zipNameFormatArgs)}")
        ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOut ->
            files.forEach { file ->
                FileInputStream(file).use { inputStream ->
                    val zipEntry = ZipEntry(file.name)
                    zipOut.putNextEntry(zipEntry)
                    inputStream.copyTo(zipOut)
                    zipOut.closeEntry()
                }
            }
        }

        outputZipFile
    }

    private fun createCacheFile(fileName: String, content: ByteArray): File =
        File(context.cacheDir, "/$fileName").also { it.writeBytes(content) }
}
