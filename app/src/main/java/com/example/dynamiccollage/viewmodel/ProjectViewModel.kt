package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProjectViewModel : ViewModel() {

    private val _currentCoverConfig = MutableStateFlow(CoverPageConfig())
    val currentCoverConfig: StateFlow<CoverPageConfig> = _currentCoverConfig.asStateFlow()

    private val _currentPageGroups = MutableStateFlow<List<PageGroup>>(emptyList())
    val currentPageGroups: StateFlow<List<PageGroup>> = _currentPageGroups.asStateFlow()

    fun updateCoverConfig(newConfig: CoverPageConfig) {
        _currentCoverConfig.value = newConfig
    }

    fun setPageGroups(groups: List<PageGroup>) {
        _currentPageGroups.value = groups
    }

    fun addPageGroupToProject(group: PageGroup) {
        _currentPageGroups.update { currentList -> currentList + group }
    }

    fun updatePageGroupInProject(updatedGroup: PageGroup) {
        _currentPageGroups.update { currentList ->
            currentList.map { if (it.id == updatedGroup.id) updatedGroup else it }
        }
    }

    fun removePageGroupFromProject(groupId: String) {
        _currentPageGroups.update { currentList ->
            currentList.filterNot { it.id == groupId }
        }
    }

    fun resetProject() {
        _currentCoverConfig.value = CoverPageConfig() // Restablecer a la configuración por defecto
        _currentPageGroups.value = emptyList()       // Vaciar la lista de grupos
    }
}
