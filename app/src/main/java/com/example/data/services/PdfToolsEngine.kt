package com.example.data.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class PdfToolsEngine(private val context: Context) {

    private val renderMutex = Mutex()

    fun resolveWorkspaceFile(relativePath: String): File = File(context.filesDir, relativePath)

    suspend fun getPageCount(file: File): Int = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext 1
        runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer -> renderer.pageCount }
            }
        }.getOrDefault(1)
    }

    suspend fun renderPageBitmap(file: File, pageIndex: Int, targetWidthPx: Int = 1080): Bitmap? =
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext null
            renderMutex.withLock {
                runCatching {
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            val safeIndex = pageIndex.coerceIn(0, (renderer.pageCount - 1).coerceAtLeast(0))
                            renderer.openPage(safeIndex).use { page ->
                                val scale = targetWidthPx.toFloat() / page.width.coerceAtLeast(1)
                                val height = (page.height * scale).toInt().coerceAtLeast(1)
                                val bitmap = Bitmap.createBitmap(targetWidthPx, height, Bitmap.Config.ARGB_8888)
                                bitmap.eraseColor(Color.TRANSPARENT)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmap
                            }
                        }
                    }
                }.getOrNull()
            }
        }

    suspend fun seedNotesPdf(dest: File): Int = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        val doc = PdfDocument()
        val pages = listOf(
            PdfSeedPage(
                date = "Oct 14",
                title = "Workspace Architecture & Reader Polish",
                paragraphs = listOf(
                    "1. Core Rendering Pipeline: Page rasterization completes smoothly by caching adjacent viewport tiles in memory.",
                    "2. Annotation Storage: Highlights, ink strokes, and bookmarks stay neatly organized alongside each document.",
                    "3. Typography & Contrast: Body copy maintains a 1.45x line height with ink-navy headings to reduce eye strain during extended reading sessions."
                ),
                bulletsTitle = "Next Milestone Action Items",
                bullets = listOf(
                    "Finalize 48dp touch targets on floating toolbars",
                    "Verify PDF signature embedding",
                    "Confirm clean typography across all screens"
                )
            ),
            PdfSeedPage(
                date = "Oct 15",
                title = "Frosted Glass & Surface Hierarchy",
                paragraphs = listOf(
                    "Surface elevation relies on translucent white cards over a gentle diagonal pastel canvas rather than heavy opaque dividers.",
                    "Primary actions use deep cobalt (#1F55E0) paired with crisp white iconography for effortless scanability in bright daylight."
                ),
                bulletsTitle = "Visual Tokens",
                bullets = listOf(
                    "Canvas gradient: #D9E6FF -> #EADFFF -> #FFE6E0",
                    "Ink primary: #0C1B35 · Ink secondary: #434655",
                    "Rounded glass containers: 24dp radius"
                )
            ),
            PdfSeedPage(
                date = "Oct 18",
                title = "Uncompressed WAV Capture",
                paragraphs = listOf(
                    "Voice memos capture 16-bit PCM mono audio at 16 kHz with real-time RMS amplitude sampling.",
                    "Recordings include instant single-tap playback and clean waveform visualization."
                ),
                bulletsTitle = "Recording Checklist",
                bullets = listOf(
                    "Verify RECORD_AUDIO permission flow",
                    "Update RIFF header byte counts cleanly on stop",
                    "Provide instant single-tap play/pause controls"
                )
            ),
            PdfSeedPage(
                date = "Oct 23",
                title = "PDF Toolkit & Document Flows",
                paragraphs = listOf(
                    "Merge, split, compress, rotate, extract, and password-protect PDF documents cleanly.",
                    "Gallery imports and document picking integrate directly with the Android Photo Picker and system file chooser."
                ),
                bulletsTitle = "Key Capabilities",
                bullets = listOf(
                    "High-resolution PDF page rendering",
                    "Quick previews and full-screen annotations",
                    "Read-aloud voice preview support"
                )
            )
        )

        pages.forEachIndexed { idx, spec ->
            drawSeedPage(doc, idx + 1, spec)
        }

        FileOutputStream(dest).use { out -> doc.writeTo(out) }
        doc.close()
        pages.size
    }

    suspend fun seedReceiptPdf(dest: File): Int = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        val doc = PdfDocument()
        drawSeedPage(
            doc, 1,
            PdfSeedPage(
                date = "5 Oct",
                title = "Stationery & Studio Supplies Receipt",
                paragraphs = listOf(
                    "Merchant: Paper & Ink Co. — High Street Branch",
                    "Items: Archival Dot Grid Notebook (2x), Fountain Ink Bottle, Brass Page Markers.",
                    "Total Paid: £38.40 · Payment Method: Card."
                ),
                bulletsTitle = "Filing Tags",
                bullets = listOf(
                    "Category: Studio Supplies",
                    "Tax Deductible: Yes",
                    "Status: Archived"
                )
            )
        )
        drawSeedPage(
            doc, 2,
            PdfSeedPage(
                date = "5 Oct",
                title = "Warranty & Return Policy Summary",
                paragraphs = listOf(
                    "Keep this PDF copy for 12-month hardware and stationery warranty support.",
                    "Present the reference ID below for exchanges or returns."
                ),
                bulletsTitle = "Details",
                bullets = listOf(
                    "Reference ID: PI-2026-0842",
                    "Return Window: 30 Days"
                )
            )
        )
        FileOutputStream(dest).use { out -> doc.writeTo(out) }
        doc.close()
        2
    }

    suspend fun mergePdfs(sources: List<File>, dest: File): Int = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        val outDoc = PdfDocument()
        var pageCounter = 0
        sources.forEach { src ->
            if (src.exists()) {
                ParcelFileDescriptor.open(src, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        for (i in 0 until renderer.pageCount) {
                            pageCounter++
                            renderer.openPage(i).use { srcPage ->
                                val bmp = renderToBitmap(srcPage, scale = 1.5f)
                                val info = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, pageCounter).create()
                                val outPage = outDoc.startPage(info)
                                outPage.canvas.drawBitmap(bmp, 0f, 0f, null)
                                outDoc.finishPage(outPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
            }
        }
        FileOutputStream(dest).use { out -> outDoc.writeTo(out) }
        outDoc.close()
        pageCounter
    }

    suspend fun splitPdf(source: File, outputDir: File, baseName: String): List<File> =
        withContext(Dispatchers.IO) {
            outputDir.mkdirs()
            val outputs = mutableListOf<File>()
            ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    for (i in 0 until renderer.pageCount) {
                        val singleDoc = PdfDocument()
                        renderer.openPage(i).use { srcPage ->
                            val bmp = renderToBitmap(srcPage, scale = 1.5f)
                            val info = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, 1).create()
                            val page = singleDoc.startPage(info)
                            page.canvas.drawBitmap(bmp, 0f, 0f, null)
                            singleDoc.finishPage(page)
                            bmp.recycle()
                        }
                        val outFile = File(outputDir, "${baseName}_page_${i + 1}.pdf")
                        FileOutputStream(outFile).use { out -> singleDoc.writeTo(out) }
                        singleDoc.close()
                        outputs.add(outFile)
                    }
                }
            }
            outputs
        }

    suspend fun compressPdf(source: File, dest: File, jpegQuality: Int = 55): Int =
        withContext(Dispatchers.IO) {
            dest.parentFile?.mkdirs()
            val outDoc = PdfDocument()
            var count = 0
            ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    count = renderer.pageCount
                    for (i in 0 until count) {
                        renderer.openPage(i).use { srcPage ->
                            val bmp = renderToBitmap(srcPage, scale = 1.0f)
                            val baos = ByteArrayOutputStream()
                            bmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos)
                            bmp.recycle()
                            val compressedBytes = baos.toByteArray()
                            val compressedBmp = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
                            val info = PdfDocument.PageInfo.Builder(compressedBmp.width, compressedBmp.height, i + 1).create()
                            val page = outDoc.startPage(info)
                            page.canvas.drawBitmap(compressedBmp, 0f, 0f, null)
                            outDoc.finishPage(page)
                            compressedBmp.recycle()
                        }
                    }
                }
            }
            FileOutputStream(dest).use { out -> outDoc.writeTo(out) }
            outDoc.close()
            count
        }

    suspend fun rotatePdf(source: File, dest: File, degrees: Int): Int =
        withContext(Dispatchers.IO) {
            dest.parentFile?.mkdirs()
            val outDoc = PdfDocument()
            var count = 0
            ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    count = renderer.pageCount
                    for (i in 0 until count) {
                        renderer.openPage(i).use { srcPage ->
                            val bmp = renderToBitmap(srcPage, scale = 1.5f)
                            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                            val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                            bmp.recycle()
                            val info = PdfDocument.PageInfo.Builder(rotated.width, rotated.height, i + 1).create()
                            val page = outDoc.startPage(info)
                            page.canvas.drawBitmap(rotated, 0f, 0f, null)
                            outDoc.finishPage(page)
                            rotated.recycle()
                        }
                    }
                }
            }
            FileOutputStream(dest).use { out -> outDoc.writeTo(out) }
            outDoc.close()
            count
        }

    suspend fun extractPages(source: File, dest: File, oneBasedPages: List<Int>): Int =
        withContext(Dispatchers.IO) {
            dest.parentFile?.mkdirs()
            val outDoc = PdfDocument()
            var outCount = 0
            ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val validPages = oneBasedPages
                        .map { it - 1 }
                        .filter { it in 0 until renderer.pageCount }
                        .distinct()
                    validPages.forEachIndexed { idx, pageIdx ->
                        renderer.openPage(pageIdx).use { srcPage ->
                            val bmp = renderToBitmap(srcPage, scale = 1.5f)
                            val info = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, idx + 1).create()
                            val page = outDoc.startPage(info)
                            page.canvas.drawBitmap(bmp, 0f, 0f, null)
                            outDoc.finishPage(page)
                            bmp.recycle()
                            outCount++
                        }
                    }
                }
            }
            FileOutputStream(dest).use { out -> outDoc.writeTo(out) }
            outDoc.close()
            outCount
        }

    suspend fun imagesToPdf(uris: List<Uri>, dest: File): Int = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        val outDoc = PdfDocument()
        var pageIndex = 0
        uris.forEach { uri ->
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                if (bmp != null) {
                    pageIndex++
                    val pageWidth = 1080
                    val pageHeight = 1440
                    val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
                    val page = outDoc.startPage(info)
                    page.canvas.drawColor(Color.WHITE)
                    val margin = 48f
                    val availW = pageWidth - margin * 2
                    val availH = pageHeight - margin * 2
                    val scale = minOf(availW / bmp.width, availH / bmp.height)
                    val drawW = bmp.width * scale
                    val drawH = bmp.height * scale
                    val left = (pageWidth - drawW) / 2f
                    val top = (pageHeight - drawH) / 2f
                    page.canvas.drawBitmap(bmp, null, RectF(left, top, left + drawW, top + drawH), null)
                    outDoc.finishPage(page)
                    bmp.recycle()
                }
            }
        }
        FileOutputStream(dest).use { out -> outDoc.writeTo(out) }
        outDoc.close()
        pageIndex
    }

    suspend fun watermarkPdf(source: File, dest: File, watermarkText: String): Int =
        withContext(Dispatchers.IO) {
            dest.parentFile?.mkdirs()
            val outDoc = PdfDocument()
            var count = 0
            val label = watermarkText.ifBlank { "CONFIDENTIAL" }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(68, 31, 85, 224)
                textSize = 64f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    count = renderer.pageCount
                    for (i in 0 until count) {
                        renderer.openPage(i).use { srcPage ->
                            val bmp = renderToBitmap(srcPage, scale = 1.5f)
                            val info = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i + 1).create()
                            val page = outDoc.startPage(info)
                            val canvas = page.canvas
                            canvas.drawBitmap(bmp, 0f, 0f, null)
                            canvas.save()
                            canvas.translate(bmp.width / 2f, bmp.height / 2f)
                            canvas.rotate(-32f)
                            canvas.drawText(label, 0f, 0f, paint)
                            canvas.restore()
                            outDoc.finishPage(page)
                            bmp.recycle()
                        }
                    }
                }
            }
            FileOutputStream(dest).use { out -> outDoc.writeTo(out) }
            outDoc.close()
            count
        }

    suspend fun saveBitmapAsPdf(bitmap: Bitmap, dest: File): Int = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        val outDoc = PdfDocument()
        val pageWidth = 1080
        val pageHeight = 1440
        val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = outDoc.startPage(info)
        page.canvas.drawColor(Color.WHITE)
        val margin = 48f
        val availW = pageWidth - margin * 2
        val availH = pageHeight - margin * 2
        val scale = minOf(availW / bitmap.width.coerceAtLeast(1), availH / bitmap.height.coerceAtLeast(1))
        val drawW = bitmap.width * scale
        val drawH = bitmap.height * scale
        val left = (pageWidth - drawW) / 2f
        val top = (pageHeight - drawH) / 2f
        page.canvas.drawBitmap(bitmap, null, RectF(left, top, left + drawW, top + drawH), null)
        outDoc.finishPage(page)
        FileOutputStream(dest).use { out -> outDoc.writeTo(out) }
        outDoc.close()
        1
    }

    suspend fun saveBitmapAsJpeg(bitmap: Bitmap, dest: File, quality: Int = 92): Long =
        withContext(Dispatchers.IO) {
            dest.parentFile?.mkdirs()
            FileOutputStream(dest).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            dest.length()
        }

    suspend fun seedSamplePhotoNoteImage(dest: File): Long = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        val bmp = Bitmap.createBitmap(720, 480, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#E8EEFF"))
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(RectF(48f, 48f, 672f, 432f), 28f, 28f, cardPaint)
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1F55E0") }
        canvas.drawCircle(116f, 116f, 26f, accentPaint)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0C1B35")
            textSize = 30f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("Whiteboard Layout Sketch", 168f, 126f, titlePaint)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6B7280")
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(RectF(92f, 180f, 320f, 360f), 18f, 18f, linePaint)
        canvas.drawRoundRect(RectF(360f, 180f, 628f, 260f), 18f, 18f, linePaint)
        canvas.drawRoundRect(RectF(360f, 280f, 628f, 360f), 18f, 18f, linePaint)
        FileOutputStream(dest).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        bmp.recycle()
        dest.length()
    }

    suspend fun copyUriToFile(uri: Uri, dest: File): Long = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        } ?: 0L
        dest.length()
    }

    suspend fun encryptFileWithPassword(
        plainFile: File,
        encryptedDest: File,
        password: String
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        encryptedDest.parentFile?.mkdirs()
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val keyBytes = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(128, iv))
        val plainBytes = plainFile.readBytes()
        val cipherBytes = cipher.doFinal(plainBytes)

        FileOutputStream(encryptedDest).use { out ->
            out.write(iv)
            out.write(cipherBytes)
        }
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = deriveVerifierHash(password, salt)
        saltHex to hashHex
    }

    suspend fun verifyAndDecrypt(
        encryptedFile: File,
        plainDest: File,
        password: String,
        saltHex: String,
        hashHex: String
    ): Boolean = withContext(Dispatchers.IO) {
        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        if (deriveVerifierHash(password, salt) != hashHex) return@withContext false
        runCatching {
            val all = encryptedFile.readBytes()
            val iv = all.copyOfRange(0, 12)
            val cipherBytes = all.copyOfRange(12, all.size)
            val keyBytes = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(128, iv))
            val plain = cipher.doFinal(cipherBytes)
            plainDest.parentFile?.mkdirs()
            plainDest.writeBytes(plain)
            true
        }.getOrDefault(false)
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 12_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun deriveVerifierHash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec((password + ":verify").toCharArray(), salt, 6_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun renderToBitmap(page: PdfRenderer.Page, scale: Float): Bitmap {
        val w = (page.width * scale).toInt().coerceAtLeast(1)
        val h = (page.height * scale).toInt().coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bmp
    }

    private data class PdfSeedPage(
        val date: String,
        val title: String,
        val paragraphs: List<String>,
        val bulletsTitle: String,
        val bullets: List<String>
    )

    private fun drawSeedPage(doc: PdfDocument, pageNumber: Int, spec: PdfSeedPage) {
        val width = 595
        val height = 842
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageNumber).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 48f
        val maxWidth = width - margin * 2
        var y = 64f

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#434655")
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(spec.date, width - margin, y, datePaint)
        y += 24f

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0C1B35")
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        y = drawWrappedText(canvas, spec.title, margin, y, maxWidth, titlePaint, 26f)
        y += 14f

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#434655")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        spec.paragraphs.forEach { para ->
            y = drawWrappedText(canvas, para, margin, y, maxWidth, bodyPaint, 20f)
            y += 12f
        }

        y += 8f
        val boxTop = y
        val boxHeight = 36f + spec.bullets.size * 24f + 16f
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 255, 255, 255)
        }
        canvas.drawRoundRect(RectF(margin, boxTop, width - margin, boxTop + boxHeight), 16f, 16f, boxPaint)

        val boxTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0C1B35")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        y = boxTop + 26f
        canvas.drawText(spec.bulletsTitle, margin + 16f, y, boxTitlePaint)
        y += 22f

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F55E0")
        }
        spec.bullets.forEach { bullet ->
            canvas.drawCircle(margin + 22f, y - 4f, 3.5f, dotPaint)
            canvas.drawText(bullet, margin + 34f, y, bodyPaint)
            y += 24f
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#747686")
            textSize = 10.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Reader · Document · Page $pageNumber", width / 2f, height - 32f, footerPaint)

        doc.finishPage(page)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float
    ): Float {
        var y = startY
        val words = text.split(" ")
        val line = StringBuilder()
        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth) {
                canvas.drawText(line.toString(), x, y, paint)
                y += lineHeight
                line.clear()
                line.append(word)
            } else {
                line.clear()
                line.append(candidate)
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), x, y, paint)
            y += lineHeight
        }
        return y
    }
}
