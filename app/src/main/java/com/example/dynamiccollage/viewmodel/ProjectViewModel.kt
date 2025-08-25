package com.example.dynamiccollage.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.ImageBorderSettings
import com.example.dynamiccollage.data.toDomain
import com.example.dynamiccollage.data.toSerializable
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.ImageEffectSettings
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.SerializableNormalizedRectF
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

class ProjectViewModel : ViewModel() {

    private val _currentCoverConfig = MutableStateFlow(CoverPageConfig())
    val currentCoverConfig: StateFlow<CoverPageConfig> = _currentCoverConfig.asStateFlow()

    private val _currentPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val currentPageGroups: StateFlow<List<PageGroup>> = _currentPageGroups.asStateFlow()

    private val _sunatData = MutableStateFlow<SelectedSunatData?>(null)
    val sunatData: StateFlow<SelectedSunatData?> = _sunatData.asStateFlow()

    private val _themeName = MutableStateFlow("Oscuro")
    val themeName: StateFlow<String> = _themeName.asStateFlow()

    private val _imageEffectSettings = MutableStateFlow<Map<String, ImageEffectSettings>>(emptyMap())
    val imageEffectSettings: StateFlow<Map<String, ImageEffectSettings>> = _imageEffectSettings.asStateFlow()

    private val _managerSelectedUri = MutableStateFlow<String?>(null)
    val managerSelectedUri: StateFlow<String?> = _managerSelectedUri.asStateFlow()

    fun setManagerSelectedUri(uri: String?) {
        _managerSelectedUri.value = uri
    }

    fun updateImageEffectSettings(context: Context, uri: String, settings: ImageEffectSettings) {
        _imageEffectSettings.update { currentMap ->
            currentMap.toMutableMap().apply {
                this[uri] = settings
            }
        }
        saveProject(context)
    }

    fun updateImageRotation(context: Context, uri: String, degrees: Float) {
        _imageEffectSettings.update { currentMap ->
            val currentSettings = currentMap[uri] ?: ImageEffectSettings()
            currentMap.toMutableMap().apply {
                this[uri] = currentSettings.copy(rotationDegrees = degrees)
            }
        }
        saveProject(context)
    }

    fun updateImageCrop(context: Context, uri: String, cropRect: SerializableNormalizedRectF?) {
        _imageEffectSettings.update { currentMap ->
            val currentSettings = currentMap[uri] ?: ImageEffectSettings()
            currentMap.toMutableMap().apply {
                this[uri] = currentSettings.copy(cropRect = cropRect)
            }
        }
        saveProject(context)
    }

    fun resetImageTransforms(context: Context, uri: String) {
        _imageEffectSettings.update { currentMap ->
            val currentSettings = currentMap[uri] ?: ImageEffectSettings()
            currentMap.toMutableMap().apply {
                // Reset only transform properties, keep other effects
                this[uri] = currentSettings.copy(rotationDegrees = 0f, cropRect = null)
            }
        }
        saveProject(context)
    }

    fun updateTheme(context: Context, newThemeName: String) {
        _themeName.value = newThemeName
        saveProject(context)
    }

    fun updateSunatData(context: Context, data: SelectedSunatData) {
        _sunatData.value = data
        saveProject(context)
    }

    fun updateCoverConfig(newConfig: CoverPageConfig) {
        _currentCoverConfig.value = newConfig
    }

    fun updatePageBackgroundColor(context: Context, color: Color) {
        _currentCoverConfig.update { it.copy(pageBackgroundColor = color.toArgb()) }
        saveProject(context)
    }

    fun updateImageBorderSettings(context: Context, newSettingsMap: Map<String, ImageBorderSettings>) {
        _currentCoverConfig.update { it.copy(imageBorderSettingsMap = newSettingsMap) }
        saveProject(context)
    }

    fun addPageGroup(context: Context, group: PageGroup) {
        _currentPageGroups.update { currentList -> currentList + group }
        saveProject(context)
    }

    fun updatePageGroup(context: Context, groupId: String, transform: (PageGroup) -> PageGroup) {
        _currentPageGroups.update { currentList ->
            currentList.map { if (it.id == groupId) transform(it) else it }
        }
        saveProject(context)
    }

