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

        // Determine page orientation based on photo orientation and photos per sheet
        val verticalPhotoPageOrientation = if (group.photosPerSheet == 2) PageOrientation.Horizontal else PageOrientation.Vertical
        val horizontalPhotoPageOrientation = if (group.photosPerSheet == 2) PageOrientation.Vertical else PageOrientation.Horizontal

        // Create pages for vertical photos
        val verticalChunks = verticalPhotos.chunked(group.photosPerSheet)
        verticalChunks.forEach { chunk ->
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = verticalPhotoPageOrientation
                )
            )
        }

        // Create pages for horizontal photos
        val horizontalChunks = horizontalPhotos.chunked(group.photosPerSheet)
        horizontalChunks.forEach { chunk ->
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = horizontalPhotoPageOrientation
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
