package com.example.dynamiccollage.utils

import androidx.compose.ui.unit.TextUnit
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
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
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import com.example.dynamiccollage.data.model.RowStyle
import com.example.dynamiccollage.data.model.TextStyleConfig
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private fun getAndroidAlignment(textAlign: TextAlign): Layout.Alignment {
        return when (textAlign) {
            TextAlign.Center -> Layout.Alignment.ALIGN_CENTER
            TextAlign.End -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_NORMAL
        }
    }

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val CM_TO_POINTS = 28.35f

    fun generate(
        context: Context,
        coverConfig: CoverPageConfig,
        pageGroups: List<PageGroup>,
        fileName: String
    ): File? {
        val pdfDocument = PdfDocument()

        drawCoverPage(pdfDocument, context, coverConfig)
        drawInnerPages(pdfDocument, context, pageGroups)

        try {
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            storageDir?.mkdirs()
            val pdfFile = File(storageDir, "$fileName.pdf")
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    /**
     * Genera una lista de Bitmaps para previsualizar las páginas del PDF.
     * NOTA: Esta funcionalidad es compleja y se deja pendiente.
     * Retorna una lista vacía para evitar errores de compilación.
     */
    fun generatePreviewBitmaps(
        context: Context,
        coverConfig: CoverPageConfig,
        pageGroups: List<PageGroup>
    ): List<Bitmap> {
        return emptyList()
    }

    private fun drawCoverPage(pdfDocument: PdfDocument, context: Context, config: CoverPageConfig) {
        val pageWidth = if (config.pageOrientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
        val pageHeight = if (config.pageOrientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val marginTop = config.marginTop * CM_TO_POINTS
        val marginBottom = config.marginBottom * CM_TO_POINTS
        val marginLeft = config.marginLeft * CM_TO_POINTS
        val marginRight = config.marginRight * CM_TO_POINTS

        val contentArea = RectF(marginLeft, marginTop, (pageWidth - marginRight), (pageHeight - marginBottom))

        val clientVisible = config.clientNameStyle.content.isNotBlank()
        val rucVisible = config.rucStyle.content.isNotBlank()
        val addressVisible = config.subtitleStyle.content.isNotBlank()
        val photoVisible = config.mainImageUri != null

        var totalWeight = 0f
        if(clientVisible) totalWeight += config.clientWeight
        if(rucVisible) totalWeight += config.rucWeight
        if(addressVisible) totalWeight += config.addressWeight
        if(photoVisible) totalWeight += config.photoWeight

        val visibleItemsCount = listOf(clientVisible, rucVisible, addressVisible, photoVisible).count { it }
        if (visibleItemsCount > 1) {
            totalWeight += config.separationWeight * (visibleItemsCount - 1)
        }

        if (totalWeight <= 0f) {
            pdfDocument.finishPage(page)
            return
        }

        var currentY = contentArea.top
        var isFirstElementDrawn = false

        fun drawSeparator() {
            if (isFirstElementDrawn) {
                currentY += contentArea.height() * (config.separationWeight / totalWeight)
            }
        }

        if (clientVisible) {
            val itemHeight = contentArea.height() * (config.clientWeight / totalWeight)
            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
            val content = "Cliente: " + if (config.allCaps) config.clientNameStyle.content.uppercase() else config.clientNameStyle.content
            drawRow(canvas, context, content, config.clientNameStyle, rect)
            currentY += itemHeight
            isFirstElementDrawn = true
        }

        if (rucVisible) {
            drawSeparator()
            val itemHeight = contentArea.height() * (config.rucWeight / totalWeight)
            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
            val content = "RUC: " + if (config.allCaps) config.rucStyle.content.uppercase() else config.rucStyle.content
            drawRow(canvas, context, content, config.rucStyle, rect)
            currentY += itemHeight
            isFirstElementDrawn = true
        }

        if (addressVisible) {
            drawSeparator()
            val itemHeight = contentArea.height() * (config.addressWeight / totalWeight)
            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
            var content = if (config.allCaps) config.subtitleStyle.content.uppercase() else config.subtitleStyle.content
            if (config.showAddressPrefix) content = "Dirección: $content"
            drawRow(canvas, context, content, config.subtitleStyle, rect)
            currentY += itemHeight
            isFirstElementDrawn = true
        }

        if (photoVisible) {
            drawSeparator()
            val itemHeight = contentArea.height() * (config.photoWeight / totalWeight)
            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
             drawRowBackgroundAndBorders(canvas, config.photoStyle, rect)
            config.mainImageUri?.let { uriString ->
                try {
                    val padding = config.photoStyle.padding
                    val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom)
                    val bitmap = decodeSampledBitmapFromUri(context, Uri.parse(uriString), paddedRect.width().toInt(), paddedRect.height().toInt())
                    bitmap?.let {
                        drawBitmapToCanvas(canvas, it, paddedRect)
                        it.recycle()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        pdfDocument.finishPage(page)
    }

    private fun drawRow(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) {
        drawRowBackgroundAndBorders(canvas, style.rowStyle, rect)
        drawTextInRect(canvas, context, text, style, rect)
    }

    private fun drawRowBackgroundAndBorders(canvas: Canvas, rowStyle: RowStyle, rect: RectF) {
        // Draw Background
        val backgroundPaint = Paint().apply {
            color = rowStyle.backgroundColor.toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawRect(rect, backgroundPaint)

        // Draw Borders
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


    private fun drawTextInRect(canvas: Canvas, context: Context, text: String, style: TextStyleConfig, rect: RectF) {
        if (text.isBlank()) return

        val padding = style.rowStyle.padding
        val paddedRect = RectF(rect.left + padding.left, rect.top + padding.top, rect.right - padding.right, rect.bottom - padding.bottom)
        if (paddedRect.width() <= 0 || paddedRect.height() <= 0) return

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
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

        val staticLayout = StaticLayout.Builder.obtain(
            text, 0, text.length, textPaint, paddedRect.width().toInt()
        ).setAlignment(getAndroidAlignment(style.textAlign)).build()

        val textY = paddedRect.top + (paddedRect.height() - staticLayout.height) / 2

        canvas.save()
        canvas.translate(paddedRect.left, textY)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawInnerPages(pdfDocument: PdfDocument, context: Context, pageGroups: List<PageGroup>) {
        var pageNumber = 2

        pageGroups.forEach { group ->
            if (group.imageUris.isEmpty()) return@forEach

            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            val optionalTextLayout = if (group.optionalTextStyle.content.isNotBlank()) {
                textPaint.color = group.optionalTextStyle.fontColor.toArgb()
                textPaint.textSize = group.optionalTextStyle.fontSize.toFloat()
                val alignment = getAndroidAlignment(group.optionalTextStyle.textAlign)
                StaticLayout.Builder.obtain(
                    group.optionalTextStyle.content, 0, group.optionalTextStyle.content.length, textPaint, A4_WIDTH - 100
                ).setAlignment(alignment).build()
            } else null

            var imageUriIndex = 0
            for (sheetIndex in 0 until group.sheetCount) {
                if(imageUriIndex >= group.imageUris.size) break

                val pageWidth = if (group.orientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
                val pageHeight = if (group.orientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH
                val pageMargin = 20f // Margin for inner pages

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                var currentY = pageMargin

                if (sheetIndex == 0 && optionalTextLayout != null) {
                    canvas.save()
                    canvas.translate(50f, currentY)
                    optionalTextLayout.draw(canvas)
                    canvas.restore()
                    currentY += optionalTextLayout.height + 20f
                }

                val rects = getRectsForPage(pageWidth, pageHeight, currentY, group.photosPerSheet.first, group.photosPerSheet.second, pageMargin, group.imageSpacing)

                for (rect in rects) {
                    if (imageUriIndex < group.imageUris.size) {
                        val uriString = group.imageUris[imageUriIndex++]
                        try {
                            val bitmap = decodeSampledBitmapFromUri(context, Uri.parse(uriString), rect.width().toInt(), rect.height().toInt())
                            if (bitmap != null) {
                                drawBitmapToCanvas(canvas, bitmap, rect)
                                bitmap.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
                pdfDocument.finishPage(page)
            }
        }
    }

    private fun getRectsForPage(pageWidth: Int, pageHeight: Int, startY: Float, cols: Int, rows: Int, margin: Float, spacing: Float): List<RectF> {
        val rects = mutableListOf<RectF>()
        val contentWidth = pageWidth - (2 * margin)
        val contentHeight = pageHeight - startY - margin

        val totalSpacingX = spacing * (cols - 1)
        val totalSpacingY = spacing * (rows - 1)

        val cellWidth = (contentWidth - totalSpacingX) / cols
        val cellHeight = (contentHeight - totalSpacingY) / rows

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val left = margin + (col * (cellWidth + spacing))
                val top = startY + (row * (cellHeight + spacing))
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

    private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use { finalInputStream ->
                BitmapFactory.decodeStream(finalInputStream, null, options)
            }
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
