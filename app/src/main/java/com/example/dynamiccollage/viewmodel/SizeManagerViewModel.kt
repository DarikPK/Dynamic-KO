package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import com.example.dynamiccollage.data.model.CoverPageConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SizeManagerViewModel : ViewModel() {

    private val _imageQuality = MutableStateFlow(100)
    val imageQuality: StateFlow<Int> = _imageQuality.asStateFlow()

    private val _autoAdjustSize = MutableStateFlow(true)
    val autoAdjustSize: StateFlow<Boolean> = _autoAdjustSize.asStateFlow()

    fun loadInitialState(config: CoverPageConfig) {
        _imageQuality.value = config.imageQuality
        _autoAdjustSize.value = config.autoAdjustSize
    }

    fun onQualityChange(newQuality: Float) {
        _imageQuality.value = newQuality.toInt()
    }

    fun onAutoAdjustChange(newValue: Boolean) {
        _autoAdjustSize.value = newValue
    }
}
