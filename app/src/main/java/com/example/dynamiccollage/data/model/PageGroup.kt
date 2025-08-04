package com.example.dynamiccollage.data.model

import java.util.UUID

// Valores por defecto para PageGroup
object DefaultPageGroupConfig {
    val PAGE_ORIENTATION = PageOrientation.Vertical
    const val PHOTOS_PER_SHEET = 1
    const val DEFAULT_SHEET_COUNT = 1
    const val OPTIONAL_TEXT_ID = "pageGroupOptionalText"
}

data class PageGroup(
    val id: String = UUID.randomUUID().toString(),
    val groupName: String = "", // Podría ser útil para el usuario identificar grupos
    val orientation: PageOrientation = DefaultPageGroupConfig.PAGE_ORIENTATION,
    val photosPerSheet: Int = DefaultPageGroupConfig.PHOTOS_PER_SHEET, // 1 o 2
    val sheetCount: Int = DefaultPageGroupConfig.DEFAULT_SHEET_COUNT, // Número de hojas en este grupo
    val optionalTextStyle: TextStyleConfig = TextStyleConfig(id = DefaultPageGroupConfig.OPTIONAL_TEXT_ID),
    val imageUris: List<String> = emptyList() // Lista de URIs de las imágenes seleccionadas para este grupo
) {
    val totalPhotosRequired: Int
        get() = photosPerSheet * sheetCount

    val isPhotoQuotaMet: Boolean
        get() = imageUris.size == totalPhotosRequired

    // Determina la disposición de la tabla basada en la orientación y fotos por hoja
    // Retorna Pair<columnas, filas> para la tabla en una hoja
    val tableLayout: Pair<Int, Int>
        get() = when (orientation) {
            PageOrientation.Vertical -> {
                if (photosPerSheet == 1) Pair(1, 1) // 1x1
                else Pair(1, 2) // 1 columna, 2 filas
            }
            PageOrientation.Horizontal -> {
                if (photosPerSheet == 1) Pair(1, 1) // 1x1
                else Pair(2, 1) // 2 columnas, 1 fila
            }
        }
}
