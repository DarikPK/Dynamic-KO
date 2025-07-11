package com.example.dynamiccollage.viewmodel

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageOrientation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.io.InputStream
import kotlin.math.roundToInt

class CoverSetupViewModel : ViewModel() {

    private val _coverConfig = MutableStateFlow(CoverPageConfig())
    val coverConfig: StateFlow<CoverPageConfig> = _coverConfig.asStateFlow()

    private val _detectedPhotoOrientation = MutableStateFlow<PageOrientation?>(null)
    val detectedPhotoOrientation: StateFlow<PageOrientation?> = _detectedPhotoOrientation.asStateFlow()

    fun loadInitialConfig(initialConfig: CoverPageConfig) {
        _coverConfig.value = initialConfig
        _detectedPhotoOrientation.value = null // Resetear al cargar, la detección es solo para nuevas selecciones
    }

    fun onClientNameChange(newName: String) {
        _coverConfig.update { it.copy(clientNameStyle = it.clientNameStyle.copy(content = newName)) }
    }

    fun onRucChange(newRuc: String) {
        _coverConfig.update { it.copy(rucStyle = it.rucStyle.copy(content = newRuc)) }
    }

    fun onAddressChange(newAddress: String) {
        _coverConfig.update { it.copy(subtitleStyle = it.subtitleStyle.copy(content = newAddress)) }
    }

    fun onMainImageSelected(uri: Uri?, contentResolver: ContentResolver) {
        _coverConfig.update { it.copy(mainImageUri = uri?.toString()) }
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
                        //    onPageOrientationChange(detected) // Asegúrate que esta función no cree un bucle si también actualiza la foto
                        // }
                    } else {
                        _detectedPhotoOrientation.value = null
                    }
                } ?: run { _detectedPhotoOrientation.value = null } // Si openInputStream devuelve null
            } catch (e: Exception) {
                _detectedPhotoOrientation.value = null
                e.printStackTrace() // Consider a more user-friendly error handling/logging
            }
        } else {
            _detectedPhotoOrientation.value = null
        }
    }

    fun onPageOrientationChange(newOrientation: PageOrientation) {
        _coverConfig.update { it.copy(pageOrientation = newOrientation) }
    }

    fun updateClientNameStyle(newSize: Float? = null, newAlign: TextAlign? = null, newColor: Color? = null) {
        _coverConfig.update { current ->
            current.copy(
                clientNameStyle = current.clientNameStyle.copy(
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72) ?: current.clientNameStyle.fontSize,
                    textAlign = newAlign ?: current.clientNameStyle.textAlign,
                    fontColor = newColor ?: current.clientNameStyle.fontColor
                )
            )
        }
    }

    fun updateRucStyle(newSize: Float? = null, newAlign: TextAlign? = null, newColor: Color? = null) {
        _coverConfig.update { current ->
            current.copy(
                rucStyle = current.rucStyle.copy(
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72) ?: current.rucStyle.fontSize,
                    textAlign = newAlign ?: current.rucStyle.textAlign,
                    fontColor = newColor ?: current.rucStyle.fontColor
                )
            )
        }
    }

    fun updateSubtitleStyle(newSize: Float? = null, newAlign: TextAlign? = null, newColor: Color? = null) {
        _coverConfig.update { current ->
            current.copy(
                subtitleStyle = current.subtitleStyle.copy(
                    fontSize = newSize?.roundToInt()?.coerceIn(8, 72) ?: current.subtitleStyle.fontSize,
                    textAlign = newAlign ?: current.subtitleStyle.textAlign,
                    fontColor = newColor ?: current.subtitleStyle.fontColor
                )
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
