package com.example.dynamiccollage.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.dynamiccollage.data.model.BorderProperties
import com.example.dynamiccollage.data.model.RowStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RowType {
    CLIENT, RUC, ADDRESS, PHOTO
}

class RowStyleViewModel : ViewModel() {

    private val _clientRowStyle = MutableStateFlow(RowStyle())
    val clientRowStyle: StateFlow<RowStyle> = _clientRowStyle.asStateFlow()

    private val _rucRowStyle = MutableStateFlow(RowStyle())
    val rucRowStyle: StateFlow<RowStyle> = _rucRowStyle.asStateFlow()

    private val _addressRowStyle = MutableStateFlow(RowStyle())
    val addressRowStyle: StateFlow<RowStyle> = _addressRowStyle.asStateFlow()

    private val _photoRowStyle = MutableStateFlow(RowStyle())
    val photoRowStyle: StateFlow<RowStyle> = _photoRowStyle.asStateFlow()

    fun loadStyles(
        client: RowStyle,
        ruc: RowStyle,
        address: RowStyle,
        photo: RowStyle
    ) {
        _clientRowStyle.value = client
        _rucRowStyle.value = ruc
        _addressRowStyle.value = address
        _photoRowStyle.value = photo
    }

    fun updateBackgroundColor(rowType: RowType, color: Color) {
        getMutableStateFlow(rowType).update { it.copy(backgroundColor = color) }
    }

    fun updatePadding(rowType: RowType, top: String?, bottom: String?, left: String?, right: String?) {
        getMutableStateFlow(rowType).update { style ->
            style.copy(
                padding = style.padding.copy(
                    top = top?.toFloatOrNull() ?: style.padding.top,
                    bottom = bottom?.toFloatOrNull() ?: style.padding.bottom,
                    left = left?.toFloatOrNull() ?: style.padding.left,
                    right = right?.toFloatOrNull() ?: style.padding.right
                )
            )
        }
    }

    fun updateBorderColor(rowType: RowType, color: Color) {
        getMutableStateFlow(rowType).update { style ->
            style.copy(border = style.border.copy(color = color))
        }
    }

    fun updateBorderThickness(rowType: RowType, thickness: String) {
        val floatThickness = thickness.toFloatOrNull() ?: return
        getMutableStateFlow(rowType).update { style ->
            style.copy(border = style.border.copy(thickness = floatThickness))
        }
    }

    fun updateBorderVisibility(rowType: RowType, top: Boolean?, bottom: Boolean?, left: Boolean?, right: Boolean?) {
        getMutableStateFlow(rowType).update { style ->
            style.copy(
                border = style.border.copy(
                    top = top ?: style.border.top,
                    bottom = bottom ?: style.border.bottom,
                    left = left ?: style.border.left,
                    right = right ?: style.border.right
                )
            )
        }
    }

    private fun getMutableStateFlow(rowType: RowType): MutableStateFlow<RowStyle> {
        return when (rowType) {
            RowType.CLIENT -> _clientRowStyle
            RowType.RUC -> _rucRowStyle
            RowType.ADDRESS -> _addressRowStyle
            RowType.PHOTO -> _photoRowStyle
        }
    }
}
