package com.example.dynamiccollage.utils

import android.content.Context
import com.example.dynamiccollage.data.model.GeneratedPage
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation

object PdfContentManager {

    fun groupImagesForPdf(
        context: Context,
        pageGroups: List<PageGroup>
    ): List<GeneratedPage> {
        val generatedPages = mutableListOf<GeneratedPage>()

        pageGroups.forEach { group ->
            if (group.smartLayoutEnabled) {
                generatedPages.addAll(processSmartGroup(context, group))
            } else {
                generatedPages.addAll(processManualGroup(group))
            }
        }

        return generatedPages
    }

    private fun processSmartGroup(
        context: Context,
        group: PageGroup
    ): List<GeneratedPage> {
        val smartPages = mutableListOf<GeneratedPage>()
        if (group.imageUris.isEmpty()) return smartPages

        val verticalPhotos = mutableListOf<String>()
        val horizontalPhotos = mutableListOf<String>()

        // Classify photos by their orientation
        group.imageUris.forEach { uri ->
            when (ImageUtils.getImageOrientation(context, uri)) {
                PageOrientation.Vertical -> verticalPhotos.add(uri)
                PageOrientation.Horizontal -> horizontalPhotos.add(uri)
            }
        }

        // Create pages for vertical photos
        val verticalChunks = verticalPhotos.chunked(group.photosPerSheet)
        verticalChunks.forEach { chunk ->
            val orientation = if (chunk.size == 2) {
                PageOrientation.Horizontal // Pair of vertical photos on a horizontal page
            } else {
                PageOrientation.Vertical // Single vertical photo on a vertical page
            }
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = orientation
                )
            )
        }

        // Create pages for horizontal photos
        val horizontalChunks = horizontalPhotos.chunked(group.photosPerSheet)
        horizontalChunks.forEach { chunk ->
            val orientation = if (chunk.size == 2) {
                PageOrientation.Vertical // Pair of horizontal photos on a vertical page
            } else {
                PageOrientation.Horizontal // Single horizontal photo on a horizontal page
            }
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = orientation
                )
            )
        }

        return smartPages
    }

    private fun processManualGroup(
        group: PageGroup
    ): List<GeneratedPage> {
        val manualPages = mutableListOf<GeneratedPage>()
        if (group.imageUris.isEmpty()) return manualPages

        val imageChunks = group.imageUris.chunked(group.photosPerSheet)

        imageChunks.forEach { chunk ->
            manualPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = group.orientation
                )
            )
        }
        return manualPages
    }
}
