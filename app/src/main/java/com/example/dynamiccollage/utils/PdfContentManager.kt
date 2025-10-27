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

        group.imageUris.forEach { uri ->
            when (ImageUtils.getImageOrientation(context, uri)) {
                PageOrientation.Vertical -> verticalPhotos.add(uri)
                PageOrientation.Horizontal -> horizontalPhotos.add(uri)
            }
        }

        var isFirstPageOfGroup = true

        val verticalChunks = verticalPhotos.chunked(group.photosPerSheet)
        verticalChunks.forEach { chunk ->
            val orientation = if (chunk.size == 2) PageOrientation.Horizontal else PageOrientation.Vertical
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = orientation,
                    groupId = group.id,
                    optionalTextStyle = if (isFirstPageOfGroup) group.optionalTextStyle else null,
                    isFirstPageOfGroup = isFirstPageOfGroup
                )
            )
            isFirstPageOfGroup = false // The flag is turned off after the first page is created
        }

        val horizontalChunks = horizontalPhotos.chunked(group.photosPerSheet)
        horizontalChunks.forEach { chunk ->
            val orientation = if (chunk.size == 2) PageOrientation.Vertical else PageOrientation.Horizontal
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = orientation,
                    groupId = group.id,
                    optionalTextStyle = if (isFirstPageOfGroup) group.optionalTextStyle else null,
                    isFirstPageOfGroup = isFirstPageOfGroup
                )
            )
            isFirstPageOfGroup = false // The flag is turned off after the first page is created
        }

        return smartPages
    }

    private fun processManualGroup(
        group: PageGroup
    ): List<GeneratedPage> {
        val manualPages = mutableListOf<GeneratedPage>()
        if (group.imageUris.isEmpty()) return manualPages

        val imageChunks = group.imageUris.chunked(group.photosPerSheet)

        imageChunks.forEachIndexed { index, chunk ->
            manualPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = group.orientation,
                    groupId = group.id,
                    optionalTextStyle = if (index == 0) group.optionalTextStyle else null,
                    isFirstPageOfGroup = index == 0
                )
            )
        }
        return manualPages
    }
}
