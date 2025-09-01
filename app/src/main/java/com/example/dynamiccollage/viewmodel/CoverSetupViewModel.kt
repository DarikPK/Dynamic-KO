package com.example.dynamiccollage.viewmodel

import android.net.Uri
import androidx.compose.ui.graphics.Color // Asegúrate que Color esté importado si no lo estaba
import androidx.compose.ui.text.style.TextAlign // Asegúrate que TextAlign esté importado
import androidx.lifecycle.ViewModel
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.DefaultCoverConfig // Sigue siendo usado por fieldId
import com.example.dynamiccollage.data.model.PageOrientation // NUEVA IMPORTACIÓN
import com.example.dynamiccollage.data.model.DocumentType
import com.example.dynamiccollage.data.model.SelectedSunatData
import com.example.dynamiccollage.remote.DniData
import com.example.dynamiccollage.remote.RucData
import com.example.dynamiccollage.remote.SunatData
import com.example.dynamiccollage.data.model.TextStyleConfig
import android.content.ContentResolver
import android.graphics.BitmapFactory
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

    fun onDocumentTypeChange(newType: DocumentType) {
        _coverConfig.update { it.copy(documentType = newType) }
    }

    fun onShowClientPrefixChange(show: Boolean) {
        _coverConfig.update { it.copy(showClientPrefix = show) }
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

    fun onSunatDataReceived(data: SelectedSunatData) {
        _coverConfig.update { currentState ->
            val address = data.direccion ?: currentState.subtitleStyle.content
            val clientName = data.nombre ?: currentState.clientNameStyle.content

            // Only update the doc type if it's not explicitly set to NONE
            val docType = if (currentState.documentType == DocumentType.NONE) {
                DocumentType.NONE
            } else {
                if (data.numeroDocumento.length == 8) DocumentType.DNI else DocumentType.RUC
            }

            currentState.copy(
                documentType = docType,
                clientNameStyle = currentState.clientNameStyle.copy(content = clientName),
                rucStyle = currentState.rucStyle.copy(content = data.numeroDocumento),
                subtitleStyle = currentState.subtitleStyle.copy(content = address)
            )
        }
    }

    fun onMainImageSelected(uri: Uri?, contentResolver: ContentResolver) {
        _coverConfig.update { currentState ->
            val orientation = uri?.let {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(inputStream, null, options)
                    if (options.outWidth > options.outHeight) {
                        PageOrientation.Horizontal
                    } else {
                        PageOrientation.Vertical
                    }
                }
            }
            currentState.copy(
                mainImageUri = uri?.toString(),
                pageOrientation = orientation ?: currentState.pageOrientation
            )
        }
    }

    fun clearMainImage() {
        _coverConfig.update { currentState ->
            currentState.copy(mainImageUri = null)
        }
    }

    // NUEVA FUNCIÓN
    fun onPageOrientationChange(newOrientation: PageOrientation) {
        _coverConfig.update { currentState ->
            currentState.copy(pageOrientation = newOrientation)
        }
    }

    fun onAllCapsChange(allCaps: Boolean) {
        _coverConfig.update { currentState ->
            currentState.copy(allCaps = allCaps)
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

    fun onWeightChange(
        client: String? = null,
        ruc: String? = null,
        address: String? = null,
        separation: String? = null,
        photo: String? = null
    ) {
        _coverConfig.update { currentState ->
            currentState.copy(
                clientWeight = client?.toFloatOrNull()?.coerceIn(0.1f, 10f) ?: currentState.clientWeight,
                rucWeight = ruc?.toFloatOrNull()?.coerceIn(0.1f, 10f) ?: currentState.rucWeight,
                addressWeight = address?.toFloatOrNull()?.coerceIn(0.1f, 10f) ?: currentState.addressWeight,
                separationWeight = separation?.toFloatOrNull()?.coerceIn(0.1f, 10f) ?: currentState.separationWeight,
                photoWeight = photo?.toFloatOrNull()?.coerceIn(0.1f, 10f) ?: currentState.photoWeight
            )
        }
    }

    fun updateRowStyles(
        clientRowStyle: com.example.dynamiccollage.data.model.RowStyle,
        rucRowStyle: com.example.dynamiccollage.data.model.RowStyle,
        addressRowStyle: com.example.dynamiccollage.data.model.RowStyle,
        photoRowStyle: com.example.dynamiccollage.data.model.RowStyle
    ) {
        _coverConfig.update { currentState ->
            currentState.copy(
                clientNameStyle = currentState.clientNameStyle.copy(rowStyle = clientRowStyle),
                rucStyle = currentState.rucStyle.copy(rowStyle = rucRowStyle),
                subtitleStyle = currentState.subtitleStyle.copy(rowStyle = addressRowStyle),
                photoStyle = photoRowStyle
            )
        }
    }
}
