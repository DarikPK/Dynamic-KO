package pe.pixelcollage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _isPlayStoreMode = MutableStateFlow<Boolean?>(null)
    val isPlayStoreMode: StateFlow<Boolean?> = _isPlayStoreMode.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val appSettingsDoc = firestore.collection("app_settings").document("auth_mode")

    init {
        checkAuthMode()
    }

    fun checkAuthMode() {
        viewModelScope.launch {
            appSettingsDoc.get()
                .addOnSuccessListener { document ->
                    _isPlayStoreMode.value = if (document != null && document.exists()) {
                        document.getBoolean("isPlayStoreMode") ?: false
                    } else {
                        false
                    }
                }
                .addOnFailureListener {
                    _isPlayStoreMode.value = false
                }
        }
    }
}
