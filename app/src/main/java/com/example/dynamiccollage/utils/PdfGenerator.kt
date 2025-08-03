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
        var currentY = contentArea.top

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        // Helper function to draw a text block and update currentY
        fun drawTextBlock(style: com.example.dynamiccollage.data.model.TextStyleConfig, rawContent: String, spacingAfter: Float = 0f) {
            if (rawContent.isBlank()) return

            // --- TYPEFACE AND FONT STYLE LOGIC ---
            val isBold = style.fontFamily.any { it.weight == FontWeight.Bold }
            val isItalic = style.fontStyle == FontStyle.Italic

            val typefaceStyle = when {
                isBold && isItalic -> Typeface.BOLD_ITALIC
                isBold -> Typeface.BOLD
                isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }

            // This assumes a single font family resource is defined in XML
            val fontFamilyRes = R.font.calibri // Assuming you have calibri.xml
            val typeface = try {
                val baseTypeface = ResourcesCompat.getFont(context, fontFamilyRes)
                Typeface.create(baseTypeface, typefaceStyle)
            } catch (e: Exception) {
                e.printStackTrace()
                Typeface.create(Typeface.DEFAULT, typefaceStyle) // Fallback
            }

            textPaint.typeface = typeface
            textPaint.color = style.fontColor.toArgb()
            textPaint.textSize = style.fontSize.value

            val alignment = getAndroidAlignment(style.textAlign)
            val staticLayout = StaticLayout.Builder.obtain(
                rawContent, 0, rawContent.length, textPaint, contentArea.width().toInt()
            ).setAlignment(alignment).build()

            canvas.save()
            canvas.translate(contentArea.left, currentY)
            staticLayout.draw(canvas)
            canvas.restore()
            currentY += staticLayout.height + spacingAfter
        }

        // --- Drawing Logic ---

        // Client
        var clientContent = if (config.allCaps) config.clientNameStyle.content.uppercase() else config.clientNameStyle.content
        clientContent = "Cliente: $clientContent"
        drawTextBlock(config.clientNameStyle, clientContent)

        // RUC
        var rucContent = if (config.allCaps) config.rucStyle.content.uppercase() else config.rucStyle.content
        rucContent = "RUC: $rucContent"
        drawTextBlock(config.rucStyle, rucContent, spacingAfter = config.spacingRucAddress * CM_TO_POINTS)

        // Address
        var addressContent = if (config.allCaps) config.subtitleStyle.content.uppercase() else config.subtitleStyle.content
        if (config.showAddressPrefix) {
            addressContent = "Dirección: $addressContent"
        }
        drawTextBlock(config.subtitleStyle, addressContent, spacingAfter = config.spacingAddressImage * CM_TO_POINTS)

        // Image takes the rest of the space
        config.mainImageUri?.let { uriString ->
            val imageRect = RectF(contentArea.left, currentY, contentArea.right, contentArea.bottom)
            if (imageRect.height() > 0) { // Only draw if there's space
                try {
                    val bitmap = decodeSampledBitmapFromUri(context, Uri.parse(uriString), imageRect.width().toInt(), imageRect.height().toInt())
                    if (bitmap != null) {
                        drawBitmapToCanvas(canvas, bitmap, imageRect)
                        bitmap.recycle()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        // Draw borders (unchanged)
        val borderPaint = Paint().apply {
            color = config.borderColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        if (config.borderVisibleTop) canvas.drawLine(0f, 0f, pageWidth.toFloat(), 0f, borderPaint)
        if (config.borderVisibleBottom) canvas.drawLine(0f, pageHeight.toFloat(), pageWidth.toFloat(), pageHeight.toFloat(), borderPaint)
        if (config.borderVisibleLeft) canvas.drawLine(0f, 0f, 0f, pageHeight.toFloat(), borderPaint)
        if (config.borderVisibleRight) canvas.drawLine(pageWidth.toFloat(), 0f, pageWidth.toFloat(), pageHeight.toFloat(), borderPaint)

        pdfDocument.finishPage(page)
    }

    private fun drawInnerPages(pdfDocument: PdfDocument, context: Context, pageGroups: List<PageGroup>) {
        var pageNumber = 2

        pageGroups.forEach { group ->
            if (group.imageUris.isEmpty()) return@forEach

            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            val optionalTextLayout = if (group.optionalTextStyle.isVisible) {
                textPaint.color = group.optionalTextStyle.fontColor.toArgb()
                textPaint.textSize = group.optionalTextStyle.fontSize.value
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

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                var currentY = 20f

                if (sheetIndex == 0 && optionalTextLayout != null) {
                    canvas.save()
                    canvas.translate(50f, currentY)
                    optionalTextLayout.draw(canvas)
                    canvas.restore()
                    currentY += optionalTextLayout.height + 20f
                }

                val rects = getRectsForPage(pageWidth, pageHeight, currentY, group.tableLayout.first, group.tableLayout.second)

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

    private fun getRectsForPage(pageWidth: Int, pageHeight: Int, startY: Float, cols: Int, rows: Int): List<RectF> {
        val rects = mutableListOf<RectF>()
        val cellWidth = pageWidth.toFloat() / cols
        val cellHeight = (pageHeight - startY) / rows

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val left = col * cellWidth
                val top = startY + (row * cellHeight)
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
