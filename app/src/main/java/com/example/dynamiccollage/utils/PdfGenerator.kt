package com.example.dynamiccollage.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import com.example.dynamiccollage.data.model.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import com.tom_roush.pdfbox.pdmodel.PDDocument

object PdfGenerator {

    private enum class ImageAlignment { TOP, BOTTOM, LEFT, RIGHT, CENTER }

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val CM_TO_POINTS = 28.35f

    fun generate(
        context: Context,
        coverConfig: CoverPageConfig,
        generatedPages: List<GeneratedPage>,
        fileName: String
    ): File? {
        val pdfDocument = PdfDocument()
        val uncompressedPdfStream = ByteArrayOutputStream()

        try {
            val quality = coverConfig.quality

            val shouldDrawCover = coverConfig.clientNameStyle.content.isNotBlank() ||
                    coverConfig.rucStyle.content.isNotBlank() ||
                    coverConfig.subtitleStyle.content.isNotBlank() ||
                    coverConfig.mainImageUri != null

            if (shouldDrawCover) {
                drawCoverPage(pdfDocument, context, coverConfig, quality)
            }
            drawInnerPages(pdfDocument, context, generatedPages, coverConfig, if (shouldDrawCover) 2 else 1, quality)

            pdfDocument.writeTo(uncompressedPdfStream)
            pdfDocument.close()

            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            storageDir?.mkdirs()
            val pdfFile = File(storageDir, "$fileName.pdf")

            val pdDocument = PDDocument.load(uncompressedPdfStream.toByteArray())
            pdDocument.version = 1.5f
            pdDocument.save(pdfFile)
            pdDocument.close()

            return pdfFile
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error al generar PDF", e)
            pdfDocument.close()
            return null
        }
    }

