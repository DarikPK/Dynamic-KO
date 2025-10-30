package pe.pixelcollage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pe.pixelcollage.app.data.model.User
import pe.pixelcollage.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ControlPanelViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _childrenState = MutableStateFlow<List<User>>(emptyList())
    val childrenState: StateFlow<List<User>> = _childrenState

    fun getChildren(parentId: String) {
        viewModelScope.launch {
            userRepository.getChildren(parentId).collect {
                _childrenState.value = it
            }
        }
    }

    fun updateChild(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
        }
    }
}
