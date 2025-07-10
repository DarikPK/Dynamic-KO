package com.example.dynamiccollage.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.DefaultCoverConfig
import com.example.dynamiccollage.data.model.TextStyleConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CoverSetupViewModel : ViewModel() {

    private val _coverConfig = MutableStateFlow(CoverPageConfig())
    val coverConfig: StateFlow<CoverPageConfig> = _coverConfig.asStateFlow()

    fun onClientNameChange(newName: String) {
        _coverConfig.update { currentState ->
            currentState.copy(
                clientNameStyle = currentState.clientNameStyle.copy(content = newName)
            )
        }
    }

    fun onRucChange(newRuc: String) {
        _coverConfig.update { currentState ->
            currentState.copy(
                rucStyle = currentState.rucStyle.copy(content = newRuc)
            )
        }
    }

    fun onAddressChange(newAddress: String) {
        _coverConfig.update { currentState ->
            currentState.copy(
                subtitleStyle = currentState.subtitleStyle.copy(content = newAddress)
            )
        }
    }

    fun onMainImageSelected(uri: Uri?) {
        _coverConfig.update { currentState ->
            currentState.copy(mainImageUri = uri?.toString())
        }
    }

    fun onTextStyleChange(
        fieldId: String,
        newSize: Float? = null, // Usamos Float para el slider, luego convertimos a sp
        newAlign: androidx.compose.ui.text.style.TextAlign? = null
        // newFontFamily: androidx.compose.ui.text.font.FontFamily? = null // Para futura implementación
    ) {
        _coverConfig.update { currentState ->
            val newClientStyle = if (fieldId == DefaultCoverConfig.CLIENT_NAME_ID) {
                currentState.clientNameStyle.copy(
                    fontSize = newSize?.toInt()?.coerceIn(8, 72)?.sp ?: currentState.clientNameStyle.fontSize,
                    textAlign = newAlign ?: currentState.clientNameStyle.textAlign
                )
            } else currentState.clientNameStyle

            val newRucStyle = if (fieldId == DefaultCoverConfig.RUC_ID) {
                currentState.rucStyle.copy(
                    fontSize = newSize?.toInt()?.coerceIn(8, 72)?.sp ?: currentState.rucStyle.fontSize,
                    textAlign = newAlign ?: currentState.rucStyle.textAlign
                )
            } else currentState.rucStyle

            val newSubtitleStyle = if (fieldId == DefaultCoverConfig.SUBTITLE_ID) {
                currentState.subtitleStyle.copy(
                    fontSize = newSize?.toInt()?.coerceIn(8, 72)?.sp ?: currentState.subtitleStyle.fontSize,
                    textAlign = newAlign ?: currentState.subtitleStyle.textAlign
                )
            } else currentState.subtitleStyle

            currentState.copy(
                clientNameStyle = newClientStyle,
                rucStyle = newRucStyle,
                subtitleStyle = newSubtitleStyle
            )
        }
    }

    fun onBorderColorChange(newColor: androidx.compose.ui.graphics.Color) {
        _coverConfig.update { it.copy(borderColor = newColor) }
    }

    fun onBorderVisibilityChange(
        top: Boolean? = null,
        bottom: Boolean? = null,
        left: Boolean? = null,
        right: Boolean? = null
    ) {
        _coverConfig.update { currentState ->
            currentState.copy(
                borderVisibleTop = top ?: currentState.borderVisibleTop,
                borderVisibleBottom = bottom ?: currentState.borderVisibleBottom,
                borderVisibleLeft = left ?: currentState.borderVisibleLeft,
                borderVisibleRight = right ?: currentState.borderVisibleRight
            )
        }
    }

    fun onMarginChange(
        top: String? = null,
        bottom: String? = null,
        left: String? = null,
        right: String? = null
    ) {
        _coverConfig.update { currentState ->
            // Convertir String a Dp, manteniendo el valor anterior si la conversión falla o el string es nulo/vacío
            // Se asume que el input es en cm y se convierte a Dp (1 cm ~ 37.8 dp, aproximado)
            // Para una conversión más precisa, se debería usar la densidad de pantalla,
            // pero para la UI, una conversión fija es suficiente. La conversión final se hará para el PDF.
            val cmToDpRatio = 37.8f

            val newTop = top?.toFloatOrNull()?.let { (it * cmToDpRatio).dp } ?: currentState.marginTop
            val newBottom = bottom?.toFloatOrNull()?.let { (it * cmToDpRatio).dp } ?: currentState.marginBottom
            val newLeft = left?.toFloatOrNull()?.let { (it * cmToDpRatio).dp } ?: currentState.marginLeft
            val newRight = right?.toFloatOrNull()?.let { (it * cmToDpRatio).dp } ?: currentState.marginRight

            currentState.copy(
                marginTop = newTop,
                marginBottom = newBottom,
                marginLeft = newLeft,
                marginRight = newRight
            )
        }
    }

    // TODO: Add functions for template loading/saving
}
