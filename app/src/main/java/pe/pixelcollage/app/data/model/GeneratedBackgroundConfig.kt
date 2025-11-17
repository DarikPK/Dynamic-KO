package pe.pixelcollage.app.data.model

import androidx.compose.ui.graphics.Color

enum class BackgroundPatternType(val displayName: String) {
    SÓLIDO("Sólido"),
    LOW_POLY("Polígono Bajo"),
    CRISTALES("Cristales"),
    GEOMETRICO("Geométrico"),
    PAPELES_SUPERPUESTOS("Papeles Superpuestos"),
    ONDAS_ABSTRACTAS("Ondas Abstractas"),
    BOKEH_DORADO("Bokeh Dorado"),
    ACUARELA("Lavado de Acuarela")
}

data class GeneratedBackgroundConfig(
    val patternType: BackgroundPatternType = BackgroundPatternType.SÓLIDO,
    val transparency: Float = 5f,
    val size: Float = 5f,
    val density: Float = 5f,
    val colorThemeName: String = "SkyBlue",
    val isRandom: Boolean = false,
    val solidColor: Color = Color.White,
    val combineWithSolidColor: Boolean = false
)
