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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InnerPagesViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    // El ViewModel ahora observa directamente la lista del ProjectViewModel.
    // Esto es la fuente de verdad.
    val pageGroups: StateFlow<List<PageGroup>> = projectViewModel.currentPageGroups

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    val isEditingGroupConfigValid: StateFlow<Boolean> = editingGroup.map { group ->
        if (group == null) return@map true

        val isValidSheetCount = group.sheetCount > 0
        // La validación de cuota de fotos ya no es necesaria aquí para bloquear el guardado,
        // se maneja en la pantalla de carga de imágenes.
        isValidSheetCount
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)


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

        val existingGroup = pageGroups.value.find { it.id == groupToSave.id }

        if (existingGroup != null) {
            projectViewModel.updatePageGroupInProject(groupToSave)
        } else {
            projectViewModel.addPageGroupToProject(groupToSave)
        }

        onDismissCreateGroupDialog()
    }

    fun removePageGroup(groupId: String) {
        projectViewModel.removePageGroupFromProject(groupId)
    }

    // La lógica para la carga de imágenes permanece igual, pero opera sobre el grupo
    // que se encuentra en el ProjectViewModel.
    fun onAddImagesClickedForGroup(groupId: String) {
        // Esta función no es estrictamente necesaria en el ViewModel si la navegación
        // se maneja completamente en la UI, pero no hace daño tenerla por claridad.
    }

    fun onImagesSelectedForGroup(uris: List<Uri>, groupId: String) {
        val groupToUpdate = projectViewModel.currentPageGroups.value.find { it.id == groupId } ?: return

        val existingUris = groupToUpdate.imageUris.toSet()
        val newUrisToAdd = uris.map { it.toString() }.filterNot { existingUris.contains(it) }
        val updatedGroup = groupToUpdate.copy(imageUris = groupToUpdate.imageUris + newUrisToAdd)

        projectViewModel.updatePageGroupInProject(updatedGroup)
    }

    fun removeImageFromGroup(groupId: String, imageUri: String) {
        val groupToUpdate = projectViewModel.currentPageGroups.value.find { it.id == groupId } ?: return

        val updatedImageUris = groupToUpdate.imageUris.toMutableList().apply {
            remove(imageUri)
        }

        val updatedGroup = groupToUpdate.copy(imageUris = updatedImageUris)
        projectViewModel.updatePageGroupInProject(updatedGroup)
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

    val areAllPhotoQuotasMet: StateFlow<Boolean> = pageGroups.map { groups ->
        if (groups.isEmpty()) true
        else groups.all { it.isPhotoQuotaMet }
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)
}
