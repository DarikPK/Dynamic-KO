package com.example.dynamiccollage.utils

import android.content.Context
import com.example.dynamiccollage.data.model.GeneratedPage
import com.example.dynamiccollage.data.model.PageOrientation

object PdfContentManager {

    fun groupImagesForPdf(
        context: Context,
        imageUris: List<String>,
        photosPerPage: Int,
        smartLayoutEnabled: Boolean,
        groupOrientation: PageOrientation
    ): List<GeneratedPage> {
        if (imageUris.isEmpty()) return emptyList()

        if (smartLayoutEnabled && photosPerPage == 2) {
            return runSmartLayout(context, imageUris)
        } else {
            return runSimpleLayout(imageUris, photosPerPage, groupOrientation)
        }
    }

    private fun runSimpleLayout(
        imageUris: List<String>,
        photosPerPage: Int,
        groupOrientation: PageOrientation
    ): List<GeneratedPage> {
        val effectivePhotosPerPage = if (photosPerPage in 1..2) photosPerPage else 1
        return imageUris.chunked(effectivePhotosPerPage).map { chunk ->
            GeneratedPage(
                imageUris = chunk,
                orientation = groupOrientation
            )
        }
    }

    private fun runSmartLayout(
        context: Context,
        imageUris: List<String>
    ): List<GeneratedPage> {
        val generatedPages = mutableListOf<GeneratedPage>()
        val photosByOrientation = imageUris.groupBy {
            ImageUtils.getImageOrientation(context, it)
        }
        val verticalPhotos = photosByOrientation[PageOrientation.Vertical] ?: emptyList()
        val horizontalPhotos = photosByOrientation[PageOrientation.Horizontal] ?: emptyList()

        // Process pairs of vertical photos -> create horizontal pages
        verticalPhotos.chunked(2).forEach { chunk ->
            if (chunk.size == 2) {
                generatedPages.add(GeneratedPage(imageUris = chunk, orientation = PageOrientation.Horizontal))
            } else { // Leftover single vertical photo
                generatedPages.add(GeneratedPage(imageUris = chunk, orientation = PageOrientation.Vertical))
            }
        }

        // Process pairs of horizontal photos -> create vertical pages
        horizontalPhotos.chunked(2).forEach { chunk ->
            if (chunk.size == 2) {
                generatedPages.add(GeneratedPage(imageUris = chunk, orientation = PageOrientation.Vertical))
            } else { // Leftover single horizontal photo
                generatedPages.add(GeneratedPage(imageUris = chunk, orientation = PageOrientation.Horizontal))
            }
        }
        return generatedPages
    }
}
