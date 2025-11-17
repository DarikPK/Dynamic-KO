package pe.pixelcollage.app.data.model

import androidx.compose.ui.graphics.Color

enum class BackgroundPatternType(val displayName: String) {
    SÓLIDO("Sólido"),
    LOW_POLY("Polígono Bajo"),
    CRISTALES("Cristales"),
    GEOMETRICO("Geométrico"),
    PAPELES_SUPERPUESTOS("Papeles Superpuestos"),
    ONDAS_ABSTRACTAS("Ondas Abstractas"),
    BOKEH_DORADO("Bokeh Dorado")
}

data class GeneratedBackgroundConfig(
    val patternType: BackgroundPatternType = BackgroundPatternType.SÓLIDO,
    val opacity: Float = 0.5f,
    val size: Float = 10f,
    val density: Float = 0.5f,
    val solidColor: Color = Color.White,
    val combineWithSolidColor: Boolean = false
)
