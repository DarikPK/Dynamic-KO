package com.example.dynamiccollage.data.model

enum class ImageBorderStyle {
    NONE,
    CURVED,
    CHAMFERED // Also known as "triangular" corners
}

data class ImageBorderSettings(
    val style: ImageBorderStyle = ImageBorderStyle.NONE,
    val size: Float = 10f // Represents radius for curved, or cut size for chamfered
)
