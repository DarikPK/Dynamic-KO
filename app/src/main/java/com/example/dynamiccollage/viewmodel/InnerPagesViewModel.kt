package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InnerPagesViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    // Page Groups
    private val _pageGroups = projectViewModel.currentPageGroups
    val pageGroups: StateFlow<List<PageGroup>> = _pageGroups.asStateFlow()

    // Create/Edit Dialog
    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    val showCreateGroupDialog: StateFlow<Boolean> = _editingGroup.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    val isEditingGroupConfigValid: StateFlow<Boolean> = combine(editingGroup) { group ->
        group?.let { it.totalPhotosRequired >= it.imageUris.size } ?: true
    }

    // Adding Images
    private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
    val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages.asStateFlow()


    fun onAddNewGroupClicked() {
        _editingGroup.value = PageGroup()
    }

    fun onEditGroupClicked(group: PageGroup) {
        _editingGroup.value = group
    }

    fun onAddImagesClickedForGroup(groupId: String) {
        _currentGroupAddingImages.value = groupId
    }
    fun onImagesSelectedForGroup(uris: List<String>, groupId: String) {
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris + uris.map { it.toString() })
        }
        _currentGroupAddingImages.value = null
    }

    fun removeImageFromGroup(groupId: String, uri: String) {
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris.toMutableList().apply { remove(uri) })
        }
    }

    fun onDismissCreateGroupDialog() {
        _editingGroup.value = null
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
                // If it's a new group, add it. Otherwise, update it.
                val isNewGroup = pageGroups.value.none { it.id == groupToSave.id }
                if (isNewGroup) {
                    projectViewModel.addPageGroup(groupToSave)
                } else {
                    projectViewModel.updatePageGroup(groupToSave.id) { groupToSave }
                }
                onDismissCreateGroupDialog()
            }
        }
    }

    fun removePageGroup(groupId: String) {
        projectViewModel.deletePageGroup(groupId)
    }
}
