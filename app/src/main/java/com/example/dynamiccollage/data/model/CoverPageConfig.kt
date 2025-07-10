package com.example.dynamiccollage.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Valores por defecto para la configuración de la portada
object DefaultCoverConfig {
    const val CLIENT_NAME_ID = "clientName"
    const val RUC_ID = "ruc"
    const val SUBTITLE_ID = "subtitle" // Dirección

    val BORDER_COLOR: Color = Color.Black
    const val BORDER_VISIBLE_TOP: Boolean = true
    const val BORDER_VISIBLE_BOTTOM: Boolean = true
    const val BORDER_VISIBLE_LEFT: Boolean = true
    const val BORDER_VISIBLE_RIGHT: Boolean = true

    val MARGIN_TOP: Dp = 25.4.dp // 2.54 cm
    val MARGIN_BOTTOM: Dp = 25.4.dp // 2.54 cm
    val MARGIN_LEFT: Dp = 31.8.dp // 3.18 cm
    val MARGIN_RIGHT: Dp = 31.8.dp // 3.18 cm
}

data class CoverPageConfig(
    val clientNameStyle: TextStyleConfig = TextStyleConfig(id = DefaultCoverConfig.CLIENT_NAME_ID),
    val rucStyle: TextStyleConfig = TextStyleConfig(id = DefaultCoverConfig.RUC_ID),
    val subtitleStyle: TextStyleConfig = TextStyleConfig(id = DefaultCoverConfig.SUBTITLE_ID), // Dirección

    val mainImageUri: String? = null, // URI de la imagen principal como String

    val borderColor: Color = DefaultCoverConfig.BORDER_COLOR,
    val borderVisibleTop: Boolean = DefaultCoverConfig.BORDER_VISIBLE_TOP,
    val borderVisibleBottom: Boolean = DefaultCoverConfig.BORDER_VISIBLE_BOTTOM,
    val borderVisibleLeft: Boolean = DefaultCoverConfig.BORDER_VISIBLE_LEFT,
    val borderVisibleRight: Boolean = DefaultCoverConfig.BORDER_VISIBLE_RIGHT,

    // Márgenes en Dp para la UI, se convertirán para el PDF
    val marginTop: Dp = DefaultCoverConfig.MARGIN_TOP,
    val marginBottom: Dp = DefaultCoverConfig.MARGIN_BOTTOM,
    val marginLeft: Dp = DefaultCoverConfig.MARGIN_LEFT,
    val marginRight: Dp = DefaultCoverConfig.MARGIN_RIGHT,

    val templateName: String? = null // Para guardar/cargar plantillas
)
