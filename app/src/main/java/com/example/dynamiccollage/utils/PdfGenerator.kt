package com.example.dynamiccollage.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.GeneratedPage
import com.example.dynamiccollage.data.model.PageOrientation
import com.example.dynamiccollage.data.model.TextStyleConfig
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min

object PdfGenerator {

    private const val A4_WIDTH = 595f
    private const val A4_HEIGHT = 842f
    private const val CM_TO_POINTS = 28.35f
    private const val A4_MAX_WIDTH_PX = 2480
    private const val A4_MAX_HEIGHT_PX = 3508

    private val fontCache = mutableMapOf<String, PDType0Font>()

    fun generate(
        context: Context,
        coverConfig: CoverPageConfig,
        generatedPages: List<GeneratedPage>,
        fileName: String
    ): File? {
        val pdDocument = PDDocument()
        try {
            val shouldDrawCover = coverConfig.clientNameStyle.content.isNotBlank() ||
                    coverConfig.rucStyle.content.isNotBlank() ||
                    coverConfig.subtitleStyle.content.isNotBlank() ||
                    coverConfig.mainImageUri != null

            if (shouldDrawCover) {
                drawCoverPage(pdDocument, context, coverConfig)
            }
            drawInnerPages(pdDocument, context, generatedPages)

            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            storageDir?.mkdirs()
            val pdfFile = File(storageDir, "$fileName.pdf")
            pdDocument.save(pdfFile)
            return pdfFile
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error al generar PDF", e)
            return null
        } finally {
            fontCache.clear()
            pdDocument.close()
        }
    }

    private fun getFont(context: Context, document: PDDocument, fontName: String): PDType0Font {
        return fontCache.getOrPut(fontName) {
            // A simple font loading, assuming fonts are in assets
            context.assets.open("calibri_regular.ttf").use {
                PDType0Font.load(document, it)
            }
        }
    }

    private fun processImageForPdf(context: Context, uri: Uri): ByteArray? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

            options.inSampleSize = calculateInSampleSize(options, A4_MAX_WIDTH_PX, A4_MAX_HEIGHT_PX)
            options.inJustDecodeBounds = false

