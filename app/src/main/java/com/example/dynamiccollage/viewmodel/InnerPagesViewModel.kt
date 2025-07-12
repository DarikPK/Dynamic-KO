package com.example.dynamiccollage.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// Este ViewModel ahora necesita el ProjectViewModel para comunicarse con él
class InnerPagesViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    // Lista LOCAL de grupos para la edición en esta pantalla
    private val _localPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val localPageGroups: StateFlow<List<PageGroup>> = _localPageGroups.asStateFlow()

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
    val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages.asStateFlow()

    val isEditingGroupConfigValid: StateFlow<Boolean> = combine(
        editingGroup,
        _localPageGroups // Validar contra la lista local
    ) { group, currentGroups ->
        if (group == null) return@combine true
        val isValidSheetCount = group.sheetCount > 0
        val originalGroup = currentGroups.find { it.id == group.id }
        val imagesAlreadyLoaded = originalGroup?.imageUris?.size ?: 0

        if (imagesAlreadyLoaded > 0) {
            isValidSheetCount && (group.totalPhotosRequired == imagesAlreadyLoaded)
        } else {
            isValidSheetCount
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    // Carga la lista desde el ProjectViewModel al iniciar
    fun loadGroupsFromProject() {
        _localPageGroups.value = projectViewModel.currentPageGroups.value
        // Al cargar, marcamos que no hay cambios sin guardar
        projectViewModel.confirmInnerPagesSaved(true)
    }

    // El botón "Guardar" en la UI llamará a esta función
    fun saveChangesToProject() {
        projectViewModel.setPageGroups(_localPageGroups.value)
        projectViewModel.confirmInnerPagesSaved(true)
    }

    private fun markChangesAsUnsaved() {
        projectViewModel.confirmInnerPagesSaved(false)
    }

    fun onAddNewGroupClicked() {
        _editingGroup.value = PageGroup()
        _showCreateGroupDialog.value = true
    }

    fun onEditGroupClicked(groupToEdit: PageGroup) {
        _editingGroup.value = groupToEdit
        _showCreateGroupDialog.value = true
    }

    fun onDismissCreateGroupDialog() {
        _showCreateGroupDialog.value = false
        _editingGroup.value = null
    }

    fun saveEditingGroup() {
        val groupToSave = _editingGroup.value ?: return
        if (!isEditingGroupConfigValid.value) return

        val existingGroupIndex = _localPageGroups.value.indexOfFirst { it.id == groupToSave.id }

        if (existingGroupIndex != -1) {
            _localPageGroups.update { currentList ->
                currentList.toMutableList().apply { this[existingGroupIndex] = groupToSave }
            }
        } else {
            _localPageGroups.update { currentList -> currentList + groupToSave }
        }
        markChangesAsUnsaved()
        onDismissCreateGroupDialog()
    }

    fun removePageGroup(groupId: String) {
        _localPageGroups.update { currentList ->
            currentList.filterNot { it.id == groupId }
        }
        markChangesAsUnsaved()
    }

    fun onAddImagesClickedForGroup(groupId: String) {
        _currentGroupAddingImages.value = groupId
    }

    fun onImagesSelectedForGroup(uris: List<Uri>) {
        val groupId = _currentGroupAddingImages.value ?: return

        _localPageGroups.update { currentList ->
            currentList.map { group ->
                if (group.id == groupId) {
                    val existingUris = group.imageUris.toSet()
                    val newUrisToAdd = uris.map { it.toString() }.filterNot { existingUris.contains(it) }
                    group.copy(imageUris = group.imageUris + newUrisToAdd)
                } else {
                    group
                }
            }
        }
        markChangesAsUnsaved()
        _currentGroupAddingImages.value = null
    }

    fun onEditingGroupNameChange(name: String) {
        _editingGroup.value = _editingGroup.value?.copy(groupName = name)
    }

    fun onEditingGroupOrientationChange(orientation: PageOrientation) {
        _editingGroup.value = _editingGroup.value?.copy(orientation = orientation)
    }

    fun onEditingGroupPhotosPerSheetChange(count: Int) {
        _editingGroup.value = _editingGroup.value?.copy(photosPerSheet = count.coerceIn(1, 2))
    }

    fun onEditingGroupSheetCountChange(countString: String) {
        val currentGroup = _editingGroup.value
        val newCount = countString.toIntOrNull()?.coerceAtLeast(1) ?: currentGroup?.sheetCount ?: 1
        if (currentGroup?.sheetCount != newCount) {
            _editingGroup.value = currentGroup?.copy(sheetCount = newCount)
        }
    }

    fun onEditingGroupOptionalTextChange(text: String) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(content = text)
        )
    }

    val areAllPhotoQuotasMet: StateFlow<Boolean> = _localPageGroups.map { groups ->
        if (groups.isEmpty()) true
        else groups.all { it.isPhotoQuotaMet }
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)
}
