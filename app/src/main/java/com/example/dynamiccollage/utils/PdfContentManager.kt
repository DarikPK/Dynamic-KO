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

        // 1. Clasificar imágenes por orientación
        val verticalUris = mutableListOf<String>()
        val horizontalUris = mutableListOf<String>()

        allImageUris.forEach { uri ->
            when (ImageUtils.getImageOrientation(context, uri)) {
                PageOrientation.Vertical -> verticalUris.add(uri)
                PageOrientation.Horizontal -> horizontalUris.add(uri)
            }
        }

        val pages = mutableListOf<GeneratedPage>()
        // Asegurarse de que el valor sea 1 o 2. Si no, por defecto es 1.
        val effectivePhotosPerPage = if (photosPerPage == 2) 2 else 1

        // 2. Agrupar fotos verticales
        verticalUris.chunked(effectivePhotosPerPage).forEach { chunk ->
            pages.add(GeneratedPage(imageUris = chunk, orientation = PageOrientation.Vertical))
        }

        // 3. Agrupar fotos horizontales
        horizontalUris.chunked(effectivePhotosPerPage).forEach { chunk ->
            pages.add(GeneratedPage(imageUris = chunk, orientation = PageOrientation.Horizontal))
        }

        return pages
    }
}
