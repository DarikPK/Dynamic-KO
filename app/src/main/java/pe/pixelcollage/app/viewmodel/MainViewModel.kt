package pe.pixelcollage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pe.pixelcollage.app.data.model.User
import pe.pixelcollage.app.data.repository.AuthRepository
import pe.pixelcollage.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    private val _remainingPdfs = MutableStateFlow<Int?>(null)
    val remainingPdfs: StateFlow<Int?> = _remainingPdfs

    private var isPlayStoreMode: Boolean? = null

    init {
        fetchAuthMode {
            checkUser()
        }
    }

    private fun fetchAuthMode(onComplete: () -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val appSettingsDoc = firestore.collection("app_settings").document("auth_mode")
        appSettingsDoc.get()
            .addOnSuccessListener { document ->
                isPlayStoreMode = if (document != null && document.exists()) {
                    document.getBoolean("isPlayStoreMode") ?: false
                } else {
                    false
                }
                onComplete()
            }
            .addOnFailureListener {
                isPlayStoreMode = false
                onComplete()
            }
    }

    fun checkUser() {
        viewModelScope.launch {
            if (isPlayStoreMode == true) {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser == null || firebaseUser.isAnonymous) {
                    val anonymousUser = authRepository.signInAnonymously()
                    if (anonymousUser != null) {
                        val appUser = User(uid = anonymousUser.uid, role = "guest")
                        _userState.value = UserState.Authenticated(appUser)
                        updateRemainingPdfs() // Actualizar contador para usuario anónimo
                    } else {
                        _userState.value = UserState.Unauthenticated
                    }
                } else {
                    // Si ya hay un usuario anónimo, solo actualizamos el contador
                    updateRemainingPdfs()
                }
            } else {
                _remainingPdfs.value = null // Limpiar contador si no es modo Play Store
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    val appUser = userRepository.getUser(firebaseUser.uid)
                    if (appUser != null) {
                        if (appUser.locked) {
                            _userState.value = UserState.Blocked
                        } else if (appUser.role == "child" && !appUser.allow_auto_login) {
                            authRepository.logout()
                            _userState.value = UserState.Blocked
                        } else {
                            _userState.value = UserState.Authenticated(appUser)
                        }
                    } else {
                        _userState.value = UserState.Unauthenticated
                    }
                } else {
                    _userState.value = UserState.Unauthenticated
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _userState.value = UserState.Unauthenticated
    }

    private fun updateRemainingPdfs() {
        viewModelScope.launch {
            try {
                val installationId = authRepository.getInstallationId()
                val pdfCount = authRepository.getPdfCount(installationId)
                _remainingPdfs.value = (10 - pdfCount).coerceAtLeast(0)
            } catch (e: Exception) {
                // Manejar error si no se puede obtener el contador
                _remainingPdfs.value = null
            }
        }
    }
}

sealed class UserState {
    object Loading : UserState()
    data class Authenticated(val user: User) : UserState()
    object Unauthenticated : UserState()
    object Blocked : UserState()
}
