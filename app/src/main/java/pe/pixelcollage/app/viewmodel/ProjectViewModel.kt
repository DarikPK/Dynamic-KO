package pe.pixelcollage.app.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.pixelcollage.app.data.model.*
import pe.pixelcollage.app.data.toDomain
import pe.pixelcollage.app.data.toSerializable
import pe.pixelcollage.app.utils.PdfGenerator
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ProjectViewModel : ViewModel() {

    private val _currentCoverConfig = MutableStateFlow(CoverPageConfig())
    val currentCoverConfig: StateFlow<CoverPageConfig> = _currentCoverConfig.asStateFlow()

    private val _currentPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val currentPageGroups: StateFlow<List<PageGroup>> = _currentPageGroups.asStateFlow()

    // --- Draft system for Background Editing ---
    private val _draftGeneratedBackgroundConfig = MutableStateFlow<GeneratedBackgroundConfig?>(null)
    val draftGeneratedBackgroundConfig: StateFlow<GeneratedBackgroundConfig?> = _draftGeneratedBackgroundConfig.asStateFlow()
    private var isBackgroundEditingSessionActive = false

    fun startBackgroundEditingSession() {
        if (!isBackgroundEditingSessionActive) {
            _draftGeneratedBackgroundConfig.value = _currentCoverConfig.value.generatedBackgroundConfig
            isBackgroundEditingSessionActive = true
        }
    }

    fun saveBackgroundConfig(context: Context) {
        _currentCoverConfig.update { it.copy(generatedBackgroundConfig = _draftGeneratedBackgroundConfig.value) }
        isBackgroundEditingSessionActive = false
        saveProject(context)
    }

    fun discardBackgroundConfig() {
        _draftGeneratedBackgroundConfig.value = _currentCoverConfig.value.generatedBackgroundConfig
        isBackgroundEditingSessionActive = false
    }

    fun updateGeneratedBackgroundConfig(config: GeneratedBackgroundConfig?) {
        _draftGeneratedBackgroundConfig.value = config
    }
    // --- End of Draft system ---

    private val _sunatData = MutableStateFlow<SelectedSunatData?>(null)
    val sunatData: StateFlow<SelectedSunatData?> = _sunatData.asStateFlow()

    private val _sunatDataConsumed = MutableStateFlow(false)
    val sunatDataConsumed: StateFlow<Boolean> = _sunatDataConsumed.asStateFlow()

    private val _themeName = MutableStateFlow("Emerald Green")
    val themeName: StateFlow<String> = _themeName.asStateFlow()

    private val _imageEffectSettings = MutableStateFlow<Map<String, ImageEffectSettings>>(emptyMap())
    val imageEffectSettings: StateFlow<Map<String, ImageEffectSettings>> = _imageEffectSettings.asStateFlow()

    private val _draftImageEffectSettings = MutableStateFlow<Map<String, ImageEffectSettings>>(emptyMap())
    val draftImageEffectSettings: StateFlow<Map<String, ImageEffectSettings>> = _draftImageEffectSettings.asStateFlow()
    private var isEditingSessionActive = false

    fun startImageEditingSession() {
        if (!isEditingSessionActive) {
            _draftImageEffectSettings.value = _imageEffectSettings.value
            isEditingSessionActive = true
        }
    }

    fun saveImageEffects(context: Context) {
        _imageEffectSettings.value = _draftImageEffectSettings.value
        isEditingSessionActive = false
        saveProject(context)
    }

    fun discardImageEffects() {
        _draftImageEffectSettings.value = _imageEffectSettings.value
        isEditingSessionActive = false
    }

    private val _managerSelectedUri = MutableStateFlow<String?>(null)
    val managerSelectedUri: StateFlow<String?> = _managerSelectedUri.asStateFlow()

    private val _recycledUris = MutableStateFlow<List<String>>(emptyList())
    val recycledUris: StateFlow<List<String>> = _recycledUris.asStateFlow()

    private val _photoArrangement = MutableStateFlow<List<PhotoArrangementItem>>(emptyList())
    val photoArrangement: StateFlow<List<PhotoArrangementItem>> = _photoArrangement.asStateFlow()

    private val _isProjectLoaded = MutableStateFlow(false)
    val isProjectLoaded: StateFlow<Boolean> = _isProjectLoaded.asStateFlow()

    fun initializePhotoArrangement() {
        val arrangement = mutableListOf<PhotoArrangementItem>()
        var order = 1
        currentCoverConfig.value.mainImageUri?.let {
            arrangement.add(PhotoArrangementItem(it, order++, SheetType.SINGLE))
        }
        currentPageGroups.value.forEach { group ->
            val sheetType = if (group.photosPerSheet > 1) SheetType.DOUBLE else SheetType.SINGLE
            group.imageUris.forEach { uri ->
                arrangement.add(PhotoArrangementItem(uri, order++, sheetType))
            }
        }
        _photoArrangement.value = arrangement
    }

    fun setManagerSelectedUri(uri: String?) {
        _managerSelectedUri.value = uri
    }

    fun updateImageEffectSettings(uri: String, settings: ImageEffectSettings) {
        _draftImageEffectSettings.update { currentMap ->
            currentMap.toMutableMap().apply { this[uri] = settings }
        }
    }

    fun swapPhotoOrder(item1: PhotoArrangementItem, item2: PhotoArrangementItem) {
        _photoArrangement.update { currentList ->
            currentList.map {
                when (it.uri) {
                    item1.uri -> it.copy(order = item2.order)
                    item2.uri -> it.copy(order = item1.order)
                    else -> it
                }
            }
        }
    }

    fun movePhotoOrder(fromItem: PhotoArrangementItem, toItem: PhotoArrangementItem) {
        _photoArrangement.update { currentList ->
            val fromOrder = fromItem.order
            val toOrder = toItem.order
            val newList = currentList.toMutableList()
            val itemToMove = newList.find { it.order == fromOrder }
            if (itemToMove != null) {
                if (fromOrder < toOrder) {
                    newList.filter { it.order > fromOrder && it.order <= toOrder }.forEach { it.order-- }
                } else {
                    newList.filter { it.order >= toOrder && it.order < fromOrder }.forEach { it.order++ }
                }
                itemToMove.order = toOrder
            }
            newList
        }
    }

    fun saveArrangement(context: Context) {
        val sortedArrangement = _photoArrangement.value.sortedBy { it.order }
        val newCoverUri = sortedArrangement.firstOrNull()?.uri
        _currentCoverConfig.update { it.copy(mainImageUri = newCoverUri) }

        val innerPhotos = sortedArrangement.drop(if (newCoverUri != null) 1 else 0)
        val newPageGroups = mutableListOf<PageGroup>()
        var i = 0
        while (i < innerPhotos.size) {
            val currentPhoto = innerPhotos[i]
            if (currentPhoto.sheetType == SheetType.SINGLE) {
                newPageGroups.add(
                    PageGroup(
                        id = UUID.randomUUID().toString(),
                        groupName = "Grupo ${newPageGroups.size + 1}",
                        orientation = PageOrientation.Vertical,
                        photosPerSheet = 1,
                        sheetCount = 1,
                        imageUris = listOf(currentPhoto.uri)
                    )
                )
                i++
            } else {
                val groupUris = mutableListOf(currentPhoto.uri)
                if (i + 1 < innerPhotos.size && innerPhotos[i + 1].sheetType == SheetType.DOUBLE) {
                    groupUris.add(innerPhotos[i + 1].uri)
                    i++
                }
                newPageGroups.add(
                    PageGroup(
                        id = UUID.randomUUID().toString(),
                        groupName = "Grupo ${newPageGroups.size + 1}",
                        orientation = PageOrientation.Vertical,
                        photosPerSheet = 2,
                        sheetCount = 1,
                        imageUris = groupUris
                    )
                )
                i++
            }
        }
        _currentPageGroups.value = newPageGroups
        saveProject(context)
    }

    fun deletePhoto(context: Context, uri: String) {
        _recycledUris.update { it + uri }
        if (_currentCoverConfig.value.mainImageUri == uri) {
            _currentCoverConfig.update { it.copy(mainImageUri = null) }
        } else {
            _currentPageGroups.update { currentList ->
                currentList.map { group ->
                    if (group.imageUris.contains(uri)) {
                        group.copy(imageUris = group.imageUris.filterNot { it == uri })
                    } else {
                        group
                    }
                }
            }
        }
        setManagerSelectedUri(null)
        saveProject(context)
    }

    fun restorePhoto(context: Context, uri: String) {
        _recycledUris.update { it.filterNot { it == uri } }
        if (_currentCoverConfig.value.mainImageUri == null) {
            _currentCoverConfig.update { it.copy(mainImageUri = uri) }
        } else {
            _currentPageGroups.update { currentList ->
                val list = currentList.toMutableList()
                if (list.isNotEmpty()) {
                    val firstGroup = list[0]
                    list[0] = firstGroup.copy(imageUris = firstGroup.imageUris + uri)
                }
                list.toList()
            }
        }
        saveProject(context)
    }

    fun deletePhotoPermanently(context: Context, uri: String) {
        _recycledUris.update { it.filterNot { it == uri } }
        viewModelScope.launch(Dispatchers.IO) { deleteLocalImage(uri) }
        _imageEffectSettings.update { it - uri }
        _currentCoverConfig.update { it.copy(imageBorderSettingsMap = it.imageBorderSettingsMap - uri) }
        saveProject(context)
    }

    fun updateImageRotation(uri: String, degrees: Float) {
        _draftImageEffectSettings.update { currentMap ->
            val currentSettings = currentMap[uri] ?: ImageEffectSettings()
            currentMap.toMutableMap().apply { this[uri] = currentSettings.copy(rotationDegrees = degrees) }
        }
    }

    fun updateImageCrop(uri: String, newRelativeCrop: SerializableNormalizedRectF?) {
        _draftImageEffectSettings.update { currentMap ->
            val currentSettings = currentMap[uri] ?: ImageEffectSettings()
            val existingCrop = currentSettings.cropRect
            val finalCrop = if (existingCrop != null && newRelativeCrop != null) {
                SerializableNormalizedRectF(
                    left = existingCrop.left + newRelativeCrop.left * existingCrop.width,
                    top = existingCrop.top + newRelativeCrop.top * existingCrop.height,
                    width = existingCrop.width * newRelativeCrop.width,
                    height = existingCrop.height * newRelativeCrop.height
                )
            } else {
                newRelativeCrop
            }
            currentMap.toMutableMap().apply { this[uri] = currentSettings.copy(cropRect = finalCrop) }
        }
    }

    fun resetImageTransforms(uri: String) {
        _draftImageEffectSettings.update { currentMap ->
            val currentSettings = currentMap[uri] ?: ImageEffectSettings()
            currentMap.toMutableMap().apply { this[uri] = currentSettings.copy(rotationDegrees = 0f, cropRect = null) }
        }
    }

    fun updateTheme(context: Context, newThemeName: String) {
        _themeName.value = newThemeName
        saveProject(context)
    }

    fun updateSunatData(context: Context, data: SelectedSunatData) {
        _sunatData.value = data
        _sunatDataConsumed.value = false // Marcar como no consumido
        saveProject(context)
    }

    fun consumeSunatData() {
        _sunatDataConsumed.value = true
    }

    fun updateCoverConfig(newConfig: CoverPageConfig) {
        _currentCoverConfig.value = newConfig
    }

    fun applyColorTheme(context: Context, theme: ColorTheme) {
        _currentCoverConfig.update { config ->
            config.copy(
                templateName = theme.name,
                clientNameStyle = config.clientNameStyle.copy(fontColor = theme.textColor, rowStyle = config.clientNameStyle.rowStyle.copy(border = config.clientNameStyle.rowStyle.border.copy(color = theme.borderColor))),
                rucStyle = config.rucStyle.copy(fontColor = theme.textColor, rowStyle = config.rucStyle.rowStyle.copy(backgroundColor = theme.rucBackgroundColor, border = config.rucStyle.rowStyle.border.copy(color = theme.borderColor))),
                subtitleStyle = config.subtitleStyle.copy(fontColor = theme.textColor, rowStyle = config.subtitleStyle.rowStyle.copy(border = config.subtitleStyle.rowStyle.border.copy(color = theme.borderColor)))
            )
        }
        _currentPageGroups.update { groups ->
            groups.map { group ->
                group.copy(optionalTextStyle = group.optionalTextStyle.copy(fontColor = theme.textColor, rowStyle = group.optionalTextStyle.rowStyle.copy(backgroundColor = theme.rucBackgroundColor, border = group.optionalTextStyle.rowStyle.border.copy(color = theme.borderColor))))
            }
        }
        saveProject(context)
    }

    fun updateForceFullResCover(context: Context, forceFullRes: Boolean) {
        _currentCoverConfig.update { it.copy(forceFullResCover = forceFullRes) }
        saveProject(context)
    }

    fun updateUseHybridPdfMode(context: Context, useHybrid: Boolean) {
        _currentCoverConfig.update { it.copy(useHybridPdfMode = useHybrid) }
        saveProject(context)
    }

    fun updateHybridCoverImageQuality(context: Context, quality: Int) {
        _currentCoverConfig.update { it.copy(hybridCoverImageQuality = quality) }
        saveProject(context)
    }

    fun updateHybridInnerImagesQuality(context: Context, quality: Int) {
        _currentCoverConfig.update { it.copy(hybridInnerImagesQuality = quality) }
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

    fun updatePageGroups(context: Context, newGroups: List<PageGroup>) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldUris = _currentPageGroups.value.flatMap { it.imageUris }.toSet()
            val newUris = newGroups.flatMap { it.imageUris }.toSet()
            val urisToDelete = oldUris - newUris
            urisToDelete.forEach { deleteLocalImage(it) }
        }
        _currentPageGroups.value = newGroups
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
        groupToDelete?.let { group ->
            if (group.imageUris.contains(_managerSelectedUri.value)) {
                _managerSelectedUri.value = null
            }
            viewModelScope.launch(Dispatchers.IO) { group.imageUris.forEach { uri -> deleteLocalImage(uri) } }
        }
        _currentPageGroups.update { currentList -> currentList.filterNot { it.id == groupId } }
        saveProject(context)
    }

    fun resetPageGroups(context: Context) {
        _currentPageGroups.value = emptyList()
        saveProject(context)
    }

    fun resetProject(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val imagesDir = File(context.applicationContext.filesDir, "images")
                if (imagesDir.exists()) { imagesDir.deleteRecursively() }
                val projectFile = File(context.applicationContext.filesDir, projectFileName)
                if (projectFile.exists()) { projectFile.delete() }
            }
            _currentCoverConfig.value = CoverPageConfig()
            _currentPageGroups.value = emptyList()
            _sunatData.value = null
            _recycledUris.value = emptyList()
            _managerSelectedUri.value = null
            saveProject(context)
        }
    }

    private val _pdfGenerationState = MutableStateFlow<PdfGenerationState>(PdfGenerationState.Idle)
    val pdfGenerationState: StateFlow<PdfGenerationState> = _pdfGenerationState.asStateFlow()

    private val _pdfSize = MutableStateFlow(0L)
    val pdfSize: StateFlow<Long> = _pdfSize.asStateFlow()

    private val _pdfSizeMode = MutableStateFlow(1)
    val pdfSizeMode: StateFlow<Int> = _pdfSizeMode.asStateFlow()

    fun setPdfSizeMode(mode: Int) { _pdfSizeMode.value = mode }

    fun getFormattedPdfSize(): String {
        val size = _pdfSize.value
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "%.2f MB".format(size / (1024.0 * 1024.0))
        }
    }

    fun generatePdf(context: Context, fileName: String, userState: UserState) {
        val coverConfig = _currentCoverConfig.value
        val areInnerPagesEmpty = _currentPageGroups.value.all { it.imageUris.isEmpty() }
        val isCoverEmpty = coverConfig.clientNameStyle.content.isBlank() && coverConfig.rucStyle.content.isBlank() && coverConfig.subtitleStyle.content.isBlank() && coverConfig.mainImageUri == null

        if (isCoverEmpty && areInnerPagesEmpty) {
            _pdfGenerationState.value = PdfGenerationState.Error("No hay contenido para generar un PDF.")
            return
        }

        viewModelScope.launch {
            saveJob?.join()
            _pdfGenerationState.value = PdfGenerationState.Loading
            val generatedFile = withContext(Dispatchers.IO) {
                val generatedPages = pe.pixelcollage.app.utils.PdfContentManager.groupImagesForPdf(context, _currentPageGroups.value)
                PdfGenerator.generate(
                    context = context,
                    coverConfig = _currentCoverConfig.value,
                    generatedPages = generatedPages,
                    fileName = fileName.ifBlank { "DynamicCollage" },
                    imageEffectSettings = _imageEffectSettings.value,
                    userState = userState
                )
            }
            if (generatedFile != null) {
                _pdfSize.value = generatedFile.length()
                _pdfGenerationState.value = PdfGenerationState.Success(generatedFile)
            } else {
                if (userState is UserState.Authenticated && userState.user.role == "guest") {
                    _pdfGenerationState.value = PdfGenerationState.Error("Límite de 10 PDFs alcanzado.")
                } else {
                    _pdfGenerationState.value = PdfGenerationState.Error("No se pudo generar el PDF.")
                }
            }
        }
    }

    fun resetPdfGenerationState() { _pdfGenerationState.value = PdfGenerationState.Idle }

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

    fun resetShareableUri() { _shareablePdfUri.value = null }

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
                if (file.exists()) { file.delete() }
            }
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "Error deleting local image", e)
        }
    }

    fun removeImageFromPageGroup(context: Context, groupId: String, uri: String) {
        viewModelScope.launch(Dispatchers.IO) { deleteLocalImage(uri) }
        updatePageGroup(context, groupId) { group -> group.copy(imageUris = group.imageUris.filterNot { it == uri }) }
    }

    fun removeAllImagesFromPageGroup(context: Context, groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val group = _currentPageGroups.value.find { it.id == groupId }
            group?.imageUris?.forEach { uri -> deleteLocalImage(uri) }
        }
        updatePageGroup(context, groupId) { it.copy(imageUris = emptyList()) }
    }

    fun copyAndAddImagesToPageGroup(context: Context, uriStrings: List<String>, groupId: String) {
        viewModelScope.launch {
            val permanentPaths = uriStrings.mapNotNull { copyUriToInternalStorage(context, it) }
            if (permanentPaths.isNotEmpty()) {
                updatePageGroup(context, groupId) { group -> group.copy(imageUris = group.imageUris + permanentPaths) }
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

    suspend fun copyAndGetPermanentUri(context: Context, uriString: String): String? {
        return copyUriToInternalStorage(context, uriString)
    }

    private suspend fun copyUriToInternalStorage(context: Context, uriString: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(uriString))
                val newFile = File(context.applicationContext.filesDir, "images/${UUID.randomUUID()}.jpg")
                newFile.parentFile?.mkdirs()
                val outputStream = FileOutputStream(newFile)
                inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
                Uri.fromFile(newFile).toString()
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error copying URI to internal storage", e)
                null
            }
        }
    }

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val gson = Gson()
    private val projectFileName = "last_project.json"
    private var saveJob: Job? = null

    fun saveProject(context: Context) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val serializableState = SerializableProjectState(
                coverConfig = _currentCoverConfig.value.toSerializable(),
                pageGroups = _currentPageGroups.value.map { it.toSerializable() },
                sunatData = _sunatData.value,
                themeName = _themeName.value,
                imageEffectSettings = _imageEffectSettings.value,
                recycledUris = _recycledUris.value
            )
            val jsonString = gson.toJson(serializableState)
            writeJsonToFile(context, jsonString)
        }
    }

    fun forceSaveProject(context: Context) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val serializableState = SerializableProjectState(
                coverConfig = _currentCoverConfig.value.toSerializable(),
                pageGroups = _currentPageGroups.value.map { it.toSerializable() },
                sunatData = _sunatData.value,
                themeName = _themeName.value,
                imageEffectSettings = _imageEffectSettings.value,
                recycledUris = _recycledUris.value
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
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadProject(context: Context) {
        viewModelScope.launch {
            try {
                val file = File(context.applicationContext.filesDir, projectFileName)
                if (!file.exists()) { return@launch }
                val jsonString = withContext(Dispatchers.IO) {
                    try {
                        context.applicationContext.openFileInput(projectFileName).bufferedReader().use { it.readText() }
                    } catch (e: Exception) { null }
                }
                if (jsonString != null) {
                    val serializableState = gson.fromJson(jsonString, SerializableProjectState::class.java)
                    val projectState = serializableState.toDomain()
                    _currentCoverConfig.value = projectState.coverConfig
                    _currentPageGroups.value = projectState.pageGroups
                    _sunatData.value = projectState.sunatData
                    _sunatDataConsumed.value = true // Para evitar que se reaplique al cargar
                    _themeName.value = projectState.themeName
                    _imageEffectSettings.value = projectState.imageEffectSettings
                    _recycledUris.value = projectState.recycledUris
                }
            } catch (t: Throwable) {
                _saveState.value = SaveState.Error("Fallo al cargar el proyecto guardado: ${t.javaClass.simpleName}")
            } finally {
                _isProjectLoaded.value = true
            }
        }
    }

    fun resetSaveState() { _saveState.value = SaveState.Idle }
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
