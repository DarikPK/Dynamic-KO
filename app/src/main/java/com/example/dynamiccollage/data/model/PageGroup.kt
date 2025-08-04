package com.example.dynamiccollage.data.model

import java.util.UUID

data class PageGroup(
    val id: String = UUID.randomUUID().toString(),
    var groupName: String = "Nuevo Grupo",
    var sheetCount: Int = 1,
    var photosPerSheet: Pair<Int, Int> = Pair(2, 2), // (Columnas, Filas)
    var imageUris: List<String> = emptyList(),
    var orientation: PageOrientation = PageOrientation.Vertical,
    var optionalTextStyle: TextStyleConfig = TextStyleConfig(),
    val imageSpacing: Float = 2f
)
