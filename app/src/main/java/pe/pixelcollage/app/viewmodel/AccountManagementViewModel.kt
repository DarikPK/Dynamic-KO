package pe.pixelcollage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountManagementViewModel : ViewModel() {

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
}
