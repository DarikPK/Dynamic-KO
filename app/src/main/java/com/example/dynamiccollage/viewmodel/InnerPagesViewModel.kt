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

class InnerPagesViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    // Ya no hay una lista local de grupos. Se observa directamente del ProjectViewModel.
    val pageGroups: StateFlow<List<PageGroup>> = projectViewModel.currentPageGroups

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
    val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages.asStateFlow()

    val isEditingGroupConfigValid: StateFlow<Boolean> = combine(
        editingGroup,
        projectViewModel.currentPageGroups
    ) { group, projectGroups ->
        if (group == null) {
            return@combine true
        }
        val isValidSheetCount = group.sheetCount > 0
        val originalGroup = projectGroups.find { it.id == group.id }
        val imagesAlreadyLoaded = originalGroup?.imageUris?.size ?: 0

        if (imagesAlreadyLoaded > 0) { // Es un grupo existente que se está editando
            isValidSheetCount && (group.totalPhotosRequired == imagesAlreadyLoaded)
        } else { // Es un grupo nuevo o uno existente sin imágenes
            isValidSheetCount
        }
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
        val isNewGroup = existingGroup == null

        if (isNewGroup) {
            projectViewModel.addPageGroupToProject(groupToSave)
        } else {
            projectViewModel.updatePageGroupInProject(groupToSave)
        }

        onDismissCreateGroupDialog()

        if (isNewGroup) {
            onAddImagesClickedForGroup(groupToSave.id)
        }
    }

    fun removePageGroup(groupId: String) {
        projectViewModel.removePageGroupFromProject(groupId)
    }

    fun onAddImagesClickedForGroup(groupId: String) {
        _currentGroupAddingImages.value = groupId
    }

    fun onImagesSelectedForGroup(uris: List<Uri>) {
        val groupId = _currentGroupAddingImages.value ?: return
        val groupToUpdate = pageGroups.value.find { it.id == groupId } ?: return

        val existingUris = groupToUpdate.imageUris.toSet()
        val newUrisToAdd = uris.map { it.toString() }.filterNot { existingUris.contains(it) }
        val updatedGroup = groupToUpdate.copy(imageUris = groupToUpdate.imageUris + newUrisToAdd)

        projectViewModel.updatePageGroupInProject(updatedGroup)
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

    val areAllPhotoQuotasMet: StateFlow<Boolean> = pageGroups.map { groups ->
        if (groups.isEmpty()) true
        else groups.all { it.isPhotoQuotaMet }
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)
}
