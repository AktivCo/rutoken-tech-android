/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.pdf

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.use

@Composable
fun PDFViewer(
    pdfFile: File,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val bitmaps = remember(pdfFile) { mutableStateListOf<ImageBitmap>() }

    LaunchedEffect(pdfFile) {
        withContext(Dispatchers.Default) {
            bitmaps.addAll(renderPDF(pdfFile, density))
        }
    }

    PDFViewer(bitmaps, modifier)
}

@Composable
fun PDFViewer(
    bitmaps: List<ImageBitmap>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(bitmaps) { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "PDF Page",
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .background(Color.White)
                    .fillMaxWidth()
            )
        }
    }
}

fun renderPDF(pdfFile: File, density: Float): List<ImageBitmap> {
    val bitmaps = mutableListOf<ImageBitmap>()
    runCatching {
        ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { renderer ->
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->
                        val bmpWidth = (page.width * density).toInt().coerceAtLeast(1)
                        val bmpHeight = (page.height * density).toInt().coerceAtLeast(1)
                        val bmp = createBitmap(bmpWidth, bmpHeight)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmaps.add(bmp.asImageBitmap())
                    }
                }
            }
        }
    }.onFailure { throwable ->
        throwable.printStackTrace()
    }

    return bitmaps
}
