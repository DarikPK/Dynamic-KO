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
import android.util.Log
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
        try {
            Log.d("PdfGenerator", "Iniciando la generación de PDF.")

            val shouldDrawCover = coverConfig.clientNameStyle.content.isNotBlank() ||
                    coverConfig.rucStyle.content.isNotBlank() ||
                    coverConfig.subtitleStyle.content.isNotBlank() ||
                    coverConfig.mainImageUri != null

            if (shouldDrawCover) {
                Log.d("PdfGenerator", "Dibujando portada.")
                drawCoverPage(pdfDocument, context, coverConfig)
            }
            Log.d("PdfGenerator", "Dibujando páginas interiores.")
            drawInnerPages(pdfDocument, context, pageGroups, if (shouldDrawCover) 2 else 1)

            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (storageDir == null) {
                Log.e("PdfGenerator", "El directorio de almacenamiento es nulo.")
                pdfDocument.close()
                return null
            }
            storageDir.mkdirs()
            val pdfFile = File(storageDir, "$fileName.pdf")
            Log.d("PdfGenerator", "Escribiendo en el archivo: ${pdfFile.absolutePath}")
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()
            Log.d("PdfGenerator", "PDF generado con éxito.")
            return pdfFile
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error al generar PDF", e)
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

        // --- Lógica de Diseño Refactorizada ---

        // 1. Crear una lista de todas las filas posibles
        val allRows = mutableListOf<Map<String, Any>>()
        if (config.clientNameStyle.content.isNotBlank()) {
            allRows.add(mapOf(
                "id" to "client",
                "weight" to config.clientWeight,
                "draw" to { rect: RectF ->
                    val content = "Cliente: " + if (config.allCaps) config.clientNameStyle.content.uppercase() else config.clientNameStyle.content
                    drawRow(canvas, context, content, config.clientNameStyle, rect)
                }
            ))
        }
        if (config.rucStyle.content.isNotBlank()) {
            allRows.add(mapOf(
                "id" to "ruc",
                "weight" to config.rucWeight,
                "draw" to { rect: RectF ->
                    val documentLabel = if (config.documentType == com.example.dynamiccollage.data.model.DocumentType.DNI) "DNI: " else "RUC: "
                    val content = documentLabel + if (config.allCaps) config.rucStyle.content.uppercase() else config.rucStyle.content
                    drawRow(canvas, context, content, config.rucStyle, rect)
                }
            ))
        }
        if (config.subtitleStyle.content.isNotBlank()) {
            allRows.add(mapOf(
                "id" to "address",
                "weight" to 0f, // El peso ya no se usa para la dirección
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
                            val bitmap = decodeSampledBitmapFromUri(context, Uri.parse(uriString), paddedRect.width().toInt(), paddedRect.height().toInt())
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
            pdfDocument.finishPage(page)
            return
        }

        // 2. Calcular la altura de la fila de dirección dinámicamente
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

        // 3. Calcular el espacio restante y los pesos para las otras filas
        val weightedRows = allRows.filter { it["id"] != "address" }
        val totalWeight = weightedRows.sumOf { (it["weight"] as Float).toDouble() }.toFloat()

        var separations = 0
        for (i in 0 until allRows.size - 1) {
            val currentId = allRows[i]["id"] as String
            val nextId = allRows[i+1]["id"] as String
            // No contar separación entre cliente y RUC
            if (currentId == "client" && nextId == "ruc") continue
            // No contar la separación después de la dirección, ya que es fija
            if (currentId == "address") continue
            separations++
        }

        val totalSeparationWeight = config.separationWeight * separations
        val finalTotalWeight = totalWeight + totalSeparationWeight

        val fixedSpace = if (addressRowData != null) addressRowHeight + 5f else 0f
        val availableHeight = contentArea.height() - fixedSpace

        if (finalTotalWeight <= 0f && availableHeight <= 0f) {
            pdfDocument.finishPage(page)
            return
        }

        val separationHeight = if (finalTotalWeight > 0) availableHeight * (config.separationWeight / finalTotalWeight) else 0f

        // 4. Dibujar todas las filas
        var currentY = contentArea.top
        allRows.forEachIndexed { index, rowData ->
            val id = rowData["id"] as String
            val drawFunc = rowData["draw"] as (RectF) -> Unit

            val itemHeight = if (id == "address") {
                addressRowHeight
            } else {
                if (finalTotalWeight > 0) availableHeight * ((rowData["weight"] as Float) / finalTotalWeight) else 0f
            }

            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + itemHeight)
            drawFunc(rect)
            currentY += itemHeight

            // Añadir separación
            if (index < allRows.size - 1) {
                val nextId = allRows[index + 1]["id"] as String
                if (id == "address") {
                    currentY += 5f // Espacio fijo después de la dirección
                } else if (!(id == "client" && nextId == "ruc")) {
                    currentY += separationHeight
                }
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

    private fun drawInnerPages(pdfDocument: PdfDocument, context: Context, pageGroups: List<PageGroup>, startPageNumber: Int) {
        var pageNumber = startPageNumber

        pageGroups.forEach { group ->
            if (group.imageUris.isEmpty()) return@forEach

            var imageUriIndex = 0
            for (sheetIndex in 0 until group.sheetCount) {
                if(imageUriIndex >= group.imageUris.size) break

                val pageWidth = if (group.orientation == PageOrientation.Vertical) A4_WIDTH else A4_HEIGHT
                val pageHeight = if (group.orientation == PageOrientation.Vertical) A4_HEIGHT else A4_WIDTH

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val startY = 20f

                if (sheetIndex == 0 && group.optionalTextStyle.isVisible && group.isPhotoQuotaMet) {
                    val style = group.optionalTextStyle
                    val content = if (style.allCaps) style.content.uppercase() else style.content
                    // Draw text in the top margin area, above the main content
                    val textRect = RectF(50f, 0f, pageWidth - 50f, startY)
                    drawRow(canvas, context, content, style, textRect)
                }

                val rects = getRectsForPage(pageWidth, pageHeight, startY, group.tableLayout.first, group.tableLayout.second, group.imageSpacing)

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
