package pe.pixelcollage.app.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import pe.pixelcollage.app.ui.theme.calibriFontFamily

enum class DocumentType {
    RUC, DNI, NONE
}

// Valores por defecto para la configuración de la portada
object DefaultCoverConfig {
    const val CLIENT_NAME_ID = "clientName"
    const val RUC_ID = "ruc"
    const val SUBTITLE_ID = "subtitle"

    // Márgenes como Float (cm)
    const val MARGIN_TOP_CM: Float = 0.5f
    const val MARGIN_BOTTOM_CM: Float = 0.5f
    const val MARGIN_LEFT_CM: Float = 0.5f
    const val MARGIN_RIGHT_CM: Float = 0.5f

    val PAGE_ORIENTATION: PageOrientation = PageOrientation.Vertical

    fun get(): CoverPageConfig = CoverPageConfig()
}

data class CoverPageConfig(
    val clientNameStyle: TextStyleConfig = TextStyleConfig(
        content = "",
        fontFamily = calibriFontFamily,
        fontSize = 18,
        fontWeight = FontWeight.Normal,
        fontColor = DefaultTextConfig.FONT_COLOR,
        rowStyle = RowStyle(
            border = BorderProperties(
                color = Color(0xFF50C878),
                top = true,
                bottom = false
            )
        )
    ),
    val showClientPrefix: Boolean = true,
    val documentType: DocumentType = DocumentType.RUC,
    val rucStyle: TextStyleConfig = TextStyleConfig(
        content = "",
        fontFamily = calibriFontFamily,
        fontSize = 18,
        fontWeight = FontWeight.Normal,
        fontColor = DefaultTextConfig.FONT_COLOR,
        rowStyle = RowStyle(
            backgroundColor = Color(0xFFD4EEE6),
            border = BorderProperties(
                color = Color(0xFF50C878),
                top = true,
                bottom = true
            )
        )
    ),
    val subtitleStyle: TextStyleConfig = TextStyleConfig(
        content = "",
        fontFamily = calibriFontFamily,
        fontSize = 10,
        textAlign = TextAlign.End,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontColor = DefaultTextConfig.FONT_COLOR,
        rowStyle = RowStyle(
            padding = PaddingValues(bottom = 2f),
            border = BorderProperties(
                color = Color(0xFF50C878),
                bottom = true
            )
        )
    ),
    val showAddressPrefix: Boolean = true,
    val allCaps: Boolean = true,

    val mainImageUri: String? = null,

    // Márgenes como Float (cm)
    val marginTop: Float = DefaultCoverConfig.MARGIN_TOP_CM,
    val marginBottom: Float = DefaultCoverConfig.MARGIN_BOTTOM_CM,
    val marginLeft: Float = DefaultCoverConfig.MARGIN_LEFT_CM,
    val marginRight: Float = DefaultCoverConfig.MARGIN_RIGHT_CM,

    val pageOrientation: PageOrientation = DefaultCoverConfig.PAGE_ORIENTATION,

    // Pesos para el diseño de la portada
    val clientWeight: Float = 0.5f,
    val rucWeight: Float = 0.5f,
    val addressWeight: Float = 0.3f,
    val separationWeight: Float = 0.2f,
    val photoWeight: Float = 10f,

    val photoStyle: RowStyle = RowStyle(),

    // PDF Size Management
    val quality: Int = 90,
    val forceFullResCover: Boolean = false,
    val useHybridPdfMode: Boolean = true,

    // Advanced Design
    val imageBorderSettingsMap: Map<String, ImageBorderSettings> = emptyMap(),
    val hybridCoverImageQuality: Int = 70,
    val hybridInnerImagesQuality: Int = 30,

    val templateName: String? = null,
    val generatedBackgroundConfig: GeneratedBackgroundConfig? = GeneratedBackgroundConfig()
)
