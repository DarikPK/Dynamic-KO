package com.example.dynamiccollage.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PdfGenerator(
    private val context: Context,
    private val projectViewModel: ProjectViewModel
) {
    fun generatePdf(): File? {
        val pdfFile = File(context.cacheDir, "generated.pdf")
        val outputStream = FileOutputStream(pdfFile)
        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        try {
            // Add title
            document.add(Paragraph("My Collage").setBold().setFontSize(20f))

            // Add cover image
            val coverImageUri = projectViewModel.currentCoverConfig.value.mainImageUri
            if (coverImageUri != null) {
                val image = createImage(coverImageUri)
                if (image != null) {
                    document.add(image)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            document.close()
        }

        return pdfFile
    }

    private fun createImage(uri: Uri): Image? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageData = ImageDataFactory.create(stream.toByteArray())
            Image(imageData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
