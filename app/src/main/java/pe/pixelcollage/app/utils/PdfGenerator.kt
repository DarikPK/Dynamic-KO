package pe.pixelcollage.app.utils

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
import pe.pixelcollage.app.data.model.*
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

import pe.pixelcollage.app.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import pe.pixelcollage.app.data.model.User
import pe.pixelcollage.app.viewmodel.UserState

object PdfGenerator {

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
            ImageAlignment.CENTER -> {
                x += (cellWidth - newWidth) / 2
                y += (cellHeight - newHeight) / 2
            }
            ImageAlignment.LEFT -> {
                x = cellRect.left
                y += (cellHeight - newHeight) / 2
            }
            ImageAlignment.RIGHT -> {
                x = cellRect.right - newWidth
                y += (cellHeight - newHeight) / 2
            }
            ImageAlignment.TOP -> {
                x += (cellWidth - newWidth) / 2
                y = cellRect.top
            }
            ImageAlignment.BOTTOM -> {
                x += (cellWidth - newWidth) / 2
                y = cellRect.bottom - newHeight
            }
        }
        return RectF(x, y, x + newWidth, y + newHeight)
    }

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

        // Establecer fondo con el color sólido o blanco por defecto
        val configBg = config.generatedBackgroundConfig
        val backgroundColor = if (configBg != null && (configBg.combineWithSolidColor || configBg.patternType == BackgroundPatternType.SÓLIDO)) {
            configBg.solidColor
        } else {
            androidx.compose.ui.graphics.Color.White
        }
        val colorInt = backgroundColor.toArgb()
        contentStream.setNonStrokingColor(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
        contentStream.addRect(0f, 0f, pageWidth, pageHeight)
        contentStream.fill()

        config.generatedBackgroundConfig?.let {
            if (it.patternType != BackgroundPatternType.SÓLIDO) {
                val backgroundBitmap = Bitmap.createBitmap(pageWidth.toInt(), pageHeight.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(backgroundBitmap)
                BackgroundGenerator.drawGeneratedBackground(canvas, it, pageWidth, pageHeight)

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

                    var bitmap = BitmapFactory.decodeFile(tmpFile.absolutePath)
                    val settings = imageEffectSettings[row["uri"] as String]
                    if (settings != null) {
                        bitmap = applyAllEffects(bitmap, settings)
                    }

                    val finalRect = getFinalBitmapRect(bitmap, rect, ImageAlignment.CENTER)
                    val borderSettings = config.imageBorderSettingsMap["cover"] ?: ImageBorderSettings()
                    val clippingApplied = borderSettings.style != ImageBorderStyle.NONE

                    if (clippingApplied) {
                        applyPdfBoxClippingPath(contentStream, borderSettings, finalRect, pageHeight)
                    }

                    val image = PDImageXObject.createFromFileByContent(tmpFile, pdDocument)
                    contentStream.drawImage(image, finalRect.left, pageHeight - finalRect.bottom, finalRect.width(), finalRect.height())

                    if (clippingApplied) {
                        contentStream.restoreGraphicsState()
                    }

                    tmpFile.delete()
                    bitmap.recycle()
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

            // Establecer fondo con el color sólido o blanco por defecto
            val configBg = coverConfig.generatedBackgroundConfig
            val backgroundColor = if (configBg != null && (configBg.combineWithSolidColor || configBg.patternType == BackgroundPatternType.SÓLIDO)) {
                configBg.solidColor
            } else {
                androidx.compose.ui.graphics.Color.White
            }
            val colorInt = backgroundColor.toArgb()
            contentStream.setNonStrokingColor(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
            contentStream.addRect(0f, 0f, pageWidth, pageHeight)
            contentStream.fill()

            coverConfig.generatedBackgroundConfig?.let {
                if (it.patternType != BackgroundPatternType.SÓLIDO) {
                    val backgroundBitmap = Bitmap.createBitmap(pageWidth.toInt(), pageHeight.toInt(), Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(backgroundBitmap)
                    BackgroundGenerator.drawGeneratedBackground(canvas, it, pageWidth, pageHeight)

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
            val spacingInPoints = pageData.imageSpacing * 0.75f // Convert dp to points

            val borderSettings = coverConfig.imageBorderSettingsMap[pageData.groupId] ?: ImageBorderSettings()

            when (imagesPerPage) {
                1 -> {
                    if (uris.isNotEmpty()) {
                        drawImage(context, pdDocument, contentStream, uris[0], marginLeft, pageHeight - marginTop - contentHeight, contentWidth, contentHeight, ImageAlignment.CENTER, borderSettings, imageEffectSettings, pageHeight)
                    }
                }
                2 -> {
                    if (pageData.orientation == PageOrientation.Vertical) {
                        // Separación vertical
                        val imgHeight = (contentHeight - spacingInPoints) / 2f
                        if (uris.size >= 1) {
                            drawImage(context, pdDocument, contentStream, uris[0], marginLeft, pageHeight - marginTop - imgHeight, contentWidth, imgHeight, ImageAlignment.BOTTOM, borderSettings, imageEffectSettings, pageHeight)
                        }
                        if (uris.size >= 2) {
                            drawImage(context, pdDocument, contentStream, uris[1], marginLeft, pageHeight - marginTop - (imgHeight * 2) - spacingInPoints, contentWidth, imgHeight, ImageAlignment.TOP, borderSettings, imageEffectSettings, pageHeight)
                        }
                    } else { // Orientación Horizontal
                        // Separación horizontal
                        val imgWidth = (contentWidth - spacingInPoints) / 2f
                        if (uris.size >= 1) {
                            drawImage(context, pdDocument, contentStream, uris[0], marginLeft, pageHeight - marginTop - contentHeight, imgWidth, contentHeight, ImageAlignment.RIGHT, borderSettings, imageEffectSettings, pageHeight)
                        }
                        if (uris.size >= 2) {
                            drawImage(context, pdDocument, contentStream, uris[1], marginLeft + imgWidth + spacingInPoints, pageHeight - marginTop - contentHeight, imgWidth, contentHeight, ImageAlignment.LEFT, borderSettings, imageEffectSettings, pageHeight)
                        }
                    }
                }
                // El caso 4 se mantiene como estaba, ya que la lógica original ya era correcta para una cuadrícula.
                4 -> {
                    val imgWidth = (contentWidth - spacingInPoints) / 2f
                    val imgHeight = (contentHeight - spacingInPoints) / 2f
                    if (uris.size >= 1) drawImage(context, pdDocument, contentStream, uris[0], marginLeft, pageHeight - marginTop - imgHeight, imgWidth, imgHeight, ImageAlignment.CENTER, borderSettings, imageEffectSettings, pageHeight)
                    if (uris.size >= 2) drawImage(context, pdDocument, contentStream, uris[1], marginLeft + imgWidth + spacingInPoints, pageHeight - marginTop - imgHeight, imgWidth, imgHeight, ImageAlignment.CENTER, borderSettings, imageEffectSettings, pageHeight)
                    if (uris.size >= 3) drawImage(context, pdDocument, contentStream, uris[2], marginLeft, pageHeight - marginTop - (imgHeight * 2) - spacingInPoints, imgWidth, imgHeight, ImageAlignment.CENTER, borderSettings, imageEffectSettings, pageHeight)
                    if (uris.size >= 4) drawImage(context, pdDocument, contentStream, uris[3], marginLeft + imgWidth + spacingInPoints, pageHeight - marginTop - (imgHeight * 2) - spacingInPoints, imgWidth, imgHeight, ImageAlignment.CENTER, borderSettings, imageEffectSettings, pageHeight)
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
    height: Float,
    alignment: ImageAlignment,
    borderSettings: ImageBorderSettings,
    imageEffectSettings: Map<String, ImageEffectSettings>,
    pageHeight: Float
) {
    try {
        val uri = Uri.parse(uriString)
        val tmpFile = File.createTempFile("pdf_img_inner", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tmpFile.outputStream().use { out -> input.copyTo(out) }
        }

        var bitmap = BitmapFactory.decodeFile(tmpFile.absolutePath)
        imageEffectSettings[uriString]?.let {
            bitmap = applyAllEffects(bitmap, it)
        }

        // En PDFBox, x, y son coordenadas de abajo hacia arriba.
        // Pero getFinalBitmapRect espera coordenadas de arriba hacia abajo para su lógica.
        // Sin embargo, como solo calcula proporciones dentro de un rectángulo, da igual
        // siempre que seamos consistentes.
        val cellRect = RectF(x, pageHeight - y - height, x + width, pageHeight - y)
        val finalRectTopDown = getFinalBitmapRect(bitmap, cellRect, alignment)

        val clippingApplied = borderSettings.style != ImageBorderStyle.NONE
        if (clippingApplied) {
            applyPdfBoxClippingPath(contentStream, borderSettings, finalRectTopDown, pageHeight)
        }

        val image = PDImageXObject.createFromFileByContent(tmpFile, pdDocument)
        contentStream.drawImage(image, finalRectTopDown.left, pageHeight - finalRectTopDown.bottom, finalRectTopDown.width(), finalRectTopDown.height())

        if (clippingApplied) {
            contentStream.restoreGraphicsState()
        }

        tmpFile.delete()
        bitmap.recycle()
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