    private fun drawCoverPage(pdfDocument: PdfDocument, context: Context, config: CoverPageConfig, quality: Int) {
        val pageWidth = if (config.pageOrientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
        val pageHeight = if (config.pageOrientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        config.pageBackgroundColor?.let { color ->
            canvas.drawColor(color)
        }

        drawCoverPageContent(canvas, context, config, quality, pageWidth, pageHeight)
        pdfDocument.finishPage(page)
    }

    private fun drawCoverPageContent(canvas: Canvas, context: Context, config: CoverPageConfig, quality: Int, pageWidth: Int, pageHeight: Int) {
        val marginTop = config.marginTop * CM_TO_POINTS
        val marginBottom = config.marginBottom * CM_TO_POINTS
        val marginLeft = config.marginLeft * CM_TO_POINTS
        val marginRight = config.marginRight * CM_TO_POINTS
        val contentArea = RectF(marginLeft, marginTop, (pageWidth - marginRight), (pageHeight - marginBottom))

        val allRows = mutableListOf<Map<String, Any>>()
        // ... (omitting text row setup for brevity, it remains the same)
        if (config.clientNameStyle.content.isNotBlank()) {
            allRows.add(mapOf("id" to "client", "weight" to config.clientWeight, "draw" to { rect: RectF -> drawRow(canvas, context, "Cliente: ${config.clientNameStyle.content.let { if (config.allCaps) it.uppercase() else it }}", config.clientNameStyle, rect) }))
        }
        if (config.rucStyle.content.isNotBlank()) {
            allRows.add(mapOf("id" to "ruc", "weight" to config.rucWeight, "draw" to { rect: RectF -> drawRow(canvas, context, "RUC: ${config.rucStyle.content.let { if (config.allCaps) it.uppercase() else it }}", config.rucStyle, rect) }))
        }
        if (config.subtitleStyle.content.isNotBlank()) {
            allRows.add(mapOf("id" to "address","weight" to 0f,"style" to config.subtitleStyle,"content" to "Dirección: ${config.subtitleStyle.content.let { if (config.allCaps) it.uppercase() else it }}","draw" to { rect: RectF -> drawRow(canvas, context, "Dirección: ${config.subtitleStyle.content.let { if (config.allCaps) it.uppercase() else it }}", config.subtitleStyle, rect) }))
        }

        if (config.mainImageUri != null) {
            allRows.add(mapOf(
                "id" to "photo",
                "weight" to config.photoWeight,
                "draw" to { rect: RectF ->
                    drawRowBackgroundAndBorders(canvas, config.photoStyle, rect)
                    if (config.mainImageUri != null) {
                        try {
                            val uriString = config.mainImageUri
                            val padding = config.photoStyle.padding
                            val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom)
                            val bitmap = decodeAndCompressBitmapFromUri(context, Uri.parse(uriString), paddedRect.width().toInt(), paddedRect.height().toInt(), quality)
                            bitmap?.let {
                                val borderSettings = config.imageBorderSettingsMap["cover"]
                                drawBitmapToCanvas(canvas, it, paddedRect, ImageAlignment.CENTER, borderSettings)
                                it.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            ))
        }

        // ... (omitting row calculation logic for brevity, it remains the same)
        var addressRowHeight = 0f
        val addressRowData = allRows.find { it["id"] == "address" }
        if (addressRowData != null) {
            val style = addressRowData["style"] as TextStyleConfig
            val content = addressRowData["content"] as String
            val textPaint = createTextPaint(context, style)
            val staticLayout = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, contentArea.width().toInt()).setAlignment(getAndroidAlignment(style.textAlign)).build()
            addressRowHeight = staticLayout.height.toFloat() + style.rowStyle.padding.top + style.rowStyle.padding.bottom
        }
        val weightedRows = allRows.filter { it["id"] != "address" }
        val totalWeight = weightedRows.sumOf { (it["weight"] as Float).toDouble() }.toFloat()
        var separations = 0
        for (i in 0 until allRows.size - 1) {
            val currentId = allRows[i]["id"] as String
            val nextId = allRows[i+1]["id"] as String
            if (currentId == "client" && nextId == "ruc") continue
            if (currentId == "address") continue
            separations++
        }
        val totalSeparationWeight = config.separationWeight * separations
        val finalTotalWeight = totalWeight + totalSeparationWeight
        val fixedSpace = if (addressRowData != null) addressRowHeight + 5f else 0f
        val availableHeight = contentArea.height() - fixedSpace
        val separationHeight = if (finalTotalWeight > 0) availableHeight * (config.separationWeight / finalTotalWeight) else 0f
        var currentY = contentArea.top
        allRows.forEachIndexed { index, rowData ->
            val id = rowData["id"] as String
            val drawFunc = rowData["draw"] as? (RectF) -> Unit
            val itemHeight = if (id == "address") addressRowHeight else {
                if (finalTotalWeight > 0) availableHeight * ((rowData["weight"] as Float) / finalTotalWeight) else 0f
            }
            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
            drawFunc?.invoke(rect)
            currentY += itemHeight
            if (index < allRows.size - 1) {
                val nextId = allRows[index + 1]["id"] as String
                if (id == "address") { currentY += 5f } else if (!(id == "client" && nextId == "ruc")) { currentY += separationHeight }
            }
        }
    }

    private fun drawPageOnCanvas(
        canvas: Canvas,
        context: Context,
        pageData: GeneratedPage,
        coverConfig: CoverPageConfig,
        quality: Int
    ) {
        val (cols, rows) = when (pageData.orientation) {
            PageOrientation.Vertical -> if (pageData.imageUris.size > 1) Pair(1, 2) else Pair(1, 1)
            PageOrientation.Horizontal -> if (pageData.imageUris.size > 1) Pair(2, 1) else Pair(1, 1)
        }

        val rects = getRectsForPage(canvas.width, canvas.height, 20f, cols, rows, 15f)
        val borderSettings = coverConfig.imageBorderSettingsMap[pageData.groupId]

        pageData.imageUris.forEachIndexed { index, uriString ->
            if (index < rects.size) {
                val rect = rects[index]
                try {
                    val bitmap = decodeAndCompressBitmapFromUri(context, Uri.parse(uriString), rect.width().toInt(), rect.height().toInt(), quality)
                    bitmap?.let {
                        val alignment = when {
                            cols == 1 && rows == 1 -> ImageAlignment.CENTER
                            cols == 2 -> if (index == 0) ImageAlignment.RIGHT else ImageAlignment.LEFT
                            rows == 2 -> if (index == 0) ImageAlignment.BOTTOM else ImageAlignment.TOP
                            else -> ImageAlignment.CENTER
                        }
                        drawBitmapToCanvas(canvas, it, rect, alignment, borderSettings)
                        it.recycle()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun drawInnerPages(
        pdfDocument: PdfDocument,
        context: Context,
        generatedPages: List<GeneratedPage>,
        coverConfig: CoverPageConfig,
        startPageNumber: Int,
        quality: Int
    ) {
        var pageNumber = startPageNumber
        generatedPages.forEach { pageData ->
            val pageWidth = if (pageData.orientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
            val pageHeight = if (pageData.orientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            coverConfig.pageBackgroundColor?.let { color ->
                canvas.drawColor(color)
            }

            drawPageOnCanvas(canvas, context, pageData, coverConfig, quality)

            pdfDocument.finishPage(page)
        }
    }

    // ... (omitting getAndroidAlignment, drawRow, drawRowBackgroundAndBorders, createTextPaint, drawTextInRect, getRectsForPage for brevity)
    private fun getAndroidAlignment(textAlign: TextAlign): Layout.Alignment = when (textAlign) { TextAlign.Center -> Layout.Alignment.ALIGN_CENTER; TextAlign.End -> Layout.Alignment.ALIGN_OPPOSITE; else -> Layout.Alignment.ALIGN_NORMAL }
    private fun drawRow(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) { drawRowBackgroundAndBorders(canvas, style.rowStyle, rect); drawTextInRect(canvas, context, text, style, rect) }
    private fun drawRowBackgroundAndBorders(canvas: Canvas, rowStyle: RowStyle, rect: RectF) { val backgroundPaint = Paint().apply { color = rowStyle.backgroundColor.toArgb(); style = Paint.Style.FILL }; canvas.drawRect(rect, backgroundPaint); val borderPaint = Paint().apply { color = rowStyle.border.color.toArgb(); style = Paint.Style.STROKE; strokeWidth = rowStyle.border.thickness }; val border = rowStyle.border; if (border.top) canvas.drawLine(rect.left, rect.top, rect.right, rect.top, borderPaint); if (border.bottom) canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, borderPaint); if (border.left) canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, borderPaint); if (border.right) canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, borderPaint) }
    private fun createTextPaint(context: Context, style: TextStyleConfig): TextPaint { return TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = style.fontColor.toArgb(); textSize = style.fontSize.toFloat(); val isBold = style.fontWeight == FontWeight.Bold; val isItalic = style.fontStyle == FontStyle.Italic; val typefaceStyle = when { isBold && isItalic -> Typeface.BOLD_ITALIC; isBold -> Typeface.BOLD; isItalic -> Typeface.ITALIC; else -> Typeface.NORMAL }; typeface = Typeface.create(Typeface.SANS_SERIF, typefaceStyle) } }
    private fun drawTextInRect(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) { if (text.isBlank()) return; val padding = style.rowStyle.padding; val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom); if (paddedRect.width() <= 0 || paddedRect.height() <= 0) return; val textPaint = createTextPaint(context, style); val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, paddedRect.width().toInt()).setAlignment(getAndroidAlignment(style.textAlign)).build(); val textY = paddedRect.top + (paddedRect.height() - staticLayout.height) / 2; canvas.save(); canvas.translate(paddedRect.left, textY); staticLayout.draw(canvas); canvas.restore() }
    private fun getRectsForPage(pageWidth: Int, pageHeight: Int, startY: Float, cols: Int, rows: Int, spacing: Float): List<RectF> { val rects = mutableListOf<RectF>(); val totalSpacingX = spacing * (cols + 1); val totalSpacingY = spacing * (rows + 1); val cellWidth = (pageWidth - totalSpacingX) / cols; val availableHeight = pageHeight - startY; val cellHeight = (availableHeight - totalSpacingY) / rows; for (row in 0 until rows) { for (col in 0 until cols) { val left = totalSpacingX / (cols + 1) + col * (cellWidth + spacing); val top = startY + totalSpacingY / (rows + 1) + row * (cellHeight + spacing); val right = left + cellWidth; val bottom = top + cellHeight; rects.add(RectF(left, top, right, bottom)) } }; return rects }


    private fun drawBitmapToCanvas(canvas: Canvas, bitmap: Bitmap, cellRect: RectF, alignment: ImageAlignment, borderSettings: ImageBorderSettings?) {
        val finalRect = getFinalBitmapRect(bitmap, cellRect, alignment)

        canvas.save()
        try {
            if (borderSettings != null && borderSettings.style != ImageBorderStyle.NONE) {
                val path = Path()
                when (borderSettings.style) {
                    ImageBorderStyle.CURVED -> {
                        path.addRoundRect(finalRect, borderSettings.size, borderSettings.size, Path.Direction.CW)
                    }
                    ImageBorderStyle.CHAMFERED -> {
                        val size = borderSettings.size
                        path.moveTo(finalRect.left + size, finalRect.top)
                        path.lineTo(finalRect.right - size, finalRect.top)
                        path.lineTo(finalRect.right, finalRect.top + size)
                        path.lineTo(finalRect.right, finalRect.bottom - size)
                        path.lineTo(finalRect.right - size, finalRect.bottom)
                        path.lineTo(finalRect.left + size, finalRect.bottom)
                        path.lineTo(finalRect.left, finalRect.bottom - size)
                        path.lineTo(finalRect.left, finalRect.top + size)
                        path.close()
                    }
                    ImageBorderStyle.NONE -> {}
                }
                canvas.clipPath(path)
            }
            canvas.drawBitmap(bitmap, null, finalRect, null)
        } finally {
            canvas.restore()
        }
    }

    private fun getFinalBitmapRect(bitmap: Bitmap, cellRect: RectF, alignment: ImageAlignment): RectF {
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()
        val cellWidth = cellRect.width()
        val cellHeight = cellRect.height()

        val scale: Float
        val newWidth: Float
        val newHeight: Float

        if (bitmapWidth / bitmapHeight > cellWidth / cellHeight) {
            scale = cellWidth / bitmapWidth
            newWidth = cellWidth
            newHeight = bitmapHeight * scale
        } else {
            scale = cellHeight / bitmapHeight
            newHeight = cellHeight
            newWidth = bitmapWidth * scale
        }

        var x = cellRect.left
        var y = cellRect.top

        when (alignment) {
            ImageAlignment.CENTER -> { x += (cellWidth - newWidth) / 2; y += (cellHeight - newHeight) / 2 }
            ImageAlignment.LEFT -> { x = cellRect.left; y += (cellHeight - newHeight) / 2 }
            ImageAlignment.RIGHT -> { x = cellRect.right - newWidth; y += (cellHeight - newHeight) / 2 }
            ImageAlignment.TOP -> { x += (cellWidth - newWidth) / 2; y = cellRect.top }
            ImageAlignment.BOTTOM -> { x += (cellWidth - newWidth) / 2; y = cellRect.bottom - newHeight }
        }
        return RectF(x, y, x + newWidth, y + newHeight)
    }

    private fun decodeAndCompressBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int, quality: Int): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null
            val outputStream = ByteArrayOutputStream()
            sampledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            sampledBitmap.recycle()
            val finalInputStream = ByteArrayInputStream(outputStream.toByteArray())
            BitmapFactory.decodeStream(finalInputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
