package pe.pixelcollage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.pixelcollage.app.data.model.User
import pe.pixelcollage.app.data.repository.UserRepository

class ControlPanelViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _childrenState = MutableStateFlow<List<User>>(emptyList())
    val childrenState: StateFlow<List<User>> = _childrenState.asStateFlow()

    private val _playStoreModeState = MutableStateFlow(false)
    val playStoreModeState: StateFlow<Boolean> = _playStoreModeState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val appSettingsDoc = firestore.collection("app_settings").document("auth_mode")

    init {
        loadPlayStoreMode()
    }

    private fun loadPlayStoreMode() {
        viewModelScope.launch {
            appSettingsDoc.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _playStoreModeState.value = document.getBoolean("isPlayStoreMode") ?: false
                    }
                }
        }
    }

    fun setPlayStoreMode(isPlayStoreMode: Boolean) {
        viewModelScope.launch {
            val settings = hashMapOf("isPlayStoreMode" to isPlayStoreMode)
            appSettingsDoc.set(settings)
                .addOnSuccessListener {
                    _playStoreModeState.value = isPlayStoreMode
                }
        }
    }

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
