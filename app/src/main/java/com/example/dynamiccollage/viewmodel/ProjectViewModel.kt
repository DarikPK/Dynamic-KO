package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProjectViewModel : ViewModel() {

    private val _currentCoverConfig = MutableStateFlow(DefaultCoverConfig.get()) // Usar DefaultCoverConfig.get()
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

    // --- Lógica de Guardado Explícito para Páginas Interiores ---
    private val _hasInnerPagesBeenSaved = MutableStateFlow(true)
    val hasInnerPagesBeenSaved: StateFlow<Boolean> = _hasInnerPagesBeenSaved.asStateFlow()

    fun confirmInnerPagesSaved(saved: Boolean) {
        _hasInnerPagesBeenSaved.value = saved
    }

    fun resetPageGroups() {
        _currentPageGroups.value = emptyList()
        // Al resetear los grupos, consideramos que el estado "guardado" se pierde
        // hasta que el usuario vuelva a guardar explícitamente.
        // O podríamos mantenerlo en true, dependiendo del flujo deseado.
        // Por ahora, lo mantenemos en true para no borrar al salir de la pantalla vacía.
        _hasInnerPagesBeenSaved.value = true
    }
    // --- Fin Lógica de Guardado Explícito ---

    fun resetProject() {
        _currentCoverConfig.value = CoverPageConfig()
        resetPageGroups() // Usar la nueva función para resetear grupos
    }
}
