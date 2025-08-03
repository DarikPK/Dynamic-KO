package com.example.dynamiccollage.viewmodel

import android.net.Uri
import androidx.compose.ui.graphics.Color // Asegúrate que Color esté importado si no lo estaba
import androidx.compose.ui.text.style.TextAlign // Asegúrate que TextAlign esté importado
import androidx.lifecycle.ViewModel
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.DefaultCoverConfig // Sigue siendo usado por fieldId
import com.example.dynamiccollage.data.model.PageOrientation // NUEVA IMPORTACIÓN
import com.example.dynamiccollage.data.model.TextStyleConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

class CoverSetupViewModel : ViewModel() {

    private val _coverConfig = MutableStateFlow(CoverPageConfig())
    val coverConfig: StateFlow<CoverPageConfig> = _coverConfig.asStateFlow()

    fun loadInitialConfig(initialConfig: CoverPageConfig) {
        _coverConfig.value = initialConfig
    }

    fun onClientNameChange(newName: String) {
        _coverConfig.update { currentState ->
            currentState.copy(
                clientNameStyle = currentState.clientNameStyle.copy(content = newName)
            )
        }
    }

    fun onShowAddressPrefixChange(show: Boolean) {
        _coverConfig.update { currentState ->
            currentState.copy(showAddressPrefix = show)
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
            // Guardar la URI como String para consistencia con el modelo CoverPageConfig
            currentState.copy(mainImageUri = uri?.toString())
        }
    }

    // NUEVA FUNCIÓN
    fun onPageOrientationChange(newOrientation: PageOrientation) {
        _coverConfig.update { currentState ->
            currentState.copy(pageOrientation = newOrientation)
        }
    }

    fun onTextStyleChange(
        fieldId: String,
        newSize: Float? = null,
        newAlign: TextAlign? = null,
        newColor: Color? = null
    ) {
        _coverConfig.update { currentState ->
            val newClientStyle = if (fieldId == DefaultCoverConfig.CLIENT_NAME_ID) {
                currentState.clientNameStyle.copy(
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72) ?: currentState.clientNameStyle.fontSize,
                    textAlign = newAlign ?: currentState.clientNameStyle.textAlign,
                    fontColor = newColor ?: currentState.clientNameStyle.fontColor
                )
            } else currentState.clientNameStyle

            val newRucStyle = if (fieldId == DefaultCoverConfig.RUC_ID) {
                currentState.rucStyle.copy(
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72) ?: currentState.rucStyle.fontSize,
                    textAlign = newAlign ?: currentState.rucStyle.textAlign,
                    fontColor = newColor ?: currentState.rucStyle.fontColor
                )
            } else currentState.rucStyle

            val newSubtitleStyle = if (fieldId == DefaultCoverConfig.SUBTITLE_ID) {
                currentState.subtitleStyle.copy(
                    // CORRECCIÓN DE CONSISTENCIA: usar roundToInt como en los otros
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72) ?: currentState.subtitleStyle.fontSize,
                    textAlign = newAlign ?: currentState.subtitleStyle.textAlign,
                    fontColor = newColor ?: currentState.subtitleStyle.fontColor
                )
            } else currentState.subtitleStyle

            currentState.copy(
                clientNameStyle = newClientStyle,
                rucStyle = newRucStyle,
                subtitleStyle = newSubtitleStyle
            )
        }
    }

    fun onBorderColorChange(newColor: Color) {
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
            val newTop = top?.toFloatOrNull() ?: currentState.marginTop
            val newBottom = bottom?.toFloatOrNull() ?: currentState.marginBottom
            val newLeft = left?.toFloatOrNull() ?: currentState.marginLeft
            val newRight = right?.toFloatOrNull() ?: currentState.marginRight

            currentState.copy(
                marginTop = newTop,
                marginBottom = newBottom,
                marginLeft = newLeft,
                marginRight = newRight
            )
        }
    }
}
