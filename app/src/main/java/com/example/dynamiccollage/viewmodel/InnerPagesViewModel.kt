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

class InnerPagesViewModel : ViewModel() {

    private val _pageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
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

    fun addImageUrisToEditingGroup(uris: List<String>) {
        _editingGroup.value?.let { group ->
            val updatedUris = group.imageUris + uris
            _editingGroup.value = group.copy(imageUris = updatedUris)
        }
    }

    fun removeImageUriFromEditingGroup(uri: String) {
        _editingGroup.value?.let { group ->
            val updatedUris = group.imageUris.toMutableList().apply { remove(uri) }
            _editingGroup.value = group.copy(imageUris = updatedUris)
        }
    }

    fun saveEditingGroup() {
        viewModelScope.launch {
            _editingGroup.value?.let { groupToSave ->
                val currentList = _pageGroups.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == groupToSave.id }
                if (index != -1) {
                    currentList[index] = groupToSave
                } else {
                    currentList.add(groupToSave)
                }
                _pageGroups.value = currentList
                onDismissDialog()
            }
        }
    }
}
