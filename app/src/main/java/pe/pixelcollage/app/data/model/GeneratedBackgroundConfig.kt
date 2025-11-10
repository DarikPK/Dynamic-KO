package pe.pixelcollage.app.data.model

enum class BackgroundPatternType(val displayName: String) {
    NONE("Ninguno"),
    // Original Patterns
    CURVAS("Curvas"),
    FIGURAS_GEOMETRICAS("Figuras Geométricas"),
    LINEAS("Líneas"),
    PUNTOS("Puntos"),
    ESPIRALES("Espirales"),
    ONDAS("Ondas"),
    ONDAS_ALEATORIAS("Ondas Aleatorias"),
    BURBUJAS("Burbujas"),
    ESTRELLAS("Estrellas"),
    MOSAICO("Mosaico"),
    // New Patterns from Images
    LOW_POLY("Poligonal (Low Poly)"),
    CRYSTALS("Cristales Abstractos"),
    SOFT_WAVES("Ondas Suaves"),
    BOKEH("Luces Bokeh"),
    HEXAGONS("Hexágonos Translúcidos"),
    PAPER_LAYERS("Capas de Papel")
}

data class GeneratedBackgroundConfig(
    val patternType: BackgroundPatternType = BackgroundPatternType.NONE,
    val opacity: Float = 0.5f,
    val size: Float = 10f,
    val density: Float = 0.5f,
    val enabled: Boolean = false,
    val color: Int? = null
)
