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

class InnerPagesViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    private val _pageGroups = projectViewModel.currentPageGroups
    val pageGroups: StateFlow<List<PageGroup>> = _pageGroups.asStateFlow()

    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    val isEditingGroupConfigValid: StateFlow<Boolean> = combine(editingGroup) { group ->
        group?.let { it.totalPhotosRequired >= it.imageUris.size } ?: true
    }

    fun onAddGroup() {
        _editingGroup.value = PageGroup()
    }

    fun onEditGroup(group: PageGroup) {
        _editingGroup.value = group
    }

    fun onImagesSelectedForGroup(uris: List<String>, groupId: String) {
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris + uris.map { it.toString() })
        }
    }

    fun removeImageFromGroup(groupId: String, uri: String) {
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris.toMutableList().apply { remove(uri) })
        }
    }

    fun onDismissDialog() {
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
                projectViewModel.updatePageGroup(groupToSave.id) { groupToSave }
                onDismissDialog()
            }
        }
    }

    fun deleteGroup(groupId: String) {
        projectViewModel.deletePageGroup(groupId)
    }
}
