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

        val verticalChunks = verticalPhotos.chunked(group.photosPerSheet)
        verticalChunks.forEachIndexed { index, chunk ->
            val orientation = if (chunk.size == 2) PageOrientation.Horizontal else PageOrientation.Vertical
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = orientation,
                    groupId = group.id,
                    optionalTextStyle = if (index == 0) group.optionalTextStyle else null,
                    isFirstPageOfGroup = index == 0
                )
            )
        }

        val horizontalChunks = horizontalPhotos.chunked(group.photosPerSheet)
        horizontalChunks.forEachIndexed { index, chunk ->
            val orientation = if (chunk.size == 2) PageOrientation.Vertical else PageOrientation.Horizontal
            // If there were no vertical photos, the first horizontal page is the first page of the group
            val isFirstPage = index == 0 && verticalChunks.isEmpty()
            smartPages.add(
                GeneratedPage(
                    imageUris = chunk,
                    orientation = orientation,
                    groupId = group.id,
                    optionalTextStyle = if (isFirstPage) group.optionalTextStyle else null,
                    isFirstPageOfGroup = isFirstPage
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
