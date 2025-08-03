package com.example.dynamiccollage.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Valores por defecto para la configuración de texto
object DefaultTextConfig {
    val FONT_FAMILY: FontFamily = FontFamily.Default // Esto puede quedarse o eliminarse si no se usa
    val FONT_SIZE: TextUnit = 16.sp
    val TEXT_ALIGN: TextAlign = TextAlign.Center
    val FONT_COLOR: Color = Color(0xFF2C74B5)
}

data class TextStyleConfig(
    val id: String = "default", // Eliminado 'id'
    val fontFamily: FontFamily = DefaultTextConfig.FONT_FAMILY, // Se puede mantener FontFamily.Default directamente
    val fontSize: TextUnit = DefaultTextConfig.FONT_SIZE,
    val textAlign: TextAlign = DefaultTextConfig.TEXT_ALIGN,
    val fontColor: Color = DefaultTextConfig.FONT_COLOR,
    val content: String = ""
) {
    val isVisible: Boolean
        get() = content.isNotBlank()
}
