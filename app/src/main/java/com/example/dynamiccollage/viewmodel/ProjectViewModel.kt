package com.example.dynamiccollage.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.toDomain
import com.example.dynamiccollage.data.toSerializable
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.SelectedSunatData
import com.example.dynamiccollage.data.model.SerializableProjectState
import com.example.dynamiccollage.utils.PdfGenerator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    init {
        loadProject()
    }

    private val _currentCoverConfig = MutableStateFlow(CoverPageConfig())
    val currentCoverConfig: StateFlow<CoverPageConfig> = _currentCoverConfig.asStateFlow()

    private val _currentPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val currentPageGroups: StateFlow<List<PageGroup>> = _currentPageGroups.asStateFlow()

    private val _sunatData = MutableStateFlow<SelectedSunatData?>(null)
    val sunatData: StateFlow<SelectedSunatData?> = _sunatData.asStateFlow()

    fun updateSunatData(data: SelectedSunatData) {
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

    private val _pdfSize = MutableStateFlow(0L)
    val pdfSize: StateFlow<Long> = _pdfSize.asStateFlow()

    private val _pdfSizeMode = MutableStateFlow(1)
    val pdfSizeMode: StateFlow<Int> = _pdfSizeMode.asStateFlow()

    fun setPdfSizeMode(mode: Int) {
        _pdfSizeMode.value = mode
    }

    fun getFormattedPdfSize(): String {
        val size = _pdfSize.value
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "%.2f MB".format(size / (1024.0 * 1024.0))
        }
    }

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
                _pdfSize.value = generatedFile.length()
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

    fun getAllImageUris(): List<String> {
        val coverImage = _currentCoverConfig.value.mainImageUri
        val innerImages = _currentPageGroups.value.flatMap { it.imageUris }
        val allImages = mutableListOf<String>()
        coverImage?.let { allImages.add(it) }
        allImages.addAll(innerImages)
        return allImages
    }

    fun saveCroppedImage(context: Context, oldUri: String, croppedBitmap: Bitmap) {
        viewModelScope.launch {
            val newUri = withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                FileOutputStream(file).use { out ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                Uri.fromFile(file).toString()
            }

            val coverImage = _currentCoverConfig.value.mainImageUri
            if (coverImage == oldUri) {
                _currentCoverConfig.update { it.copy(mainImageUri = newUri) }
            } else {
                _currentPageGroups.update { groups ->
                    groups.map { group ->
                        if (group.imageUris.contains(oldUri)) {
                            val newImageUris = group.imageUris.map { if (it == oldUri) newUri else it }
                            group.copy(imageUris = newImageUris)
                        } else {
                            group
                        }
                    }
                }
            }
        }
    }

    // --- Lógica de Guardado y Carga de Proyecto ---
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val gson = Gson()
    private val projectFileName = "last_project.json"

    fun saveProject() {
        viewModelScope.launch {
            val serializableState = SerializableProjectState(
                coverConfig = _currentCoverConfig.value.toSerializable(),
                pageGroups = _currentPageGroups.value.map { it.toSerializable() },
                sunatData = _sunatData.value
            )
            val jsonString = gson.toJson(serializableState)
            val sizeInBytes = jsonString.toByteArray().size.toLong()
            val sizeLimitBytes = 50 * 1024 * 1024 // 50MB

            if (sizeInBytes > sizeLimitBytes) {
                _saveState.value = SaveState.RequiresConfirmation(sizeInBytes)
            } else {
                writeJsonToFile(jsonString)
            }
        }
    }

    fun forceSaveProject() {
        viewModelScope.launch {
            val serializableState = SerializableProjectState(
                coverConfig = _currentCoverConfig.value.toSerializable(),
                pageGroups = _currentPageGroups.value.map { it.toSerializable() },
                sunatData = _sunatData.value
            )
            val jsonString = gson.toJson(serializableState)
            writeJsonToFile(jsonString)
        }
    }

    private suspend fun writeJsonToFile(jsonString: String) {
        val context = getApplication<Application>().applicationContext
        withContext(Dispatchers.IO) {
            try {
                context.openFileOutput(projectFileName, Context.MODE_PRIVATE).use {
                    it.write(jsonString.toByteArray())
                }
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error saving project", e)
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadProject() {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            val file = File(context.filesDir, projectFileName)
            if (!file.exists()) return@launch

            val jsonString = withContext(Dispatchers.IO) {
                try {
                    context.openFileInput(projectFileName).bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    Log.e("ProjectViewModel", "Error loading project", e)
                    null
                }
            }

            if (jsonString != null) {
                try {
                    val serializableState = gson.fromJson(jsonString, SerializableProjectState::class.java)
                    val projectState = serializableState.toDomain()
                    _currentCoverConfig.value = projectState.coverConfig
                    _currentPageGroups.value = projectState.pageGroups
                    _sunatData.value = projectState.sunatData
                } catch (e: Exception) {
                    Log.e("ProjectViewModel", "Error parsing project JSON", e)
                }
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

sealed class SaveState {
    object Idle : SaveState()
    data class RequiresConfirmation(val sizeInBytes: Long) : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

sealed class PdfGenerationState {
    object Idle : PdfGenerationState()
    object Loading : PdfGenerationState()
    data class Success(val file: File) : PdfGenerationState()
    data class Error(val message: String) : PdfGenerationState()
}
