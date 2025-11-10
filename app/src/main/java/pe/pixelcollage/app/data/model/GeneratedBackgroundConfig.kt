package pe.pixelcollage.app.data.model

enum class BackgroundPatternType(val displayName: String) {
    SÓLIDO_BLANCO("Sólido Blanco"),
    LOW_POLY("Polígono Bajo"),
    CRISTALES("Cristales"),
    GEOMETRICO("Geométrico"),
    PAPELES_SUPERPUESTOS("Papeles Superpuestos"),
    ONDAS_ABSTRACTAS("Ondas Abstractas"),
    BOKEH_DORADO("Bokeh Dorado")
}

data class GeneratedBackgroundConfig(
    val patternType: BackgroundPatternType = BackgroundPatternType.SÓLIDO_BLANCO,
    // Las propiedades opacity, size y density ya no son necesarias aquí,
    // se pueden manejar directamente en el generador si se desea.
    // Por ahora, las mantenemos simples para la selección.
    val opacity: Float = 0.5f,
    val size: Float = 10f,
    val density: Float = 0.5f
)
