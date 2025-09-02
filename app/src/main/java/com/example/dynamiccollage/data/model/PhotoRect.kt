package com.example.dynamiccollage.data.model

import android.graphics.RectF

/**
 * Represents the position of a single photo on a specific page of the generated PDF.
 *
 * @param uri The URI of the photo.
 * @param pageIndex The 0-based index of the page where the photo is located.
 * @param rect The bounding box of the photo on the page, in PDF points.
 */
data class PhotoRect(
    val uri: String,
    val pageIndex: Int,
    val rect: RectF
)