    fun deletePageGroup(context: Context, groupId: String) {
        val groupToDelete = _currentPageGroups.value.find { it.id == groupId }
        viewModelScope.launch(Dispatchers.IO) {
            groupToDelete?.imageUris?.forEach { uri ->
                deleteLocalImage(uri)
            }
        }
        _currentPageGroups.update { currentList ->
            currentList.filterNot { it.id == groupId }
        }
        saveProject(context)
    }

    fun resetPageGroups(context: Context) {
        _currentPageGroups.value = emptyList()
        saveProject(context)
    }

    fun resetProject(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete images directory
            val imagesDir = File(context.applicationContext.filesDir, "images")
            if (imagesDir.exists()) {
                imagesDir.deleteRecursively()
            }
            // Delete saved project file
            val projectFile = File(context.applicationContext.filesDir, projectFileName)
            if (projectFile.exists()) {
                projectFile.delete()
            }
        }
        // Reset in-memory state
        _currentCoverConfig.value = CoverPageConfig()
        _currentPageGroups.value = emptyList()
        _sunatData.value = null
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
        val areInnerPagesEmpty = _currentPageGroups.value.all { it.imageUris.isEmpty() }

        val isCoverEmpty = coverConfig.clientNameStyle.content.isBlank() &&
                coverConfig.rucStyle.content.isBlank() &&
                coverConfig.subtitleStyle.content.isBlank() &&
                coverConfig.mainImageUri == null

        if (isCoverEmpty && areInnerPagesEmpty) {
            _pdfGenerationState.value = PdfGenerationState.Error("No hay contenido para generar un PDF.")
            return
        }

