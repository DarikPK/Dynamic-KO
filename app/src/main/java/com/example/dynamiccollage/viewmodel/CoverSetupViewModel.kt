package com.example.dynamiccollage.viewmodel

import android.content.ContentResolver // Importación para ContentResolver
import android.graphics.BitmapFactory // Importación para BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color // Asegúrate que Color esté importado si no lo estaba
import androidx.compose.ui.text.style.TextAlign // Asegúrate que TextAlign esté importado
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
// viewModelScope no es estrictamente necesario aquí si no hay corutinas lanzadas directamente en init o similar
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.DefaultCoverConfig // Sigue siendo usado por fieldId para onTextStyleChange
import com.example.dynamiccollage.data.model.PageOrientation // NUEVA IMPORTACIÓN
import com.example.dynamiccollage.data.model.TextStyleConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.InputStream // Importación para InputStream
import kotlin.math.roundToInt

class CoverSetupViewModel : ViewModel() {

    private val _coverConfig = MutableStateFlow(CoverPageConfig())
    val coverConfig: StateFlow<CoverPageConfig> = _coverConfig.asStateFlow()

    // NUEVO: Para la orientación detectada de la foto
    private val _detectedPhotoOrientation = MutableStateFlow<PageOrientation?>(null)
    val detectedPhotoOrientation: StateFlow<PageOrientation?> = _detectedPhotoOrientation.asStateFlow()

    fun loadInitialConfig(initialConfig: CoverPageConfig) {
        _coverConfig.value = initialConfig
        _detectedPhotoOrientation.value = null // Resetear al cargar config, detección al seleccionar nueva imagen
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

    // MODIFICADO: para aceptar ContentResolver y detectar orientación
    fun onMainImageSelected(uri: Uri?, contentResolver: ContentResolver) {
        _coverConfig.update { currentState ->
            currentState.copy(mainImageUri = uri)
        }
        if (uri != null) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(stream, null, options)
                    val imageHeight = options.outHeight
                    val imageWidth = options.outWidth

                    if (imageWidth > 0 && imageHeight > 0) {
                        val detected = if (imageHeight > imageWidth) {
                            PageOrientation.Vertical
                        } else {
                            PageOrientation.Horizontal
                        }
                        _detectedPhotoOrientation.value = detected
                        // Opcional: Preseleccionar orientación de página
                        // if (_coverConfig.value.pageOrientation != detected) {
                        //    onPageOrientationChange(detected)
                        // }
                    } else {
                        _detectedPhotoOrientation.value = null
                    }
                } ?: run { _detectedPhotoOrientation.value = null }
            } catch (e: Exception) {
                _detectedPhotoOrientation.value = null
                e.printStackTrace() // Log o manejo de error
            }
        } else {
            _detectedPhotoOrientation.value = null
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
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72)?.sp ?: currentState.clientNameStyle.fontSize,
                    textAlign = newAlign ?: currentState.clientNameStyle.textAlign,
                    fontColor = newColor ?: currentState.clientNameStyle.fontColor
                )
            } else currentState.clientNameStyle

            val newRucStyle = if (fieldId == DefaultCoverConfig.RUC_ID) {
                currentState.rucStyle.copy(
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72)?.sp ?: currentState.rucStyle.fontSize,
                    textAlign = newAlign ?: currentState.rucStyle.textAlign,
                    fontColor = newColor ?: currentState.rucStyle.fontColor
                )
            } else currentState.rucStyle

            val newSubtitleStyle = if (fieldId == DefaultCoverConfig.SUBTITLE_ID) {
                currentState.subtitleStyle.copy(
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72)?.sp ?: currentState.subtitleStyle.fontSize,
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
