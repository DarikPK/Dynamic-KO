package com.example.dynamiccollage.data.model

import java.util.UUID

data class PageGroup(
    val id: String = UUID.randomUUID().toString(),
    var groupName: String = "Nuevo Grupo",
    var sheetCount: Int = 1,
    var photosPerSheet: Int = 1,
    var imageUris: List<String> = emptyList(),
    var orientation: PageOrientation = PageOrientation.Vertical,
    var optionalTextStyle: TextStyleConfig = TextStyleConfig(),
    val imageSpacing: Float = 4f
) {
    val totalPhotosRequired: Int
        get() = photosPerSheet * sheetCount

    val isPhotoQuotaMet: Boolean
        get() = imageUris.size == totalPhotosRequired
}
