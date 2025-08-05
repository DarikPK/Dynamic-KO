package com.example.dynamiccollage.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import androidx.core.content.FileProvider // Importar FileProvider
import com.example.dynamiccollage.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.net.Uri // Importar Uri
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ProjectViewModel : ViewModel() {

    private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
    val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages

    private val _currentCoverConfig = MutableStateFlow(CoverPageConfig())
    val currentCoverConfig: StateFlow<CoverPageConfig> = _currentCoverConfig.asStateFlow()

    private val _currentPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val currentPageGroups: StateFlow<List<PageGroup>> = _currentPageGroups.asStateFlow()

    fun updateCoverConfig(newConfig: CoverPageConfig) {
        _currentCoverConfig.value = newConfig
    }

    fun setPageGroups(groups: List<PageGroup>) {
        _currentPageGroups.value = groups
    }

    fun onAddImagesClickedForGroup(groupId: String) {
        _currentGroupAddingImages.value = groupId
    }

    fun addPageGroupToProject(group: PageGroup) {
        _currentPageGroups.update { currentList -> currentList + group }
    }

    fun updatePageGroupInProject(updatedGroup: PageGroup) {
        _currentPageGroups.update { currentList ->
            currentList.map { if (it.id == updatedGroup.id) updatedGroup else it }
        }
    }

    fun removePageGroupFromProject(groupId: String) {
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
        viewModelScope.launch {
            _pdfGenerationState.value = PdfGenerationState.Loading
            val generatedFile = withContext(Dispatchers.IO) {
                PdfGenerator.generate(
                    context = context,
                    coverConfig = _currentCoverConfig.value,
                    pageGroups = _currentPageGroups.value,
                    fileName = fileName.ifBlank { "DynamicCollage" }
                )
            }
            _pdfGenerationState.value = if (generatedFile != null) {
                PdfGenerationState.Success(generatedFile)
            } else {
                PdfGenerationState.Error("No se pudo generar el PDF.")
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
            // Podríamos tener un StateFlow de error de "compartir" también
            _pdfGenerationState.value = PdfGenerationState.Error("No se pudo crear el enlace para compartir.")
        }
    }

    fun resetShareableUri() {
        _shareablePdfUri.value = null
    }
}

// Clase para manejar los estados de la generación de PDF
sealed class PdfGenerationState {
    object Idle : PdfGenerationState()
    object Loading : PdfGenerationState()
    data class Success(val file: File) : PdfGenerationState()
    data class Error(val message: String) : PdfGenerationState()
}

private val _isEditingGroupConfigValid = MutableStateFlow(true) // Nuevo StateFlow para validación
val isEditingGroupConfigValid: StateFlow<Boolean> = _isEditingGroupConfigValid.asStateFlow()

// Estado para saber a qué grupo se le están añadiendo imágenes
private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages.asStateFlow()

fun onAddImagesClickedForGroup(groupId: String) {
    _currentGroupAddingImages.value = groupId
    // La lógica para lanzar el selector de imágenes estará en la UI (Screen)
    // porque necesita el ActivityResultLauncher.
}

fun onImagesSelectedForGroup(uris: List<android.net.Uri>) {
    val groupId = _currentGroupAddingImages.value ?: return // Si no hay grupo actual, no hacer nada
}