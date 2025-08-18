package com.example.dynamiccollage.utils

import android.content.Context
import com.example.dynamiccollage.data.model.GeneratedPage
import com.example.dynamiccollage.data.model.PageOrientation

object PdfContentManager {

    fun groupImagesForPdf(
        context: Context,
        imageUris: List<String>,
        photosPerPage: Int
    ): List<GeneratedPage> {
        if (imageUris.isEmpty()) return emptyList()

        // Case 1: One photo per page
        if (photosPerPage == 1) {
            return imageUris.map { uri ->
                GeneratedPage(
                    imageUris = listOf(uri),
                    orientation = ImageUtils.getImageOrientation(context, uri)
                )
            }
        }

        // Case 2: Two photos per page (Smart Layout)
        val generatedPages = mutableListOf<GeneratedPage>()

        // Separate photos by orientation
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
