package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class InnerPagesViewModel : ViewModel() { // No tomará ProjectViewModel en constructor por ahora

    private val _pageGroups = MutableStateFlow<List<PageGroup>>(emptyList()) // Estado local
    val pageGroups: StateFlow<List<PageGroup>> = _pageGroups.asStateFlow()

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    // Funciones para que la UI cargue el estado inicial y observe cambios del ProjectViewModel
    fun loadInitialPageGroups(initialGroups: List<PageGroup>) {
        _pageGroups.value = initialGroups
    }

    // Estado temporal para el grupo que se está creando/editando
    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

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

        _pageGroups.update { currentList ->
            currentList.map { group ->
                if (group.id == groupId) {
                    // Añadir nuevas URIs, evitando duplicados si ya existieran (aunque GetMultipleContents no debería dar duplicados de la misma selección)
                    val existingUris = group.imageUris.toSet()
                    val newUrisToAdd = uris.map { it.toString() }.filterNot { existingUris.contains(it) }
                    group.copy(imageUris = group.imageUris + newUrisToAdd)
                } else {
                    group
                }
            }
        }
        _currentGroupAddingImages.value = null // Resetear después de la selección
    }

    fun clearImagesForGroup(groupId: String) {
        _pageGroups.update { list ->
            list.map { if (it.id == groupId) it.copy(imageUris = emptyList()) else it }
        }
    }


    fun onAddNewGroupClicked() {
        _editingGroup.value = PageGroup() // Iniciar con un PageGroup por defecto
        _isEditingGroupConfigValid.value = true // Un grupo nuevo es inicialmente válido (hasta que se definan hojas)
        _showCreateGroupDialog.value = true
    }

    fun onEditGroupClicked(groupToEdit: PageGroup) {
        _editingGroup.value = groupToEdit
        validateEditingGroupConfig() // Validar al cargar para edición
        _showCreateGroupDialog.value = true
    }

    fun onDismissCreateGroupDialog() {
        _showCreateGroupDialog.value = false
        _editingGroup.value = null
        _isEditingGroupConfigValid.value = true // Resetear validación
    }

    fun saveEditingGroup() {
        if (!_isEditingGroupConfigValid.value) {
            // Opcionalmente, podrías tener un StateFlow para un mensaje de error específico
            // y no cerrar el diálogo, o permitir cerrar pero no guardar.
            // Por ahora, si el botón Guardar estuviera habilitado y se llama a esto,
            // se guardaría. La UI debería deshabilitar el botón Guardar.
            return
        }

        val groupToSave = _editingGroup.value ?: return
        val existingGroupIndex = _pageGroups.value.indexOfFirst { it.id == groupToSave.id }

        if (existingGroupIndex != -1) {
            _pageGroups.update { currentList ->
                currentList.toMutableList().apply { this[existingGroupIndex] = groupToSave }
            }
        } else {
            _pageGroups.update { currentList -> currentList + groupToSave }
        }
        onDismissCreateGroupDialog()
    }


    // addPageGroup y updatePageGroup pueden ser eliminadas si saveEditingGroup las reemplaza,
    // o mantenidas si se usan en otros lugares. Por ahora, las comentaré.
    /*
    private fun addPageGroup(newGroup: PageGroup) {
        _pageGroups.update { currentList -> currentList + newGroup }
    }

    fun updatePageGroup(updatedGroup: PageGroup) {
        _pageGroups.update { currentList ->
            currentList.map { if (it.id == updatedGroup.id) updatedGroup else it }
        }
    }
    */

    fun removePageGroup(groupId: String) {
        _pageGroups.update { currentList ->
            currentList.filterNot { it.id == groupId }
        }
    }

    // Funciones para actualizar _editingGroup mientras se configura en el diálogo
    fun onEditingGroupNameChange(name: String) {
        _editingGroup.value = _editingGroup.value?.copy(groupName = name)
        // El nombre no afecta la validación de cuota de fotos
    }
    fun onEditingGroupOrientationChange(orientation: PageOrientation) {
        _editingGroup.value = _editingGroup.value?.copy(orientation = orientation)
        validateEditingGroupConfig()
    }
    fun onEditingGroupPhotosPerSheetChange(count: Int) {
        _editingGroup.value = _editingGroup.value?.copy(photosPerSheet = count.coerceIn(1,2))
        validateEditingGroupConfig()
    }
    fun onEditingGroupSheetCountChange(countString: String) {
        val currentGroup = _editingGroup.value
        val newCount = countString.toIntOrNull()?.coerceAtLeast(1) ?: currentGroup?.sheetCount ?: 1
        if (currentGroup?.sheetCount != newCount) {
            _editingGroup.value = currentGroup?.copy(sheetCount = newCount)
            validateEditingGroupConfig()
        }
    }
    fun onEditingGroupOptionalTextChange(text: String) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(content = text)
        )
        // El texto opcional no afecta la validación de cuota de fotos
    }

    private fun validateEditingGroupConfig() {
        val group = _editingGroup.value
        if (group == null) {
            _isEditingGroupConfigValid.value = true // No hay grupo en edición, o es nuevo sin datos.
            return
        }

        // La validación principal es que el número de hojas sea > 0
        // Y si hay imágenes cargadas, la nueva config (totalPhotosRequired) debe ser igual al número de imágenes cargadas.
        // O, si no hay imágenes cargadas, la config es válida.
        val isValidSheetCount = group.sheetCount > 0
        val imagesAlreadyLoaded = _pageGroups.value.find { it.id == group.id }?.imageUris?.size ?: 0

        if (imagesAlreadyLoaded > 0) { // Solo validar cuota si ya hay imágenes (es decir, estamos editando)
            _isEditingGroupConfigValid.value = isValidSheetCount && (group.totalPhotosRequired == imagesAlreadyLoaded)
        } else { // Si es un grupo nuevo o un grupo sin imágenes aún, solo validar sheetCount
            _isEditingGroupConfigValid.value = isValidSheetCount
        }
    }

    val areAllPhotoQuotasMet: StateFlow<Boolean> = _pageGroups.map { groups ->
        if (groups.isEmpty()) true
        else groups.all { it.isPhotoQuotaMet }
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

}