            val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
            val scaledBitmap = scaleBitmapToA4(bitmap)
            if (scaledBitmap != bitmap) bitmap.recycle()

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            scaledBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error processing image for PDF", e)
            null
        }
    }

    private fun scaleBitmapToA4(bitmap: Bitmap): Bitmap {
        val ratio = min(A4_MAX_WIDTH_PX.toFloat() / bitmap.width, A4_MAX_HEIGHT_PX.toFloat() / bitmap.height)
        if (ratio >= 1.0f) return bitmap
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
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

    private fun drawWrappedText(
        document: PDDocument,
        contentStream: PDPageContentStream,
        context: Context,
        text: String,
        style: TextStyleConfig,
        bounds: PDRectangle,
        yPos: Float
    ) {
        val font = getFont(context, document, "calibri_regular.ttf") // Simplified font selection
        val fontSize = style.fontSize
        val lines = mutableListOf<String>()
        val words = text.split(" ").iterator()
        var currentLine = ""
        while(words.hasNext()){
            val word = words.next()
            val prospectiveLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = font.getStringWidth(prospectiveLine) / 1000 * fontSize
            if(width > bounds.width){
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = prospectiveLine
            }
        }
        lines.add(currentLine)

        val leading = fontSize * 1.2f
        contentStream.beginText()
        contentStream.setFont(font, fontSize)
        contentStream.setNonStrokingColor(style.fontColor.toArgb())

        var y = yPos
        lines.forEach { line ->
            val textWidth = font.getStringWidth(line) / 1000 * fontSize
            val startX = when (style.textAlign) {
                TextAlign.Center -> bounds.lowerLeftX + (bounds.width - textWidth) / 2
                TextAlign.End -> bounds.lowerLeftX + bounds.width - textWidth
                else -> bounds.lowerLeftX
            }
            contentStream.newLineAtOffset(startX, y)
            contentStream.showText(line)
            y -= leading
            contentStream.newLineAtOffset(-startX, -leading)
        }
        contentStream.endText()
    }

    private fun drawCoverPage(document: PDDocument, context: Context, config: CoverPageConfig) {
        val isVertical = config.pageOrientation == PageOrientation.Vertical
        val pageSize = if (isVertical) PDRectangle.A4 else PDRectangle(A4_HEIGHT, A4_WIDTH)
        val page = PDPage(pageSize)
        document.addPage(page)
        val contentStream = PDPageContentStream(document, page)

        val marginTop = config.marginTop * CM_TO_POINTS
        val marginBottom = config.marginBottom * CM_TO_POINTS
        val marginLeft = config.marginLeft * CM_TO_POINTS
        val marginRight = config.marginRight * CM_TO_POINTS
        val contentArea = PDRectangle(marginLeft, marginBottom, pageSize.width - marginLeft - marginRight, pageSize.height - marginTop - marginBottom)

        var currentY = contentArea.upperRightY

        if (config.clientNameStyle.content.isNotBlank()) {
            val text = (if (config.showClientPrefix) "Cliente: " else "") + if (config.allCaps) config.clientNameStyle.content.uppercase() else config.clientNameStyle.content
            currentY -= 50f
            drawWrappedText(document, contentStream, context, text, config.clientNameStyle, contentArea, currentY)
        }
        if (config.rucStyle.content.isNotBlank()) {
            val documentLabel = when (config.documentType) {
                com.example.dynamiccollage.data.model.DocumentType.DNI -> "DNI: "
                com.example.dynamiccollage.data.model.DocumentType.RUC -> "RUC: "
                else -> ""
            }
            val text = documentLabel + if (config.allCaps) config.rucStyle.content.uppercase() else config.rucStyle.content
             currentY -= 30f
            drawWrappedText(document, contentStream, context, text, config.rucStyle, contentArea, currentY)
        }
        if (config.subtitleStyle.content.isNotBlank()) {
            val text = (if (config.showAddressPrefix) "Dirección: " else "") + if (config.allCaps) config.subtitleStyle.content.uppercase() else config.subtitleStyle.content
             currentY -= 30f
            drawWrappedText(document, contentStream, context, text, config.subtitleStyle, contentArea, currentY)
        }

        if (config.mainImageUri != null) {
            try {
                val jpegBytes = processImageForPdf(context, Uri.parse(config.mainImageUri!!))
                jpegBytes?.let { bytes ->
                    val imageXObject = PDImageXObject.createFromByteArray(document, bytes, "cover-img")
                    val imgHeight = contentArea.height / 2f
                    val imgWidth = imgHeight * imageXObject.width / imageXObject.height
                    val x = contentArea.lowerLeftX + (contentArea.width - imgWidth) / 2f
                    val y = contentArea.lowerLeftY
                    contentStream.drawImage(imageXObject, x, y, imgWidth, imgHeight)
                }
            } catch (e: Exception) {
                Log.e("PdfGenerator", "Error drawing cover image", e)
            }
        }
        contentStream.close()
    }

    private fun getRectsForPage(pageWidth: Float, pageHeight: Float, cols: Int, rows: Int, spacing: Float): List<PDRectangle> {
        val rects = mutableListOf<PDRectangle>()
        val totalSpacingX = spacing * (cols + 1)
        val totalSpacingY = spacing * (rows + 1)
        val cellWidth = (pageWidth - totalSpacingX) / cols
        val cellHeight = (pageHeight - totalSpacingY) / rows
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = spacing + col * (cellWidth + spacing)
                val y = pageHeight - (spacing + (row + 1) * cellHeight + row * spacing)
                rects.add(PDRectangle(x, y, cellWidth, cellHeight))
            }
        }
        return rects
    }

    private fun drawInnerPages(document: PDDocument, context: Context, generatedPages: List<GeneratedPage>) {
        generatedPages.forEach { pageData ->
            val isVertical = pageData.orientation == PageOrientation.Vertical
            val pageSize = if (isVertical) PDRectangle.A4 else PDRectangle(A4_HEIGHT, A4_WIDTH)
            val page = PDPage(pageSize)
            document.addPage(page)
            val contentStream = PDPageContentStream(document, page)

            val (cols, rows) = when {
                !isVertical && pageData.imageUris.size > 1 -> Pair(2, 1)
                isVertical && pageData.imageUris.size > 1 -> Pair(1, 2)
                else -> Pair(1, 1)
            }

            val rects = getRectsForPage(page.mediaBox.width, page.mediaBox.height, cols, rows, 15f)

            pageData.imageUris.forEachIndexed { index, uriString ->
                if (index < rects.size) {
                    val rect = rects[index]
                    try {
                        val jpegBytes = processImageForPdf(context, Uri.parse(uriString))
                        jpegBytes?.let {
                            val imageXObject = PDImageXObject.createFromByteArray(document, it, "img-$index")
                            contentStream.drawImage(imageXObject, rect.lowerLeftX, rect.lowerLeftY, rect.width, rect.height)
                        }
                    } catch (e: Exception) {
                        Log.e("PdfGenerator", "Error drawing image on page", e)
                    }
                }
            }
            contentStream.close()
        }
    }
}
