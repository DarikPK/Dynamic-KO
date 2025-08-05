package com.example.dynamiccollage.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import androidx.core.content.FileProvider
import com.example.dynamiccollage.remote.SunatData
import com.example.dynamiccollage.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProjectViewModel : ViewModel() {

    private val _currentCoverConfig = MutableStateFlow(CoverPageConfig())
    val currentCoverConfig: StateFlow<CoverPageConfig> = _currentCoverConfig.asStateFlow()

    private val _currentPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val currentPageGroups: StateFlow<List<PageGroup>> = _currentPageGroups.asStateFlow()

    private val _sunatData = MutableStateFlow<SunatData?>(null)
    val sunatData: StateFlow<SunatData?> = _sunatData.asStateFlow()

    fun updateSunatData(data: SunatData) {
        _sunatData.value = data
    }

    fun updateCoverConfig(newConfig: CoverPageConfig) {
        _currentCoverConfig.value = newConfig
    }

    fun addPageGroup(group: PageGroup) {
        _currentPageGroups.update { currentList -> currentList + group }
    }

    fun updatePageGroup(groupId: String, transform: (PageGroup) -> PageGroup) {
        _currentPageGroups.update { currentList ->
            currentList.map { if (it.id == groupId) transform(it) else it }
        }
    }

    fun deletePageGroup(groupId: String) {
        _currentPageGroups.update { currentList ->
            currentList.filterNot { it.id == groupId }
        }
    }

    fun resetPageGroups() {
        _currentPageGroups.value = emptyList()
    }

    fun resetProject() {
        _currentCoverConfig.value = CoverPageConfig()
        resetPageGroups()
    }

    // --- Generación de PDF ---
    private val _pdfGenerationState = MutableStateFlow<PdfGenerationState>(PdfGenerationState.Idle)
    val pdfGenerationState: StateFlow<PdfGenerationState> = _pdfGenerationState.asStateFlow()

    fun generatePdf(context: Context, fileName: String) {
        val coverConfig = _currentCoverConfig.value
        val pageGroups = _currentPageGroups.value

        val isCoverEmpty = coverConfig.clientNameStyle.content.isBlank() &&
                coverConfig.rucStyle.content.isBlank() &&
                coverConfig.subtitleStyle.content.isBlank() &&
                coverConfig.mainImageUri == null
        val areInnerPagesEmpty = pageGroups.all { it.imageUris.isEmpty() }

        if (isCoverEmpty && areInnerPagesEmpty) {
            _pdfGenerationState.value = PdfGenerationState.Error("No hay contenido para generar un PDF.")
            return
        }

        viewModelScope.launch {
            Log.d("ProjectViewModel", "generatePdf: Iniciando...")
            _pdfGenerationState.value = PdfGenerationState.Loading
            val generatedFile = withContext(Dispatchers.IO) {
                Log.d("ProjectViewModel", "generatePdf: En el hilo de IO, llamando a PdfGenerator.")
                PdfGenerator.generate(
                    context = context,
                    coverConfig = _currentCoverConfig.value,
                    pageGroups = _currentPageGroups.value,
                    fileName = fileName.ifBlank { "DynamicCollage" }
                )
            }
            if (generatedFile != null) {
                Log.d("ProjectViewModel", "generatePdf: Éxito. Archivo: ${generatedFile.absolutePath}")
                _pdfGenerationState.value = PdfGenerationState.Success(generatedFile)
            } else {
                Log.e("ProjectViewModel", "generatePdf: Fallo. `generatedFile` es nulo.")
                _pdfGenerationState.value = PdfGenerationState.Error("No se pudo generar el PDF.")
            }
        }
    }

    fun resetPdfGenerationState() {
        _pdfGenerationState.value = PdfGenerationState.Idle
    }

    // --- Lógica para Compartir PDF ---
    private val _shareablePdfUri = MutableStateFlow<Uri?>(null)
    val shareablePdfUri: StateFlow<Uri?> = _shareablePdfUri.asStateFlow()

    fun createShareableUriForFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            _shareablePdfUri.value = uri
        } catch (e: Exception) {
            e.printStackTrace()
            _pdfGenerationState.value = PdfGenerationState.Error("No se pudo crear el enlace para compartir.")
        }
    }

    fun resetShareableUri() {
        _shareablePdfUri.value = null
    }
}

sealed class PdfGenerationState {
    object Idle : PdfGenerationState()
    object Loading : PdfGenerationState()
    data class Success(val file: File) : PdfGenerationState()
    data class Error(val message: String) : PdfGenerationState()
}
