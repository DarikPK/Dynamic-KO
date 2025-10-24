package com.example.dynamiccollage.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.GeneratedPage
import com.example.dynamiccollage.data.model.ImageEffectSettings
import com.example.dynamiccollage.data.model.PageOrientation
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.FileOutputStream

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
            // Esta función necesitará ser adaptada o copiada a este archivo.
            // Por ahora, asumimos que existe una función similar a la de PdfGenerator.
            // drawCoverPage(pdfDocument, context, coverConfig, quality, imageEffectSettings)
        }
        // drawInnerPages(pdfDocument, context, generatedPages, coverConfig, if (shouldDrawCover) 2 else 1, quality, imageEffectSettings)

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
        Log.e("HybridPdf", "Error al generar PDF Híbrido", e)
        pdfDocument.close()
        return null
    } finally {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}
