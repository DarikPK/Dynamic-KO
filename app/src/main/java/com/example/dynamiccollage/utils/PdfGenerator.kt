package com.example.dynamiccollage.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.res.ResourcesCompat
import android.util.Log
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import com.example.dynamiccollage.data.model.RowStyle
import com.example.dynamiccollage.data.model.TextStyleConfig
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val CM_TO_POINTS = 28.35f

    fun generate(
        context: Context,
        coverConfig: CoverPageConfig,
        generatedPages: List<com.example.dynamiccollage.data.model.GeneratedPage>,
        fileName: String,
        imageQuality: Int
    ): File? {
        val pdfDocument = PdfDocument()
        val uncompressedPdfStream = ByteArrayOutputStream()

        try {
            val shouldDrawCover = coverConfig.clientNameStyle.content.isNotBlank() ||
                    coverConfig.rucStyle.content.isNotBlank() ||
                    coverConfig.subtitleStyle.content.isNotBlank() ||
                    coverConfig.mainImageUri != null

            if (shouldDrawCover) {
                drawCoverPage(pdfDocument, context, coverConfig, imageQuality)
            }
            drawInnerPages(pdfDocument, context, generatedPages, if (shouldDrawCover) 2 else 1, imageQuality)

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
        if (config.clientNameStyle.content.isNotBlank()) {
            allRows.add(mapOf(
                "id" to "client",
                "weight" to config.clientWeight,
                "draw" to { rect: RectF ->
                    var content = if (config.allCaps) config.clientNameStyle.content.uppercase() else config.clientNameStyle.content
                    if (config.showClientPrefix) {
                        content = "Cliente: $content"
                    }
                    drawRow(canvas, context, content, config.clientNameStyle, rect)
                }
            ))
        }
        if (config.rucStyle.content.isNotBlank()) {
            allRows.add(mapOf(
                "id" to "ruc",
                "weight" to config.rucWeight,
                "draw" to { rect: RectF ->
                    val documentLabel = when (config.documentType) {
                        com.example.dynamiccollage.data.model.DocumentType.DNI -> "DNI: "
                        com.example.dynamiccollage.data.model.DocumentType.RUC -> "RUC: "
                        com.example.dynamiccollage.data.model.DocumentType.NONE -> ""
                    }
                    val content = documentLabel + if (config.allCaps) config.rucStyle.content.uppercase() else config.rucStyle.content
                    drawRow(canvas, context, content, config.rucStyle, rect)
                }
            ))
        }
        if (config.subtitleStyle.content.isNotBlank()) {
            allRows.add(mapOf(
                "id" to "address",
                "weight" to 0f,
                "style" to config.subtitleStyle,
                "content" to (if (config.showAddressPrefix) "Dirección: " else "") + if (config.allCaps) config.subtitleStyle.content.uppercase() else config.subtitleStyle.content,
                "draw" to { rect: RectF ->
                    var content = if (config.allCaps) config.subtitleStyle.content.uppercase() else config.subtitleStyle.content
                    if (config.showAddressPrefix) content = "Dirección: $content"
                    drawRow(canvas, context, content, config.subtitleStyle, rect)
                }
            ))
        }
        if (config.mainImageUri != null) {
            allRows.add(mapOf(
                "id" to "photo",
                "weight" to config.photoWeight,
                "draw" to { rect: RectF ->
                    drawRowBackgroundAndBorders(canvas, config.photoStyle, rect)
                    config.mainImageUri?.let { uriString ->
                        try {
                            val padding = config.photoStyle.padding
                            val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom)
                            val bitmap = decodeAndCompressBitmapFromUri(context, Uri.parse(uriString), paddedRect.width().toInt(), paddedRect.height().toInt(), quality)
                            bitmap?.let {
                                drawBitmapToCanvas(canvas, it, paddedRect)
                                it.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            ))
        }

        if (allRows.isEmpty()) {
            return
        }

        var addressRowHeight = 0f
        val addressRowData = allRows.find { it["id"] == "address" }
        if (addressRowData != null) {
            val style = addressRowData["style"] as TextStyleConfig
            val content = addressRowData["content"] as String
            val textPaint = createTextPaint(context, style)
            val staticLayout = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, contentArea.width().toInt())
                .setAlignment(getAndroidAlignment(style.textAlign))
                .build()
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
        if (finalTotalWeight <= 0f && availableHeight <= 0f) {
            return
        }
        val separationHeight = if (finalTotalWeight > 0) availableHeight * (config.separationWeight / finalTotalWeight) else 0f
        var currentY = contentArea.top
        allRows.forEachIndexed { index, rowData ->
            val id = rowData["id"] as String
            val drawFunc = rowData["draw"] as (RectF) -> Unit
            val itemHeight = if (id == "address") addressRowHeight else {
                if (finalTotalWeight > 0) availableHeight * ((rowData["weight"] as Float) / finalTotalWeight) else 0f
            }
            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
            drawFunc(rect)
            currentY += itemHeight
            if (index < allRows.size - 1) {
                val nextId = allRows[index + 1]["id"] as String
                if (id == "address") {
                    currentY += 5f
                } else if (!(id == "client" && nextId == "ruc")) {
                    currentY += separationHeight
                }
            }
        }
    }

    fun generatePreviewBitmaps(
        context: Context,
        generatedPages: List<com.example.dynamiccollage.data.model.GeneratedPage>
    ): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val quality = 85 // Calidad de imagen razonable para previsualizaciones
        try {
            generatedPages.forEach { pageData ->
                val pageWidth = if (pageData.orientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
                val pageHeight = if (pageData.orientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH
                val bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawPageOnCanvas(canvas, context, pageData, quality)
                bitmaps.add(bitmap)
            }
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error al generar previsualizaciones de bitmap", e)
        }
        return bitmaps
    }

    private fun drawPageOnCanvas(
        canvas: Canvas,
        context: Context,
        pageData: com.example.dynamiccollage.data.model.GeneratedPage,
        quality: Int
    ) {
        val (cols, rows) = when (pageData.orientation) {
            PageOrientation.Vertical -> if (pageData.imageUris.size > 1) Pair(1, 2) else Pair(1, 1)
            PageOrientation.Horizontal -> if (pageData.imageUris.size > 1) Pair(2, 1) else Pair(1, 1)
        }

        val rects = getRectsForPage(canvas.width, canvas.height, 20f, cols, rows, 15f)

        pageData.imageUris.forEachIndexed { index, uriString ->
            if (index < rects.size) {
                val rect = rects[index]
                try {
                    val bitmap = decodeAndCompressBitmapFromUri(context, Uri.parse(uriString), rect.width().toInt(), rect.height().toInt(), quality)
                    bitmap?.let {
                        drawBitmapToCanvas(canvas, it, rect)
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
        generatedPages: List<com.example.dynamiccollage.data.model.GeneratedPage>,
        startPageNumber: Int,
        quality: Int
    ) {
        var pageNumber = startPageNumber
        generatedPages.forEach { pageData ->
            val pageWidth = if (pageData.orientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
            val pageHeight = if (pageData.orientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
            val page = pdfDocument.startPage(pageInfo)

            drawPageOnCanvas(page.canvas, context, pageData, quality)

            pdfDocument.finishPage(page)
        }
    }

    private fun getAndroidAlignment(textAlign: TextAlign): Layout.Alignment {
        return when (textAlign) {
            TextAlign.Center -> Layout.Alignment.ALIGN_CENTER
            TextAlign.End -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_NORMAL
        }
    }

    private fun drawRow(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) {
        drawRowBackgroundAndBorders(canvas, style.rowStyle, rect)
        drawTextInRect(canvas, context, text, style, rect)
    }

    private fun drawRowBackgroundAndBorders(canvas: Canvas, rowStyle: RowStyle, rect: RectF) {
        val backgroundPaint = Paint().apply {
            color = rowStyle.backgroundColor.toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawRect(rect, backgroundPaint)
        val borderPaint = Paint().apply {
            color = rowStyle.border.color.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = rowStyle.border.thickness
        }
        val border = rowStyle.border
        if (border.top) canvas.drawLine(rect.left, rect.top, rect.right, rect.top, borderPaint)
        if (border.bottom) canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, borderPaint)
        if (border.left) canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, borderPaint)
        if (border.right) canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, borderPaint)
    }

    private fun createTextPaint(context: Context, style: TextStyleConfig): TextPaint {
        return TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.fontColor.toArgb()
            textSize = style.fontSize.toFloat()
            val isBold = style.fontWeight == FontWeight.Bold
            val isItalic = style.fontStyle == FontStyle.Italic
            val typefaceStyle = when {
                isBold && isItalic -> Typeface.BOLD_ITALIC
                isBold -> Typeface.BOLD
                isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            val fontFamilyRes = R.font.calibri
            typeface = try {
                val baseTypeface = ResourcesCompat.getFont(context, fontFamilyRes)
                Typeface.create(baseTypeface, typefaceStyle)
            } catch (e: Exception) {
                e.printStackTrace()
                Typeface.create(Typeface.DEFAULT, typefaceStyle)
            }
        }
    }

    private fun drawTextInRect(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) {
        if (text.isBlank()) return
        val padding = style.rowStyle.padding
        val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom)
        if (paddedRect.width() <= 0 || paddedRect.height() <= 0) return
        val textPaint = createTextPaint(context, style)
        val staticLayout = StaticLayout.Builder.obtain(
            text, 0, text.length, textPaint, paddedRect.width().toInt()
        ).setAlignment(getAndroidAlignment(style.textAlign)).build()
        val textY = paddedRect.top + (paddedRect.height() - staticLayout.height) / 2
        canvas.save()
        canvas.translate(paddedRect.left, textY)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun getRectsForPage(pageWidth: Int, pageHeight: Int, startY: Float, cols: Int, rows: Int, spacing: Float): List<RectF> {
        val rects = mutableListOf<RectF>()
        val totalSpacingX = spacing * (cols + 1)
        val totalSpacingY = spacing * (rows + 1)
        val cellWidth = (pageWidth - totalSpacingX) / cols
        val availableHeight = pageHeight - startY
        val cellHeight = (availableHeight - totalSpacingY) / rows
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val left = totalSpacingX / (cols + 1) + col * (cellWidth + spacing)
                val top = startY + totalSpacingY / (rows + 1) + row * (cellHeight + spacing)
                val right = left + cellWidth
                val bottom = top + cellHeight
                rects.add(RectF(left, top, right, bottom))
            }
        }
        return rects
    }

    private fun drawBitmapToCanvas(canvas: Canvas, bitmap: Bitmap, destRect: RectF) {
        val srcRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val matrix = Matrix()
        matrix.setRectToRect(srcRect, destRect, Matrix.ScaleToFit.CENTER)
        canvas.drawBitmap(bitmap, matrix, null)
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
