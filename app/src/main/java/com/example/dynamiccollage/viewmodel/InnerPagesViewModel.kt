package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.net.Uri

class InnerPagesViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    // Asumiendo que ProjectViewModel expone algo como esto. Si no, esto necesitará ajuste.
    private val _pageGroups = projectViewModel.currentPageGroups
    val pageGroups: StateFlow<List<PageGroup>> = _pageGroups.asStateFlow()

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
    val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages.asStateFlow()


    val isEditingGroupConfigValid: StateFlow<Boolean> = combine(editingGroup) { group ->
        group?.let { it.totalPhotosRequired >= it.imageUris.size } ?: true
    }

    fun onAddNewGroupClicked() {
        _editingGroup.value = PageGroup()
        _showCreateGroupDialog.value = true
    }

    fun onEditGroupClicked(group: PageGroup) {
        _editingGroup.value = group
        _showCreateGroupDialog.value = true
    }

    fun onAddImagesClickedForGroup(groupId: String) {
        _currentGroupAddingImages.value = groupId
    }

    fun onImagesSelectedForGroup(uris: List<android.net.Uri>, groupId: String) {
        val uriStrings = uris.map { it.toString() }
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris + uriStrings)
        }
        _currentGroupAddingImages.value = null // Reset after selection
    }

    fun removeImageFromGroup(groupId: String, uri: String) {
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris.toMutableList().apply { remove(uri) })
        }
    }

    fun removePageGroup(groupId: String) {
        projectViewModel.deletePageGroup(groupId)
    }

    fun onDismissCreateGroupDialog() {
        _editingGroup.value = null
        _showCreateGroupDialog.value = false
    }

    fun onEditingGroupNameChange(name: String) {
        _editingGroup.value = _editingGroup.value?.copy(groupName = name)
    }

    fun onEditingGroupOrientationChange(orientation: PageOrientation) {
        _editingGroup.value = _editingGroup.value?.copy(orientation = orientation)
    }

    fun onEditingGroupPhotosPerSheetChange(count: Int) {
        _editingGroup.value = _editingGroup.value?.copy(photosPerSheet = count)
    }

    fun onEditingGroupSheetCountChange(countStr: String) {
        val count = countStr.toIntOrNull() ?: 0
        _editingGroup.value = _editingGroup.value?.copy(sheetCount = count)
    }

    fun onEditingGroupOptionalTextChange(text: String) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(content = text)
        )
    }

    fun onEditingGroupImageSpacingChange(spacing: Float) {
        _editingGroup.value = _editingGroup.value?.copy(imageSpacing = spacing)
    }

    fun saveEditingGroup() {
        viewModelScope.launch {
            _editingGroup.value?.let { groupToSave ->
                // Distinguir entre crear y editar
                val currentGroups = _pageGroups.value
                if (currentGroups.any { it.id == groupToSave.id }) {
                    projectViewModel.updatePageGroup(groupToSave.id) { groupToSave }
                } else {
                    projectViewModel.addPageGroup(groupToSave)
                }
                onDismissCreateGroupDialog()
            }
        }
    }
}
