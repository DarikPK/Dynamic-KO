package com.example.dynamiccollage.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfPreviewViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    private val _previewBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val previewBitmaps: StateFlow<List<Bitmap>> = _previewBitmaps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun generatePreview(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val bitmaps = withContext(Dispatchers.IO) {
                // Esta función actualmente retorna una lista vacía.
                // Cuando se implemente una librería de renderizado, generará los bitmaps reales.
                PdfGenerator.generatePreviewBitmaps(
                    context = context,
                    coverConfig = projectViewModel.currentCoverConfig.value,
                    pageGroups = projectViewModel.currentPageGroups.value
                )
            }
            _previewBitmaps.value = bitmaps
            _isLoading.value = false
        }
    }
}
