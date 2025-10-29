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
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import android.graphics.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private fun applyAllEffects(input: Bitmap, settings: ImageEffectSettings): Bitmap {
        var processedBitmap = input

        // 1. Apply Crop
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

        // 2. Apply Rotation
        if (settings.rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(settings.rotationDegrees) }
            processedBitmap = Bitmap.createBitmap(processedBitmap, 0, 0, processedBitmap.width, processedBitmap.height, matrix, true)
        }

        // 3. Apply Color & Sharpness Effects
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

    private enum class ImageAlignment { TOP, BOTTOM, LEFT, RIGHT, CENTER }

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val CM_TO_POINTS = 28.35f

    fun generate(
        context: Context,
        coverConfig: CoverPageConfig,
        generatedPages: List<GeneratedPage>,
        fileName: String,
        imageEffectSettings: Map<String, ImageEffectSettings>
    ): File? {
        if (coverConfig.useHybridPdfMode) {
            return generateHybridPdf(context, coverConfig, generatedPages, fileName, imageEffectSettings)
        }

        if (coverConfig.forceFullResCover) {
            // TODO: Implementar la ruta de generación con PDFBox
            Log.d("PdfGenerator", "Usando la ruta de alta calidad con PDFBox.")
            return generateWithPdfBox(context, coverConfig, generatedPages, fileName, imageEffectSettings)
        }

        // Ruta estándar con PdfDocument para previsualización rápida
        Log.d("PdfGenerator", "Usando la ruta estándar con PdfDocument.")
        val pdfDocument = PdfDocument()
        val tempFile = File.createTempFile("uncompressed_pdf", ".pdf", context.cacheDir)

        try {
            val quality = coverConfig.quality

            val shouldDrawCover = coverConfig.clientNameStyle.content.isNotBlank() ||
                    coverConfig.rucStyle.content.isNotBlank() ||
                    coverConfig.subtitleStyle.content.isNotBlank() ||
                    coverConfig.mainImageUri != null

            val colorTheme = coverConfig.templateName?.let { name ->
                ColorThemes.themes.find { it.name == name }
            } ?: ColorThemes.themes.first()
            if (shouldDrawCover) {
                drawCoverPage(pdfDocument, context, coverConfig, quality, imageEffectSettings, renderImages = true, colorTheme)
            }
            drawInnerPages(pdfDocument, context, generatedPages, coverConfig, if (shouldDrawCover) 2 else 1, quality, imageEffectSettings, renderImages = true, colorTheme)

            val fileOutputStream = FileOutputStream(tempFile)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            storageDir?.mkdirs()
            val pdfFile = File(storageDir, "$fileName.pdf")

            val pdDocument = PDDocument.load(tempFile)
            pdDocument.version = 1.5f
            pdDocument.save(pdfFile)
            pdDocument.close()

            return pdfFile
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error al generar PDF con PdfDocument", e)
            pdfDocument.close()
            return null
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

// === FUNCIONES CORREGIDAS PARA PDFBOX EN ANDROID ===

private fun generateWithPdfBox(
    context: Context,
    coverConfig: CoverPageConfig,
    generatedPages: List<GeneratedPage>,
    fileName: String,
    imageEffectSettings: Map<String, ImageEffectSettings>
): File? {
    val pdDocument = PDDocument()
    return try {
        Log.d("PdfGenerator", "Iniciando ruta PDFBox (alta calidad)...")

        val shouldDrawCover = coverConfig.clientNameStyle.content.isNotBlank() ||
                coverConfig.rucStyle.content.isNotBlank() ||
                coverConfig.subtitleStyle.content.isNotBlank() ||
                coverConfig.mainImageUri != null

        val colorTheme = coverConfig.templateName?.let { name ->
            ColorThemes.themes.find { it.name == name }
        } ?: ColorThemes.themes.first()
        if (shouldDrawCover) {
            drawCoverPageWithPdfBox(context, pdDocument, coverConfig, imageEffectSettings, colorTheme)
        }
        drawInnerPagesWithPdfBox(context, pdDocument, generatedPages, coverConfig, imageEffectSettings, colorTheme)

        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        storageDir?.mkdirs()
        val pdfFile = File(storageDir, "$fileName.pdf")

        pdDocument.save(pdfFile)
        Log.d("PdfGenerator", "PDFBox: PDF guardado correctamente en ${pdfFile.absolutePath}")
        pdfFile
    } catch (e: Exception) {
        Log.e("PdfBoxCrash", "Error al generar PDF con PDFBox: ${Log.getStackTraceString(e)}")
        null
    } finally {
        try { pdDocument.close() } catch (_: Exception) {}
    }
}

private fun drawCoverPageWithPdfBox(
    context: Context,
    pdDocument: PDDocument,
    config: CoverPageConfig,
    imageEffectSettings: Map<String, ImageEffectSettings>,
    colorTheme: ColorTheme
) {
    val isVertical = config.pageOrientation == PageOrientation.Vertical
    val mediaBox = if (isVertical) PDRectangle.A4 else PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)
    val page = PDPage(mediaBox)
    pdDocument.addPage(page)
    val contentStream = PDPageContentStream(pdDocument, page)

    try {
        val pageWidth = page.mediaBox.width
        val pageHeight = page.mediaBox.height

        config.pageBackgroundColor?.let {
            val r = Color.red(it)
            val g = Color.green(it)
            val b = Color.blue(it)
            contentStream.setNonStrokingColor(r, g, b)
            contentStream.addRect(0f, 0f, pageWidth, pageHeight)
            contentStream.fill()
        }

        config.generatedBackgroundConfig?.let {
            if (it.enabled) {
                val backgroundBitmap = Bitmap.createBitmap(pageWidth.toInt(), pageHeight.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(backgroundBitmap)
                BackgroundGenerator.drawGeneratedBackground(canvas, it, pageWidth, pageHeight, colorTheme)

                val tempFile = File.createTempFile("background", ".png", context.cacheDir)
                FileOutputStream(tempFile).use { out ->
                    backgroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val pdImage = PDImageXObject.createFromFile(tempFile.absolutePath, pdDocument)
                contentStream.drawImage(pdImage, 0f, 0f, pageWidth, pageHeight)
                tempFile.delete()
                backgroundBitmap.recycle()
            }
        }

        val marginTop = config.marginTop * CM_TO_POINTS
        val marginBottom = config.marginBottom * CM_TO_POINTS
        val marginLeft = config.marginLeft * CM_TO_POINTS
        val marginRight = config.marginRight * CM_TO_POINTS
        val contentArea = RectF(marginLeft, marginTop, (pageWidth - marginRight), (pageHeight - marginBottom))

        val rows = mutableListOf<Map<String, Any>>()
        if (config.clientNameStyle.content.isNotBlank()) {
            rows.add(mapOf("id" to "client", "weight" to config.clientWeight, "style" to config.clientNameStyle, "content" to config.clientNameStyle.content))
        }
        if (config.rucStyle.content.isNotBlank()) {
            rows.add(mapOf("id" to "ruc", "weight" to config.rucWeight, "style" to config.rucStyle, "content" to config.rucStyle.content))
        }
        if (config.subtitleStyle.content.isNotBlank()) {
            rows.add(mapOf("id" to "address", "weight" to 0f, "style" to config.subtitleStyle, "content" to config.subtitleStyle.content))
        }
        if (config.mainImageUri != null) {
            rows.add(mapOf("id" to "photo", "weight" to config.photoWeight, "uri" to config.mainImageUri!!))
        }

        val totalWeight = rows.sumOf { (it["weight"] as Float).toDouble() }.toFloat()
        var currentY = contentArea.top

        rows.forEach { row ->
            val id = row["id"] as String
            val height = if (totalWeight > 0) contentArea.height() * ((row["weight"] as Float) / totalWeight) else 0f
            val rect = RectF(contentArea.left, currentY, contentArea.right, currentY + height)

            if (id == "photo") {
                val uri = Uri.parse(row["uri"] as String)
                try {
                    val tmpFile = File.createTempFile("pdf_img", ".jpg", context.cacheDir)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tmpFile.outputStream().use { out -> input.copyTo(out) }
                    }
                    val image = PDImageXObject.createFromFileByContent(tmpFile, pdDocument)
                    tmpFile.delete()
                    contentStream.drawImage(image, rect.left, pageHeight - rect.bottom, rect.width(), rect.height())
                } catch (e: Exception) {
                    Log.e("PdfBoxCrash", "Error cargando imagen: ${e.message}")
                }
            } else if (row.containsKey("content")) {
                val text = row["content"] as String
                val style = row["style"] as TextStyleConfig
                val font = getPdfBoxFont(context, pdDocument, style.fontWeight ?: FontWeight.Normal, style.fontStyle ?: FontStyle.Normal)
                val fontSize = style.fontSize.toFloat()
                val textWidth = font.getStringWidth(text) / 1000 * fontSize
                val textX = rect.left + (rect.width() - textWidth) / 2f
                val textY = pageHeight - rect.top - (rect.height() / 2f) - (fontSize / 4f)
                contentStream.beginText()
                contentStream.setFont(font, fontSize)
                val fontColorInt = style.fontColor.toArgb()
                val r = Color.red(fontColorInt)
                val g = Color.green(fontColorInt)
                val b = Color.blue(fontColorInt)
                contentStream.setNonStrokingColor(r, g, b)
                contentStream.newLineAtOffset(textX, textY)
                contentStream.showText(text)
                contentStream.endText()
            }
            currentY += height + 5f
        }
    } finally {
        try { contentStream.close() } catch (_: Exception) {}
    }
}

private fun drawInnerPagesWithPdfBox(
    context: Context,
    pdDocument: PDDocument,
    generatedPages: List<GeneratedPage>,
    coverConfig: CoverPageConfig,
    imageEffectSettings: Map<String, ImageEffectSettings>,
    colorTheme: ColorTheme
) {
    generatedPages.forEach { pageData ->
        val isVertical = pageData.orientation == PageOrientation.Vertical
        val mediaBox = if (isVertical) PDRectangle.A4 else PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)
        val page = PDPage(mediaBox)
        pdDocument.addPage(page)
        val contentStream = PDPageContentStream(pdDocument, page)

        try {
            val pageWidth = page.mediaBox.width
            val pageHeight = page.mediaBox.height

            coverConfig.pageBackgroundColor?.let {
                val r = Color.red(it)
                val g = Color.green(it)
                val b = Color.blue(it)
                contentStream.setNonStrokingColor(r, g, b)
                contentStream.addRect(0f, 0f, pageWidth, pageHeight)
                contentStream.fill()
            }

            coverConfig.generatedBackgroundConfig?.let {
                if (it.enabled) {
                    val backgroundBitmap = Bitmap.createBitmap(pageWidth.toInt(), pageHeight.toInt(), Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(backgroundBitmap)
                    BackgroundGenerator.drawGeneratedBackground(canvas, it, pageWidth, pageHeight, colorTheme)

                    val tempFile = File.createTempFile("background_inner", ".png", context.cacheDir)
                    FileOutputStream(tempFile).use { out ->
                        backgroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    val pdImage = PDImageXObject.createFromFile(tempFile.absolutePath, pdDocument)
                    contentStream.drawImage(pdImage, 0f, 0f, pageWidth, pageHeight)
                    tempFile.delete()
                    backgroundBitmap.recycle()
                }
            }

            val marginTop = coverConfig.marginTop * CM_TO_POINTS
            val marginBottom = coverConfig.marginBottom * CM_TO_POINTS
            val marginLeft = coverConfig.marginLeft * CM_TO_POINTS
            val marginRight = coverConfig.marginRight * CM_TO_POINTS
            val contentWidth = pageWidth - marginLeft - marginRight
            val contentHeight = pageHeight - marginTop - marginBottom

            val imagesPerPage = pageData.imagesPerPage
            val uris = pageData.imageUris.take(imagesPerPage)
            val spacing = pageData.imageSpacing

            when (imagesPerPage) {
                1 -> {
                    if (uris.isNotEmpty()) {
                        drawImage(context, pdDocument, contentStream, uris[0], marginLeft, pageHeight - marginTop - contentHeight, contentWidth, contentHeight)
                    }
                }
                2 -> {
                    val imgHeight = (contentHeight - spacing) / 2f
                    if (uris.size >= 1) {
                        drawImage(context, pdDocument, contentStream, uris[0], marginLeft, pageHeight - marginTop - imgHeight, contentWidth, imgHeight)
                    }
                    if (uris.size >= 2) {
                        drawImage(context, pdDocument, contentStream, uris[1], marginLeft, pageHeight - marginTop - imgHeight * 2 - spacing, contentWidth, imgHeight)
                    }
                }
                4 -> {
                    val imgWidth = (contentWidth - spacing) / 2f
                    val imgHeight = (contentHeight - spacing) / 2f
                    if (uris.size >= 1) drawImage(context, pdDocument, contentStream, uris[0], marginLeft, pageHeight - marginTop - imgHeight, imgWidth, imgHeight)
                    if (uris.size >= 2) drawImage(context, pdDocument, contentStream, uris[1], marginLeft + imgWidth + spacing, pageHeight - marginTop - imgHeight, imgWidth, imgHeight)
                    if (uris.size >= 3) drawImage(context, pdDocument, contentStream, uris[2], marginLeft, pageHeight - marginTop - imgHeight * 2 - spacing, imgWidth, imgHeight)
                    if (uris.size >= 4) drawImage(context, pdDocument, contentStream, uris[3], marginLeft + imgWidth + spacing, pageHeight - marginTop - imgHeight * 2 - spacing, imgWidth, imgHeight)
                }
            }
        } finally {
            try { contentStream.close() } catch (_: Exception) {}
        }
    }
}

private fun drawImage(
    context: Context,
    pdDocument: PDDocument,
    contentStream: PDPageContentStream,
    uriString: String,
    x: Float,
    y: Float,
    width: Float,
    height: Float
) {
    try {
        val uri = Uri.parse(uriString)
        val tmpFile = File.createTempFile("pdf_img_inner", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tmpFile.outputStream().use { out -> input.copyTo(out) }
        }
        val image = PDImageXObject.createFromFileByContent(tmpFile, pdDocument)
        tmpFile.delete()
        contentStream.drawImage(image, x, y, width, height)
    } catch (e: Exception) {
        Log.e("PdfBoxCrash", "Error en drawImage para URI: $uriString", e)
    }
}

private fun getPdfBoxFont(
    context: Context,
    pdDocument: PDDocument,
    fontWeight: FontWeight,
    fontStyle: FontStyle
): PDFont {
    val fallbackFont = "Roboto-Regular.ttf"
    val fontName = when {
        fontWeight == FontWeight.Bold && fontStyle == FontStyle.Italic -> "Roboto-BoldItalic.ttf"
        fontWeight == FontWeight.Bold -> "Roboto-Bold.ttf"
        fontStyle == FontStyle.Italic -> "Roboto-Italic.ttf"
        else -> fallbackFont
    }

    return try {
        PDType0Font.load(pdDocument, context.assets.open("fonts/$fontName"))
    } catch (e: Exception) {
        Log.e("PdfGenerator", "⚠️ No se pudo cargar la fuente $fontName, usando respaldo.", e)
        PDType0Font.load(pdDocument, context.assets.open("fonts/$fallbackFont"))
    }
}

}
