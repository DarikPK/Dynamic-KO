package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.PageGroup // Importar el modelo PageGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update // Importar update

class InnerPagesViewModel : ViewModel() {

    private val _pageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val pageGroups: StateFlow<List<PageGroup>> = _pageGroups.asStateFlow()

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    // Estado temporal para el grupo que se está creando/editando
    private val _editingGroup = MutableStateFlow<PageGroup?>(null) // Podría ser un PageGroup temporal o borrador
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

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
        _editingGroup.value = PageGroup() // Iniciar con un PageGroup por defecto, el ID se genera aquí
        _showCreateGroupDialog.value = true
    }

    fun onEditGroupClicked(groupToEdit: PageGroup) {
        _editingGroup.value = groupToEdit // Cargar el grupo existente para edición
        _showCreateGroupDialog.value = true
    }

    fun onDismissCreateGroupDialog() {
        _showCreateGroupDialog.value = false
        _editingGroup.value = null // Limpiar el grupo en edición
    }

    fun saveEditingGroup() {
        val groupToSave = _editingGroup.value ?: return
        // Verificar si el ID del grupo en edición ya existe en la lista de pageGroups
        val existingGroupIndex = _pageGroups.value.indexOfFirst { it.id == groupToSave.id }

        if (existingGroupIndex != -1) {
            // El grupo existe, actualizarlo
            _pageGroups.update { currentList ->
                currentList.toMutableList().apply { this[existingGroupIndex] = groupToSave }
            }
        } else {
            // El grupo es nuevo, añadirlo
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
    }
    fun onEditingGroupOrientationChange(orientation: com.example.dynamiccollage.data.model.PageOrientation) {
        _editingGroup.value = _editingGroup.value?.copy(orientation = orientation)
    }
    fun onEditingGroupPhotosPerSheetChange(count: Int) {
        _editingGroup.value = _editingGroup.value?.copy(photosPerSheet = count.coerceIn(1,2))
    }
    fun onEditingGroupSheetCountChange(countString: String) {
        val count = countString.toIntOrNull()?.coerceAtLeast(1) ?: _editingGroup.value?.sheetCount ?: 1
        _editingGroup.value = _editingGroup.value?.copy(sheetCount = count)
    }
    fun onEditingGroupOptionalTextChange(text: String) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(content = text)
        )
    }


    // Más estados y lógica:
    // - Lógica de validación de imágenes por grupo

    val areAllPhotoQuotasMet: StateFlow<Boolean> = pageGroups.map { groups ->
        if (groups.isEmpty()) true // Si no hay grupos, se considera válido para no bloquear
        else groups.all { it.isPhotoQuotaMet }
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)
}
