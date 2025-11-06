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
                    // Si el documento existe, usa su valor; si no, asume true.
                    _isPlayStoreMode.value = document?.getBoolean("isPlayStoreMode") ?: true
                }
                .addOnFailureListener {
                    // Si hay un error de lectura (ej. permisos), asume true.
                    _isPlayStoreMode.value = true
                }
        }
    }
}