        viewModelScope.launch {
            Log.d("ProjectViewModel", "generatePdf: Iniciando...")
            _pdfGenerationState.value = PdfGenerationState.Loading
            val generatedFile = withContext(Dispatchers.IO) {
                val generatedPages = com.example.dynamiccollage.utils.PdfContentManager.groupImagesForPdf(
                    context,
                    _currentPageGroups.value
                )

                Log.d("ProjectViewModel", "generatePdf: En el hilo de IO, llamando a PdfGenerator.")
                PdfGenerator.generate(
                    context = context,
                    coverConfig = _currentCoverConfig.value,
                    generatedPages = generatedPages,
                    fileName = fileName.ifBlank { "DynamicCollage" },
                    imageEffectSettings = _imageEffectSettings.value
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

    private fun deleteLocalImage(path: String) {
        try {
            if (path.startsWith("file://")) {
                val file = File(Uri.parse(path).path!!)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "Error deleting local image", e)
        }
    }

    fun removeImageFromPageGroup(context: Context, groupId: String, uri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteLocalImage(uri)
        }
        updatePageGroup(context, groupId) { group ->
            group.copy(imageUris = group.imageUris.filterNot { it == uri })
        }
    }

    fun removeAllImagesFromPageGroup(context: Context, groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val group = _currentPageGroups.value.find { it.id == groupId }
            group?.imageUris?.forEach { uri ->
                deleteLocalImage(uri)
            }
        }
        updatePageGroup(context, groupId) { it.copy(imageUris = emptyList()) }
    }

    fun copyAndAddImagesToPageGroup(context: Context, uriStrings: List<String>, groupId: String) {
        viewModelScope.launch {
            val permanentPaths = uriStrings.mapNotNull { copyUriToInternalStorage(context, it) }
            if (permanentPaths.isNotEmpty()) {
                updatePageGroup(groupId) { group ->
                    group.copy(imageUris = group.imageUris + permanentPaths)
                }
                saveProject(context)
            }
        }
    }

    fun saveCoverConfigAndProcessImage(context: Context, coverConfig: CoverPageConfig) {
        viewModelScope.launch {
            val imageUri = coverConfig.mainImageUri
            var finalConfig = coverConfig

            if (imageUri != null && imageUri.startsWith("content://")) {
                val permanentPath = copyUriToInternalStorage(context, imageUri)
                finalConfig = coverConfig.copy(mainImageUri = permanentPath)
            }

            val oldImageUri = _currentCoverConfig.value.mainImageUri
            if (oldImageUri != null && oldImageUri != finalConfig.mainImageUri) {
                deleteLocalImage(oldImageUri)
            }

            updateCoverConfig(finalConfig)
            saveProject(context)
        }
    }

    private suspend fun copyUriToInternalStorage(context: Context, uriString: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(uriString))
                val newFile = File(context.applicationContext.filesDir, "images/${UUID.randomUUID()}.jpg")
                newFile.parentFile?.mkdirs()
                val outputStream = FileOutputStream(newFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                Uri.fromFile(newFile).toString()
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error copying URI to internal storage", e)
                null
            }
        }
    }


    // --- Lógica de Guardado y Carga de Proyecto ---
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val gson = Gson()
    private val projectFileName = "last_project.json"

    fun saveProject(context: Context) {
        viewModelScope.launch {
            val serializableState = SerializableProjectState(
                coverConfig = _currentCoverConfig.value.toSerializable(),
                pageGroups = _currentPageGroups.value.map { it.toSerializable() },
                sunatData = _sunatData.value,
                themeName = _themeName.value,
                imageEffectSettings = _imageEffectSettings.value
            )
            val jsonString = gson.toJson(serializableState)
            val sizeInBytes = jsonString.toByteArray().size.toLong()
            val sizeLimitBytes = 50 * 1024 * 1024 // 50MB

            if (sizeInBytes > sizeLimitBytes) {
                _saveState.value = SaveState.RequiresConfirmation(sizeInBytes)
            } else {
                writeJsonToFile(context, jsonString)
            }
        }
    }

    fun forceSaveProject(context: Context) {
        viewModelScope.launch {
            val serializableState = SerializableProjectState(
                coverConfig = _currentCoverConfig.value.toSerializable(),
                pageGroups = _currentPageGroups.value.map { it.toSerializable() },
                sunatData = _sunatData.value,
                themeName = _themeName.value,
                imageEffectSettings = _imageEffectSettings.value
            )
            val jsonString = gson.toJson(serializableState)
            writeJsonToFile(context, jsonString)
        }
    }

    private suspend fun writeJsonToFile(context: Context, jsonString: String) {
        withContext(Dispatchers.IO) {
            try {
                context.applicationContext.openFileOutput(projectFileName, Context.MODE_PRIVATE).use {
                    it.write(jsonString.toByteArray())
                }
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error saving project", e)
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadProject(context: Context) {
        viewModelScope.launch {
            try {
                Log.d("ProjectViewModel", "loadProject: Attempting to load project...")
                val file = File(context.applicationContext.filesDir, projectFileName)

                if (!file.exists()) {
                    Log.d("ProjectViewModel", "loadProject: No project file found. Starting fresh.")
                    return@launch
                }

                Log.d("ProjectViewModel", "loadProject: Project file exists. Reading...")
                val jsonString = withContext(Dispatchers.IO) {
                    try {
                        context.applicationContext.openFileInput(projectFileName).bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        Log.e("ProjectViewModel", "loadProject: Error reading file.", e)
                        null
                    }
                }

                if (jsonString != null) {
                    Log.d("ProjectViewModel", "loadProject: File read success. Parsing JSON...")
                    val serializableState = gson.fromJson(jsonString, SerializableProjectState::class.java)
                    Log.d("ProjectViewModel", "loadProject: JSON parsing success. Mapping to domain...")
                    val projectState = serializableState.toDomain()
                    _currentCoverConfig.value = projectState.coverConfig
                    _currentPageGroups.value = projectState.pageGroups
                    _sunatData.value = projectState.sunatData
                    _themeName.value = projectState.themeName
                    _imageEffectSettings.value = projectState.imageEffectSettings
                    Log.d("ProjectViewModel", "loadProject: Project loaded and state restored successfully.")
                }
            } catch (t: Throwable) {
                Log.e("ProjectViewModel", "loadProject: A critical error occurred during project load. Starting fresh.", t)
                _saveState.value = SaveState.Error("Fallo al cargar el proyecto guardado: ${t.javaClass.simpleName}")
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
