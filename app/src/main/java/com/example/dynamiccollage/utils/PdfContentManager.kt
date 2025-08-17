package com.example.dynamiccollage.utils

import android.content.Context
import com.example.dynamiccollage.data.model.GeneratedPage
import com.example.dynamiccollage.data.model.PageOrientation

object PdfContentManager {

    fun groupImagesForPdf(
        imageUris: List<String>,
        photosPerPage: Int,
        groupOrientation: PageOrientation
    ): List<GeneratedPage> {
        if (imageUris.isEmpty()) {
            return emptyList()
        }

        val effectivePhotosPerPage = if (photosPerPage in 1..2) photosPerPage else 1

        val pages = mutableListOf<GeneratedPage>()
        val chunks = imageUris.chunked(effectivePhotosPerPage)

        chunks.forEach { chunk ->
            pages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = groupOrientation
                )
            )
        }
        return pages
    }
}
