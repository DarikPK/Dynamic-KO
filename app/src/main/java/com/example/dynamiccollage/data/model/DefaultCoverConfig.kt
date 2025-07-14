package com.example.dynamiccollage.data.model

import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dynamiccollage.data.model.PageOrientation // Asegúrate de importar esto


data class CoverPageConfig(
    val pageOrientation: PageOrientation = PageOrientation.Vertical,
    val clientNameStyle: TextStyleConfig = TextStyleConfig(),
    val rucStyle: TextStyleConfig = TextStyleConfig(),
    val subtitleStyle: TextStyleConfig = TextStyleConfig(),
    val mainImageUri: Uri? = null,
    val borderColor: Color = Color.Black,
    val borderVisibleTop: Boolean = true,
    val borderVisibleBottom: Boolean = true,
    val borderVisibleLeft: Boolean = true,
    val borderVisibleRight: Boolean = true,
    val marginTop: Float = 2.54f,
    val marginBottom: Float = 2.54f,
    val marginLeft: Float = 3.18f,
    val marginRight: Float = 3.18f
)

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

    fun default(): CoverPageConfig {
        return CoverPageConfig()
    }
    fun get(): CoverPageConfig {
        return CoverPageConfig(
            // valores por defecto
        )
    }
}
