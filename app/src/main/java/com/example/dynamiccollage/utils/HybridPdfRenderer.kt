package com.example.dynamiccollage.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.dynamiccollage.data.model.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun generateHybridPdf(
    context: Context,
    coverConfig: CoverPageConfig,
    generatedPages: List<GeneratedPage>,
    fileName: String,
    imageEffectSettings: Map<String, ImageEffectSettings>
): File? {
    Log.d("HybridPdf", "Modo híbrido activado: generando PDF con PdfDocument + PDFBox")

    val pdfDocument = PdfDocument()
    val tempFile = File.createTempFile("uncompressed_pdf", ".pdf", context.cacheDir)

    try {
        val quality = coverConfig.quality

        val shouldDrawCover = coverConfig.clientNameStyle.content.isNotBlank() ||
                coverConfig.rucStyle.content.isNotBlank() ||
                coverConfig.subtitleStyle.content.isNotBlank() ||
                coverConfig.mainImageUri != null

        if (shouldDrawCover) {
             drawCoverPage(pdfDocument, context, coverConfig, quality, imageEffectSettings, renderImages = false)
        }
         drawInnerPages(pdfDocument, context, generatedPages, coverConfig, if (shouldDrawCover) 2 else 1, quality, imageEffectSettings, renderImages = false)

        val outputStream = FileOutputStream(tempFile)
        pdfDocument.writeTo(outputStream)

        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        Log.d("HybridPdf", "PdfDocument cerrado correctamente. Tamaño final: ${tempFile.length()} bytes")

        if (tempFile.length() == 0L) {
            throw IOException("El archivo PDF temporal está vacío antes de cargar en PDFBox")
        }

        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        storageDir?.mkdirs()
        val pdfFile = File(storageDir, "$fileName.pdf")

        val pdDocument = PDDocument.load(tempFile)

        // Superponer imágenes con PDFBox
        if (shouldDrawCover) {
            drawImagesWithPdfBox(context, pdDocument, coverConfig, imageEffectSettings, 0)
        }
        generatedPages.forEachIndexed { index, pageData ->
            drawImagesWithPdfBox(context, pdDocument, pageData, coverConfig, imageEffectSettings, if (shouldDrawCover) index + 1 else index)
        }

        pdDocument.version = 1.5f
        pdDocument.save(pdfFile)
        pdDocument.close()

        Log.d("HybridPdf", "PDFBox imágenes renderizadas con éxito.")
        return pdfFile
    } catch (e: Exception) {
        Log.e("HybridPdf", "Error al generar PDF Híbrido", e)
        pdfDocument.close()
        return null
    } finally {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}

// --- Funciones de Dibujo Transferidas de PdfGenerator ---

internal fun applyAllEffects(input: Bitmap, settings: ImageEffectSettings): Bitmap {
    var processedBitmap = input
    settings.cropRect?.let { normalizedRect ->
        if (normalizedRect.width > 0 && normalizedRect.height > 0) {
            val left = (normalizedRect.left * processedBitmap.width).toInt()
            val top = (normalizedRect.top * processedBitmap.height).toInt()
            val width = (normalizedRect.width * processedBitmap.width).toInt()
            val height = (normalizedRect.height * processedBitmap.height).toInt()
            if (width > 0 && height > 0 && (left + width) <= processedBitmap.width && (top + height) <= processedBitmap.height) {
                processedBitmap = Bitmap.createBitmap(processedBitmap, left, top, width, height)
            }
        }
    }
    if (settings.rotationDegrees != 0f) {
        val matrix = Matrix().apply { postRotate(settings.rotationDegrees) }
        processedBitmap = Bitmap.createBitmap(processedBitmap, 0, 0, processedBitmap.width, processedBitmap.height, matrix, true)
    }
    val contrast = 1.0f + settings.contrast / 100.0f
    val saturation = 1.0f + settings.saturation / 100.0f
    processedBitmap = ImageEffects.applyEffects(processedBitmap, settings.brightness, contrast, saturation)
    val sharpness = settings.sharpness / 100.0f
    if (sharpness > 0) {
        processedBitmap = ImageEffects.applySharpen(processedBitmap, sharpness)
    } else if (sharpness < 0) {
        processedBitmap = ImageEffects.applyBlur(processedBitmap, -sharpness)
    }
    return processedBitmap
}

internal enum class ImageAlignment { TOP, BOTTOM, LEFT, RIGHT, CENTER }

internal const val A4_WIDTH = 595
internal const val A4_HEIGHT = 842
internal const val CM_TO_POINTS = 28.35f

internal fun drawCoverPage(pdfDocument: PdfDocument, context: Context, config: CoverPageConfig, quality: Int, imageEffectSettings: Map<String, ImageEffectSettings>, renderImages: Boolean = true) {
    val pageWidth = if (config.pageOrientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
    val pageHeight = if (config.pageOrientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    config.pageBackgroundColor?.let { color -> canvas.drawColor(color) }
    drawCoverPageContent(canvas, context, config, quality, pageWidth, pageHeight, imageEffectSettings, renderImages)
    pdfDocument.finishPage(page)
}

internal fun calculateCoverPageLayout(context: Context, config: CoverPageConfig, contentArea: RectF): Map<String, RectF> {
    val layoutRects = mutableMapOf<String, RectF>()
    val allRows = mutableListOf<Map<String, Any>>()

    if (config.clientNameStyle.content.isNotBlank()) {
        allRows.add(mapOf("id" to "client", "weight" to config.clientWeight, "style" to config.clientNameStyle, "content" to config.clientNameStyle.content))
    }
    if (config.rucStyle.content.isNotBlank()) {
        allRows.add(mapOf("id" to "ruc", "weight" to config.rucWeight, "style" to config.rucStyle, "content" to config.rucStyle.content))
    }
    if (config.subtitleStyle.content.isNotBlank()) {
        allRows.add(mapOf("id" to "address", "weight" to 0f, "style" to config.subtitleStyle, "content" to "Dirección: ${config.subtitleStyle.content.let { if (config.allCaps) it.uppercase() else it }}"))
    }
    if (config.mainImageUri != null) {
        allRows.add(mapOf("id" to "photo", "weight" to config.photoWeight))
    }

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
        val itemHeight = if (id == "address") addressRowHeight else {
            if (finalTotalWeight > 0) availableHeight * ((rowData["weight"] as Float) / finalTotalWeight) else 0f
        }
        val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
        layoutRects[id] = rect
        currentY += itemHeight
        if (index < allRows.size - 1) {
            val nextId = allRows[index + 1]["id"] as String
            if (id == "address") { currentY += 5f } else if (!(id == "client" && nextId == "ruc")) { currentY += separationHeight }
        }
    }
    return layoutRects
}

internal fun drawCoverPageContent(canvas: Canvas, context: Context, config: CoverPageConfig, quality: Int, pageWidth: Int, pageHeight: Int, imageEffectSettings: Map<String, ImageEffectSettings>, renderImages: Boolean = true) {
    val marginTop = config.marginTop * CM_TO_POINTS
    val marginBottom = config.marginBottom * CM_TO_POINTS
    val marginLeft = config.marginLeft * CM_TO_POINTS
    val marginRight = config.marginRight * CM_TO_POINTS
    val contentArea = RectF(marginLeft, marginTop, (pageWidth - marginRight), (pageHeight - marginBottom))

    val layoutRects = calculateCoverPageLayout(context, config, contentArea)

    layoutRects["client"]?.let { rect ->
        drawRow(canvas, context, "Cliente: ${config.clientNameStyle.content.let { if (config.allCaps) it.uppercase() else it }}", config.clientNameStyle, rect)
    }
    layoutRects["ruc"]?.let { rect ->
        drawRow(canvas, context, "RUC: ${config.rucStyle.content.let { if (config.allCaps) it.uppercase() else it }}", config.rucStyle, rect)
    }
    layoutRects["address"]?.let { rect ->
        drawRow(canvas, context, "Dirección: ${config.subtitleStyle.content.let { if (config.allCaps) it.uppercase() else it }}", config.subtitleStyle, rect)
    }
    layoutRects["photo"]?.let { rect ->
        drawRowBackgroundAndBorders(canvas, config.photoStyle, rect)
        if (renderImages && config.mainImageUri != null) {
            try {
                val uriString = config.mainImageUri
                val padding = config.photoStyle.padding
                val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom)
                var bitmap = decodeSampledBitmapFromUri(
                    context,
                    Uri.parse(uriString),
                    paddedRect.width().toInt(),
                    paddedRect.height().toInt(),
                    quality,
                    forceFullRes = config.forceFullResCover
                )
                bitmap?.let {
                    val settings = imageEffectSettings[uriString]
                    if (settings != null) {
                        bitmap = applyAllEffects(it, settings)
                    }
                    val borderSettings = config.imageBorderSettingsMap["cover"]
                    drawBitmapToCanvas(canvas, bitmap!!, paddedRect, ImageAlignment.CENTER, borderSettings)
                    it.recycle()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}

internal fun drawPageOnCanvas(canvas: Canvas, context: Context, pageData: GeneratedPage, coverConfig: CoverPageConfig, quality: Int, imageEffectSettings: Map<String, ImageEffectSettings>, renderImages: Boolean = true) {
    var startY = 20f
    if (pageData.isFirstPageOfGroup && pageData.optionalTextStyle != null && pageData.optionalTextStyle.isVisible) {
        val textStyle = pageData.optionalTextStyle
        val textPaint = createTextPaint(context, textStyle)
        val text = if (textStyle.allCaps) textStyle.content.uppercase() else textStyle.content
        val textWidth = canvas.width - 40f
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth.toInt()).setAlignment(getAndroidAlignment(textStyle.textAlign)).build()
        val rowHeight = staticLayout.height + textStyle.rowStyle.padding.top + textStyle.rowStyle.padding.bottom
        val rowRect = RectF(20f, startY, canvas.width - 20f, startY + rowHeight)
        drawRow(canvas, context, text, textStyle, rowRect)
        startY += rowHeight + 15f
    }
    val (cols, rows) = when (pageData.orientation) {
        PageOrientation.Vertical -> if (pageData.imageUris.size > 1) Pair(1, 2) else Pair(1, 1)
        PageOrientation.Horizontal -> if (pageData.imageUris.size > 1) Pair(2, 1) else Pair(1, 1)
    }
    val rects = getRectsForPage(canvas.width, canvas.height, startY, cols, rows, 15f)
    val borderSettings = coverConfig.imageBorderSettingsMap[pageData.groupId]

    pageData.imageUris.forEachIndexed { index, uriString ->
        if (index < rects.size) {
            if (renderImages) {
                val rect = rects[index]
                try {
                    var bitmap = decodeSampledBitmapFromUri(
                        context,
                        Uri.parse(uriString),
                        rect.width().toInt(),
                        rect.height().toInt(),
                        quality,
                        forceFullRes = false
                    )
                    bitmap?.let {
                        val settings = imageEffectSettings[uriString]
                        if (settings != null) {
                            bitmap = applyAllEffects(it, settings)
                        }
                        val alignment = when {
                            cols == 1 && rows == 1 -> ImageAlignment.CENTER
                            cols == 2 -> if (index == 0) ImageAlignment.RIGHT else ImageAlignment.LEFT
                            rows == 2 -> if (index == 0) ImageAlignment.BOTTOM else ImageAlignment.TOP
                            else -> ImageAlignment.CENTER
                        }
                        drawBitmapToCanvas(canvas, bitmap!!, rect, alignment, borderSettings)
                        it.recycle()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

internal fun drawInnerPages(pdfDocument: PdfDocument, context: Context, generatedPages: List<GeneratedPage>, coverConfig: CoverPageConfig, startPageNumber: Int, quality: Int, imageEffectSettings: Map<String, ImageEffectSettings>, renderImages: Boolean = true) {
    var pageNumber = startPageNumber
    generatedPages.forEach { pageData ->
        val pageWidth = if (pageData.orientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
        val pageHeight = if (pageData.orientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        coverConfig.pageBackgroundColor?.let { color -> canvas.drawColor(color) }
        drawPageOnCanvas(canvas, context, pageData, coverConfig, quality, imageEffectSettings, renderImages)
        pdfDocument.finishPage(page)
    }
}

internal fun getAndroidAlignment(textAlign: TextAlign): Layout.Alignment = when (textAlign) { TextAlign.Center -> Layout.Alignment.ALIGN_CENTER; TextAlign.End -> Layout.Alignment.ALIGN_OPPOSITE; else -> Layout.Alignment.ALIGN_NORMAL }
internal fun drawRow(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) { drawRowBackgroundAndBorders(canvas, style.rowStyle, rect); drawTextInRect(canvas, context, text, style, rect) }
internal fun drawRowBackgroundAndBorders(canvas: Canvas, rowStyle: RowStyle, rect: RectF) { val backgroundPaint = Paint().apply { color = rowStyle.backgroundColor.toArgb(); style = Paint.Style.FILL }; canvas.drawRect(rect, backgroundPaint); val borderPaint = Paint().apply { color = rowStyle.border.color.toArgb(); style = Paint.Style.STROKE; strokeWidth = rowStyle.border.thickness }; val border = rowStyle.border; if (border.top) canvas.drawLine(rect.left, rect.top, rect.right, rect.top, borderPaint); if (border.bottom) canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, borderPaint); if (border.left) canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, borderPaint); if (border.right) canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, borderPaint) }
internal fun createTextPaint(context: Context, style: TextStyleConfig): TextPaint { return TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = style.fontColor.toArgb(); textSize = style.fontSize.toFloat(); val isBold = style.fontWeight == FontWeight.Bold; val isItalic = style.fontStyle == FontStyle.Italic; val typefaceStyle = when { isBold && isItalic -> Typeface.BOLD_ITALIC; isBold -> Typeface.BOLD; isItalic -> Typeface.ITALIC; else -> Typeface.NORMAL }; typeface = Typeface.create(Typeface.SANS_SERIF, typefaceStyle) } }
internal fun drawTextInRect(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) { if (text.isBlank()) return; val padding = style.rowStyle.padding; val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom); if (paddedRect.width() <= 0 || paddedRect.height() <= 0) return; val textPaint = createTextPaint(context, style); val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, paddedRect.width().toInt()).setAlignment(getAndroidAlignment(style.textAlign)).build(); val textY = paddedRect.top + (paddedRect.height() - staticLayout.height) / 2; canvas.save(); canvas.translate(paddedRect.left, textY); staticLayout.draw(canvas); canvas.restore() }
internal fun getRectsForPage(pageWidth: Int, pageHeight: Int, startY: Float, cols: Int, rows: Int, spacing: Float): List<RectF> { val rects = mutableListOf<RectF>(); val totalSpacingX = spacing * (cols + 1); val totalSpacingY = spacing * (rows + 1); val cellWidth = (pageWidth - totalSpacingX) / cols; val availableHeight = pageHeight - startY; val cellHeight = (availableHeight - totalSpacingY) / rows; for (row in 0 until rows) { for (col in 0 until cols) { val left = totalSpacingX / (cols + 1) + col * (cellWidth + spacing); val top = startY + totalSpacingY / (rows + 1) + row * (cellHeight + spacing); val right = left + cellWidth; val bottom = top + cellHeight; rects.add(RectF(left, top, right, bottom)) } }; return rects }

internal fun drawBitmapToCanvas(canvas: Canvas, bitmap: Bitmap, cellRect: RectF, alignment: ImageAlignment, borderSettings: ImageBorderSettings?) {
    val finalRect = getFinalBitmapRect(bitmap, cellRect, alignment)
    canvas.save()
    try {
        if (borderSettings != null && borderSettings.style != ImageBorderStyle.NONE) {
            val path = Path()
            when (borderSettings.style) {
                ImageBorderStyle.CURVED -> path.addRoundRect(finalRect, borderSettings.size, borderSettings.size, Path.Direction.CW)
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

internal fun getFinalBitmapRect(bitmap: Bitmap, cellRect: RectF, alignment: ImageAlignment): RectF {
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
        ImageAlignment.BOTTOM -> { x += (cellWidth - newHeight) / 2; y = cellRect.bottom - newHeight }
    }
    return RectF(x, y, x + newWidth, y + newHeight)
}

internal fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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

internal fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int, quality: Int, forceFullRes: Boolean = false): Bitmap? {
    return try {
        if (forceFullRes) {
            Log.d("PdfGenerator", "Cargando imagen de portada en resolución completa.")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                return BitmapFactory.decodeStream(inputStream)
            }
        }
        var inputStream = context.contentResolver.openInputStream(uri) ?: return null
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

private fun applyPdfBoxClippingPath(
    contentStream: PDPageContentStream,
    borderSettings: ImageBorderSettings,
    finalRect: RectF,
    pageHeight: Float
) {
    val size = borderSettings.size
    val left = finalRect.left
    val right = finalRect.right
    val top = pageHeight - finalRect.top
    val bottom = pageHeight - finalRect.bottom

    contentStream.saveGraphicsState()

    when (borderSettings.style) {
        ImageBorderStyle.CURVED -> {
            val k = 0.552284749831f
            contentStream.moveTo(left + size, top)
            contentStream.lineTo(right - size, top)
            contentStream.curveTo(right - size + size * k, top, right, top - size + size * k, right, top - size)
            contentStream.lineTo(right, bottom + size)
            contentStream.curveTo(right, bottom + size - size * k, right - size + size * k, bottom, right - size, bottom)
            contentStream.lineTo(left + size, bottom)
            contentStream.curveTo(left + size - size * k, bottom, left, bottom + size - size * k, left, bottom + size)
            contentStream.lineTo(left, top - size)
            contentStream.curveTo(left, top - size + size * k, left + size - size * k, top, left + size, top)
            contentStream.closePath()
        }
        ImageBorderStyle.CHAMFERED -> {
            contentStream.moveTo(left + size, top)
            contentStream.lineTo(right - size, top)
            contentStream.lineTo(right, top - size)
            contentStream.lineTo(right, bottom + size)
            contentStream.lineTo(right - size, bottom)
            contentStream.lineTo(left + size, bottom)
            contentStream.lineTo(left, bottom + size)
            contentStream.lineTo(left, top - size)
            contentStream.closePath()
        }
        ImageBorderStyle.NONE -> {}
    }
    contentStream.clip()
}


private fun drawImagesWithPdfBox(context: Context, pdDocument: PDDocument, config: CoverPageConfig, imageEffectSettings: Map<String, ImageEffectSettings>, pageIndex: Int) {
    if (config.mainImageUri == null) return

    val page = pdDocument.getPage(pageIndex)
    val pageHeight = page.mediaBox.height
    val contentStream: PDPageContentStream
    try {
        contentStream = PDPageContentStream(pdDocument, page, PDPageContentStream.AppendMode.APPEND, true, true)
    } catch (e: IOException) {
        Log.e("HybridPdf", "Error creating content stream for cover page.", e)
        return
    }

    try {
        val pageWidth = page.mediaBox.width
        val marginTop = config.marginTop * CM_TO_POINTS
        val marginBottom = config.marginBottom * CM_TO_POINTS
        val marginLeft = config.marginLeft * CM_TO_POINTS
        val marginRight = config.marginRight * CM_TO_POINTS
        val contentArea = RectF(marginLeft, marginTop, pageWidth - marginRight, pageHeight - marginBottom)

        val layoutRects = calculateCoverPageLayout(context, config, contentArea)
        val photoRect = layoutRects["photo"]

        if (photoRect != null) {
            val padding = config.photoStyle.padding
            val paddedRect = RectF(
                photoRect.left + padding.left,
                photoRect.top + padding.top,
                photoRect.right - padding.right,
                photoRect.bottom - padding.bottom
            )

            val uri = Uri.parse(config.mainImageUri)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File.createTempFile("hybrid_img", ".jpg", context.cacheDir)
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                var bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                val settings = imageEffectSettings[config.mainImageUri]
                if (settings != null) {
                    bitmap = applyAllEffects(bitmap, settings)
                }

                val finalRect = getFinalBitmapRect(bitmap, paddedRect, ImageAlignment.CENTER)

                val borderSettings = config.imageBorderSettingsMap["cover"]
                val clippingApplied = borderSettings != null && borderSettings.style != ImageBorderStyle.NONE

                if (clippingApplied) {
                    applyPdfBoxClippingPath(contentStream, borderSettings!!, finalRect, pageHeight)
                }

                val imageXObject: PDImageXObject
                if (config.hybridCoverImageQuality < 100) {
                    val quality = config.hybridCoverImageQuality
                    val scale = quality / 100f
                    val newWidth = (bitmap.width * scale).toInt()
                    val newHeight = (bitmap.height * scale).toInt()

                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                    val resizedTempFile = File.createTempFile("resized_hybrid", ".jpg", context.cacheDir)
                    FileOutputStream(resizedTempFile).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                    imageXObject = PDImageXObject.createFromFile(resizedTempFile.absolutePath, pdDocument)
                    resizedTempFile.delete()
                    scaledBitmap.recycle()
                } else {
                    imageXObject = PDImageXObject.createFromFile(tempFile.absolutePath, pdDocument)
                }

                val pdfBoxY = pageHeight - finalRect.bottom
                contentStream.drawImage(imageXObject, finalRect.left, pdfBoxY, finalRect.width(), finalRect.height())

                if (clippingApplied) {
                    contentStream.restoreGraphicsState()
                }

                bitmap.recycle()
                tempFile.delete()
            }
        }
    } catch (e: Exception) {
        Log.e("HybridPdf", "Error dibujando imagen de portada con PDFBox", e)
    } finally {
        try {
            contentStream.close()
        } catch (e: IOException) {
            Log.e("HybridPdf", "Error closing content stream for cover page.", e)
        }
    }
}

private fun drawImagesWithPdfBox(
    context: Context,
    pdDocument: PDDocument,
    pageData: GeneratedPage,
    coverConfig: CoverPageConfig,
    imageEffectSettings: Map<String, ImageEffectSettings>,
    pageIndex: Int
) {
    val page = pdDocument.getPage(pageIndex)
    val pageHeight = page.mediaBox.height
    val contentStream: PDPageContentStream
    try {
        contentStream = PDPageContentStream(pdDocument, page, PDPageContentStream.AppendMode.APPEND, true, true)
    } catch (e: IOException) {
        Log.e("HybridPdf", "Error creating content stream for inner page.", e)
        return
    }

    try {
        val pageWidth = page.mediaBox.width
        var startY = 20f
        if (pageData.isFirstPageOfGroup && pageData.optionalTextStyle != null && pageData.optionalTextStyle.isVisible) {
            val textStyle = pageData.optionalTextStyle
            val textPaint = createTextPaint(context, textStyle)
            val text = if (textStyle.allCaps) textStyle.content.uppercase() else textStyle.content
            val textWidth = pageWidth - 40f
            val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth.toInt()).setAlignment(getAndroidAlignment(textStyle.textAlign)).build()
            startY += staticLayout.height + textStyle.rowStyle.padding.top + textStyle.rowStyle.padding.bottom + 15f
        }

        val (cols, rows) = when (pageData.orientation) {
            PageOrientation.Vertical -> if (pageData.imageUris.size > 1) Pair(1, 2) else Pair(1, 1)
            PageOrientation.Horizontal -> if (pageData.imageUris.size > 1) Pair(2, 1) else Pair(1, 1)
        }
        val rects = getRectsForPage(pageWidth.toInt(), pageHeight.toInt(), startY, cols, rows, 15f)
        val borderSettings = coverConfig.imageBorderSettingsMap[pageData.groupId]

        pageData.imageUris.forEachIndexed { index, uriString ->
            if (index < rects.size) {
                val rect = rects[index]
                try {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val tempFile = File.createTempFile("hybrid_inner", ".jpg", context.cacheDir)
                        FileOutputStream(tempFile).use { outputStream -> inputStream.copyTo(outputStream) }

                        var bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                        imageEffectSettings[uriString]?.let {
                            bitmap = applyAllEffects(bitmap, it)
                        }

                        val alignment = when {
                            cols == 1 && rows == 1 -> ImageAlignment.CENTER
                            cols == 2 -> if (index == 0) ImageAlignment.RIGHT else ImageAlignment.LEFT
                            rows == 2 -> if (index == 0) ImageAlignment.BOTTOM else ImageAlignment.TOP
                            else -> ImageAlignment.CENTER
                        }

                        val finalRect = getFinalBitmapRect(bitmap, rect, alignment)
                        val clippingApplied = borderSettings != null && borderSettings.style != ImageBorderStyle.NONE

                        if (clippingApplied) {
                            applyPdfBoxClippingPath(contentStream, borderSettings!!, finalRect, pageHeight)
                        }

                        val imageXObject: PDImageXObject
                        if (coverConfig.hybridInnerImagesQuality < 100) {
                            val quality = coverConfig.hybridInnerImagesQuality
                            val scale = quality / 100f
                            val newWidth = (bitmap.width * scale).toInt()
                            val newHeight = (bitmap.height * scale).toInt()

                            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                            val resizedTempFile = File.createTempFile("resized_hybrid_inner", ".jpg", context.cacheDir)
                            FileOutputStream(resizedTempFile).use { out ->
                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                            }
                            imageXObject = PDImageXObject.createFromFile(resizedTempFile.absolutePath, pdDocument)
                            resizedTempFile.delete()
                            scaledBitmap.recycle()
                        } else {
                            imageXObject = PDImageXObject.createFromFile(tempFile.absolutePath, pdDocument)
                        }

                        val pdfBoxY = pageHeight - finalRect.bottom
                        contentStream.drawImage(imageXObject, finalRect.left, pdfBoxY, finalRect.width(), finalRect.height())

                        if (clippingApplied) {
                            contentStream.restoreGraphicsState()
                        }

                        bitmap.recycle()
                        tempFile.delete()
                    }
                } catch (e: Exception) {
                    Log.e("HybridPdf", "Error drawing inner page image with PDFBox", e)
                }
            }
        }
    } finally {
        try {
            contentStream.close()
        } catch (e: IOException) {
            Log.e("HybridPdf", "Error closing content stream for inner page.", e)
        }
    }
}
