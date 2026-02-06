package pe.pixelcollage.app.data.model

enum class ImageBorderStyle {
    NONE,
    CURVED,
    CHAMFERED // Also known as "triangular" corners
}

data class ImageBorderSettings(
    val style: ImageBorderStyle = ImageBorderStyle.CURVED,
    val size: Float = 15f // Represents radius for curved, or cut size for chamfered
)
