package com.example.dynamiccollage.utils

import android.content.Context
import com.example.dynamiccollage.data.model.GeneratedPage
import com.example.dynamiccollage.data.model.PageOrientation

object PdfContentManager {

    fun groupImagesForPdf(
        context: Context,
        allImageUris: List<String>,
        photosPerPage: Int
    ): List<GeneratedPage> {
        if (allImageUris.isEmpty()) {
            return emptyList()
        }

        // Special case: if user wants 2 per page but selects only 1 photo, treat as 1 per page.
        // Also, if they want 2 but there aren't enough photos for even one pair, treat as 1 per page.
        val effectivePhotosPerPage = if (photosPerPage == 2 && allImageUris.size >= 2) 2 else 1

        if (effectivePhotosPerPage == 1) {
            return processSinglePhotosPerPage(context, allImageUris)
        } else {
            return processDoublePhotosPerPage(context, allImageUris)
        }
    }

    private fun processSinglePhotosPerPage(
        context: Context,
        imageUris: List<String>
    ): List<GeneratedPage> {
        return imageUris.map { uri ->
            val photoOrientation = ImageUtils.getImageOrientation(context, uri)
            GeneratedPage(
                imageUris = listOf(uri),
                orientation = photoOrientation // Page orientation matches photo orientation
            )
        }
    }

    private fun processDoublePhotosPerPage(
        context: Context,
        imageUris: List<String>
    ): List<GeneratedPage> {
        val pages = mutableListOf<GeneratedPage>()

        // 1. Determine majority orientation of the set
        val orientations = imageUris.map { ImageUtils.getImageOrientation(context, it) }
        val verticalCount = orientations.count { it == PageOrientation.Vertical }
        val horizontalCount = orientations.count { it == PageOrientation.Horizontal }

        val pageOrientationForDoubles = if (verticalCount > horizontalCount) {
            PageOrientation.Horizontal // Majority vertical photos -> Horizontal pages
        } else {
            PageOrientation.Vertical // Majority horizontal or equal -> Vertical pages
        }

        // 2. Group photos into pairs
        val chunks = imageUris.chunked(2)
        chunks.forEach { chunk ->
            if (chunk.size == 2) {
                // This is a pair, add it to a page with the calculated orientation
                pages.add(
                    GeneratedPage(
                        imageUris = chunk,
                        orientation = pageOrientationForDoubles
                    )
                )
            } else {
                // This is the leftover photo
                val leftoverUri = chunk.first()
                val leftoverOrientation = ImageUtils.getImageOrientation(context, leftoverUri)
                pages.add(
                    GeneratedPage(
                        imageUris = listOf(leftoverUri),
                        orientation = leftoverOrientation // Leftover page matches photo orientation
                    )
                )
            }
        }
        return pages
    }
}
