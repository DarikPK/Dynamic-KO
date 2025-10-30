package pe.pixelcollage.app.data.model

data class GeneratedPage(
    val imageUris: List<String>,
    val orientation: PageOrientation,
    val imagesPerPage: Int,
    val imageSpacing: Float,
    val groupId: String, // To look up border settings
    val optionalTextStyle: TextStyleConfig? = null,
    val isFirstPageOfGroup: Boolean = false
)
