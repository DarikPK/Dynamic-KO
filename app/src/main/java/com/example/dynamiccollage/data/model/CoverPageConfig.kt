package com.example.dynamiccollage.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
// Quitar Dp y dp si los márgenes son Float
// import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.dynamiccollage.ui.theme.calibriFontFamily

// Valores por defecto para la configuración de la portada
object DefaultCoverConfig {
    // IDs ya no son necesarios si TextStyleConfig no tiene 'id' y el ViewModel maneja estilos por separado
    const val CLIENT_NAME_ID = "clientName"
    const val RUC_ID = "ruc"
    const val SUBTITLE_ID = "subtitle"

    val BORDER_COLOR: Color = Color.Black
    const val BORDER_VISIBLE_TOP: Boolean = true
    const val BORDER_VISIBLE_BOTTOM: Boolean = true
    const val BORDER_VISIBLE_LEFT: Boolean = true
    const val BORDER_VISIBLE_RIGHT: Boolean = true

    // Márgenes como Float (cm)
    const val MARGIN_TOP_CM: Float = 0.5f
    const val MARGIN_BOTTOM_CM: Float = 0.5f
    const val MARGIN_LEFT_CM: Float = 0.5f
    const val MARGIN_RIGHT_CM: Float = 0.5f

    val PAGE_ORIENTATION: PageOrientation = PageOrientation.Vertical

    // Función para obtener una instancia por defecto, si ProjectViewModel la necesita
    fun get(): CoverPageConfig = CoverPageConfig()
}

data class CoverPageConfig(
    // Asumiendo que TextStyleConfig no tiene 'id' y fontSize es Int
    val clientNameStyle: TextStyleConfig = TextStyleConfig(fontFamily = calibriFontFamily, fontSize = 18.sp),
    val rucStyle: TextStyleConfig = TextStyleConfig(fontFamily = calibriFontFamily, fontSize = 18.sp),
    val subtitleStyle: TextStyleConfig = TextStyleConfig(fontFamily = calibriFontFamily, fontSize = 10.sp, textAlign = TextAlign.End, fontStyle = FontStyle.Italic),
    val showAddressPrefix: Boolean = false, // Nuevo campo para el prefijo de dirección

    val mainImageUri: String? = null,

    val borderColor: Color = DefaultCoverConfig.BORDER_COLOR,
    val borderVisibleTop: Boolean = DefaultCoverConfig.BORDER_VISIBLE_TOP,
    val borderVisibleBottom: Boolean = DefaultCoverConfig.BORDER_VISIBLE_BOTTOM,
    val borderVisibleLeft: Boolean = DefaultCoverConfig.BORDER_VISIBLE_LEFT,
    val borderVisibleRight: Boolean = DefaultCoverConfig.BORDER_VISIBLE_RIGHT,

    // Márgenes como Float (cm)
    val marginTop: Float = DefaultCoverConfig.MARGIN_TOP_CM,
    val marginBottom: Float = DefaultCoverConfig.MARGIN_BOTTOM_CM,
    val marginLeft: Float = DefaultCoverConfig.MARGIN_LEFT_CM,
    val marginRight: Float = DefaultCoverConfig.MARGIN_RIGHT_CM,

    val pageOrientation: PageOrientation = DefaultCoverConfig.PAGE_ORIENTATION, // Nuevo campo
    val allCaps: Boolean = false, // Nuevo campo para texto en mayúsculas

    val spacingRucAddress: Float = 1.0f, // Espaciado en cm
    val spacingAddressImage: Float = 1.0f, // Espaciado en cm

    val templateName: String? = null
)
