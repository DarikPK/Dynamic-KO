package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    val pageGroups: StateFlow<List<PageGroup>> = projectViewModel.currentPageGroups

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
    val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages.asStateFlow()

    val isEditingGroupConfigValid: StateFlow<Boolean> = editingGroup
        .map { group ->
            group?.let { it.totalPhotosRequired >= it.imageUris.size } ?: true
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _showDeleteGroupDialog = MutableStateFlow<String?>(null)
    val showDeleteGroupDialog: StateFlow<String?> = _showDeleteGroupDialog.asStateFlow()

    private val _showDeleteImagesDialog = MutableStateFlow<String?>(null)
    val showDeleteImagesDialog: StateFlow<String?> = _showDeleteImagesDialog.asStateFlow()

    fun onAddNewGroupClicked() {
        val coverConfig = projectViewModel.currentCoverConfig.value
        val newGroup = PageGroup(
            optionalTextStyle = coverConfig.subtitleStyle.copy(
                id = "pageGroupOptionalText",
                content = ""
            )
        )
        _editingGroup.value = newGroup
        _showCreateGroupDialog.value = true
    }

    fun onEditGroupClicked(group: PageGroup) {
        _editingGroup.value = group
        _showCreateGroupDialog.value = true
    }

    fun onAddImagesClickedForGroup(groupId: String) {
        _currentGroupAddingImages.value = groupId
    }

    fun onImagesSelectedForGroup(uris: List<Uri>, groupId: String) {
        val uriStrings = uris.map { it.toString() }
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris + uriStrings)
        }
        _currentGroupAddingImages.value = null
    }

    fun removeSingleImageFromGroup(groupId: String, uri: String) {
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = group.imageUris.toMutableList().apply { remove(uri) })
        }
    }

    fun removeImagesFromGroup(groupId: String) {
        projectViewModel.updatePageGroup(groupId) { group ->
            group.copy(imageUris = emptyList())
        }
    }

    fun onRemoveGroupClicked(groupId: String) {
        _showDeleteGroupDialog.value = groupId
    }

    fun onConfirmRemoveGroup() {
        _showDeleteGroupDialog.value?.let { projectViewModel.deletePageGroup(it) }
        _showDeleteGroupDialog.value = null
    }

    fun onDismissRemoveGroupDialog() {
        _showDeleteGroupDialog.value = null
    }

    fun onRemoveImagesClicked(groupId: String) {
        _showDeleteImagesDialog.value = groupId
    }

    fun onConfirmRemoveImages() {
        _showDeleteImagesDialog.value?.let { removeImagesFromGroup(it) }
        _showDeleteImagesDialog.value = null
    }

    fun onDismissRemoveImagesDialog() {
        _showDeleteImagesDialog.value = null
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

    fun onEditingGroupOptionalTextAllCapsChange(allCaps: Boolean) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(allCaps = allCaps)
        )
    }

    fun onEditingGroupImageSpacingChange(spacing: Float) {
        _editingGroup.value = _editingGroup.value?.copy(imageSpacing = spacing)
    }

    fun onEditingGroupFontSizeChange(size: String) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(fontSize = size.toIntOrNull() ?: 0)
        )
    }

    fun onEditingGroupTextAlignChange(align: androidx.compose.ui.text.style.TextAlign) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(textAlign = align)
        )
    }

    fun onEditingGroupFontColorChange(color: androidx.compose.ui.graphics.Color) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(fontColor = color)
        )
    }

    fun saveEditingGroup() {
        viewModelScope.launch {
            _editingGroup.value?.let { groupToSave ->
                val currentGroups = pageGroups.value
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

// Factory para InnerPagesViewModel
class InnerPagesViewModelFactory(private val projectViewModel: ProjectViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InnerPagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InnerPagesViewModel(projectViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
