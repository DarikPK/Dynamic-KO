package com.example.dynamiccollage.data.model

import android.graphics.RectF
import java.io.File

/**
 * Represents the position of a single photo on a specific page of the generated PDF.
 *
 * @param uri The URI of the photo.
 * @param pageIndex The 0-based index of the page where the photo is located.
 * @param rect The bounding box of the photo on the page, in PDF points.
 */
data class PhotoRect(val uri: String, val pageIndex: Int, val rect: RectF)

/**
 * Represents the result of a PDF generation operation.
 *
 * @param file The generated PDF file.
 * @param photoLayouts A list of all photos and their locations across all pages of the PDF.
 */
data class PdfGenerationResult(val file: File, val photoLayouts: List<PhotoRect>)
