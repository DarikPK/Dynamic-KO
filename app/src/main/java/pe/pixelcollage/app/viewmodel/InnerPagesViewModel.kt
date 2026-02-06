package pe.pixelcollage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import pe.pixelcollage.app.data.model.PageGroup
import pe.pixelcollage.app.data.model.PageOrientation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class InnerPagesViewModel(private val projectViewModel: ProjectViewModel) : ViewModel() {

    var newGroupIsSmart by mutableStateOf(true)

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _pageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val pageGroups: StateFlow<List<PageGroup>> = _pageGroups.asStateFlow()

    private val _originalPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val originalPageGroups: StateFlow<List<PageGroup>> = _originalPageGroups.asStateFlow()


    init {
        viewModelScope.launch {
            projectViewModel.isProjectLoaded.first { it }
            projectViewModel.currentPageGroups.collect { groups ->
                // Sincronizamos si no hay cambios locales
                // O si el proyecto ha sido reseteado (ahora está vacío pero antes no lo estaba)
                val isReset = groups.isEmpty() && _originalPageGroups.value.isNotEmpty()
                if (!hasChanges.value || isReset) {
                    _pageGroups.value = groups
                    _originalPageGroups.value = groups.map { it.copy() }
                }
            }
        }
    }

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    private val _editingGroup = MutableStateFlow<PageGroup?>(null)
    val editingGroup: StateFlow<PageGroup?> = _editingGroup.asStateFlow()

    val hasChanges: StateFlow<Boolean> = combine(_pageGroups, _originalPageGroups) { current, original ->
        current != original
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _currentGroupAddingImages = MutableStateFlow<String?>(null)
    val currentGroupAddingImages: StateFlow<String?> = _currentGroupAddingImages.asStateFlow()

    val isEditingGroupConfigValid: StateFlow<Boolean> = editingGroup
        .map { group ->
            if (group == null) return@map true
            if (group.smartLayoutEnabled) {
                true // La cuota de fotos no aplica a grupos inteligentes en el diálogo de edición
            } else {
                group.totalPhotosRequired >= group.imageUris.size
            }
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
        val currentGroupCount = pageGroups.value.size
        val newGroup = PageGroup(
            groupName = "Grupo ${currentGroupCount + 1}",
            optionalTextStyle = coverConfig.subtitleStyle.copy(
                id = "pageGroupOptionalText",
                content = ""
            ),
            smartLayoutEnabled = newGroupIsSmart
        )
        _editingGroup.value = newGroup
        _showCreateGroupDialog.value = true
    }

    fun onEditGroupClicked(group: PageGroup) {
        _editingGroup.value = group
        _showCreateGroupDialog.value = true
    }

    fun setGroupAddingImages(groupId: String) {
        _currentGroupAddingImages.value = groupId
    }

    fun onImagesSelectedForGroup(context: android.content.Context, uris: List<Uri>, groupId: String) {
        val uriStrings = uris.map { it.toString() }
        var duplicatesFound = 0
        val originalGroup = _originalPageGroups.value.find { it.id == groupId }
        val existingUris = originalGroup?.imageUris?.toSet() ?: emptySet()

        val updatedGroups = _pageGroups.value.map {
            if (it.id == groupId) {
                val newUniqueUris = uriStrings.filter { uri ->
                    val isDuplicate = existingUris.contains(uri) ||
                            (uri.startsWith("content://") && projectViewModel.isUriAlreadyInProject(context, uri))
                    !isDuplicate
                }
                duplicatesFound = uriStrings.size - newUniqueUris.size
                it.copy(imageUris = it.imageUris + newUniqueUris)
            } else {
                it
            }
        }
        _pageGroups.value = updatedGroups


        if (duplicatesFound > 0) {
            viewModelScope.launch {
                _toastEvent.emit("Se han omitido $duplicatesFound imágenes duplicadas.")
            }
        }

        _currentGroupAddingImages.value = null
    }

    fun removeSingleImageFromGroup(groupId: String, uri: String) {
        val updatedGroups = _pageGroups.value.map {
            if (it.id == groupId) {
                it.copy(imageUris = it.imageUris - uri)
            } else {
                it
            }
        }
        _pageGroups.value = updatedGroups
    }

    fun removeImagesFromGroup(groupId: String) {
        val updatedGroups = _pageGroups.value.map {
            if (it.id == groupId) {
                it.copy(imageUris = emptyList())
            } else {
                it
            }
        }
        _pageGroups.value = updatedGroups
    }

    fun onRemoveGroupClicked(groupId: String) {
        _showDeleteGroupDialog.value = groupId
    }

    fun onConfirmRemoveGroup(context: android.content.Context) {
        _showDeleteGroupDialog.value?.let { groupId ->
            _pageGroups.value = _pageGroups.value.filterNot { it.id == groupId }
        }
        _showDeleteGroupDialog.value = null
    }

    fun onDismissRemoveGroupDialog() {
        _showDeleteGroupDialog.value = null
    }

    fun onRemoveImagesClicked(groupId: String) {
        _showDeleteImagesDialog.value = groupId
    }

    fun onConfirmRemoveImages() {
        _showDeleteImagesDialog.value?.let { groupId ->
            val updatedGroups = _pageGroups.value.map {
                if (it.id == groupId) {
                    it.copy(imageUris = emptyList())
                } else {
                    it
                }
            }
            _pageGroups.value = updatedGroups
        }
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
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(content = text.replace("\n", ""))
        )
    }

    fun onEditingGroupOptionalTextAllCapsChange(allCaps: Boolean) {
        _editingGroup.value = _editingGroup.value?.copy(
            optionalTextStyle = _editingGroup.value!!.optionalTextStyle.copy(allCaps = allCaps)
        )
    }

    fun onEditingGroupImageSpacingChange(spacingStr: String) {
        val spacing = spacingStr.toFloatOrNull() ?: 0f
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

    fun onEditingGroupHeaderStyleChange(newStyle: pe.pixelcollage.app.data.model.TextStyleConfig) {
        _editingGroup.value = _editingGroup.value?.copy(optionalTextStyle = newStyle)
    }

    fun saveEditingGroup() {
        viewModelScope.launch {
            _editingGroup.value?.let { groupToSave ->
                val currentGroups = _pageGroups.value
                if (currentGroups.any { it.id == groupToSave.id }) {
                    _pageGroups.value = currentGroups.map {
                        if (it.id == groupToSave.id) groupToSave else it
                    }
                } else {
                    _pageGroups.value = currentGroups + groupToSave
                }
                onDismissCreateGroupDialog()
            }
        }
    }

    fun saveProject(context: android.content.Context) {
        onSaveChanges(context)
        projectViewModel.saveProject(context)
    }

    fun onSaveChanges(context: android.content.Context) {
        viewModelScope.launch {
            val groupsWithPermanentUris = _pageGroups.value.map { group ->
                val permanentUris = group.imageUris.map { uri ->
                    if (uri.startsWith("content://")) {
                        projectViewModel.copyAndGetPermanentUri(context, uri) ?: uri
                    } else {
                        uri
                    }
                }
                group.copy(imageUris = permanentUris)
            }

            projectViewModel.updatePageGroups(context, groupsWithPermanentUris)
            _pageGroups.value = groupsWithPermanentUris
            _originalPageGroups.value = groupsWithPermanentUris.map { it.copy() }
        }
    }

    fun discardChanges() {
        _pageGroups.value = _originalPageGroups.value
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
