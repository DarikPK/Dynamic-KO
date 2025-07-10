package com.example.dynamiccollage.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Valores por defecto para la configuración de texto
object DefaultTextConfig {
    val FONT_FAMILY: FontFamily = FontFamily.Default
    val FONT_SIZE: TextUnit = 16.sp
    val TEXT_ALIGN: TextAlign = TextAlign.Center
    val FONT_COLOR: Color = Color.Black // Considerar usar colores del tema más adelante
}

data class TextStyleConfig(
    val id: String = "default", // Para identificar el campo de texto (cliente, ruc, direccion)
    val fontFamily: FontFamily = DefaultTextConfig.FONT_FAMILY,
    val fontSize: TextUnit = DefaultTextConfig.FONT_SIZE,
    val textAlign: TextAlign = DefaultTextConfig.TEXT_ALIGN,
    val fontColor: Color = DefaultTextConfig.FONT_COLOR, // Podría ser útil para PDFs, aunque la UI usará el tema
    val content: String = "" // El texto en sí
) {
    val isVisible: Boolean
        get() = content.isNotBlank()
}
