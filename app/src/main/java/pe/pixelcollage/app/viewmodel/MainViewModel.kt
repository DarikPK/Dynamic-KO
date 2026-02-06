package pe.pixelcollage.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import pe.pixelcollage.app.data.model.User
import pe.pixelcollage.app.data.repository.AuthRepository
import pe.pixelcollage.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

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
                // Si el documento existe, usa su valor; si no, asume true por defecto.
                isPlayStoreMode = document?.getBoolean("isPlayStoreMode") ?: true
                onComplete()
            }
            .addOnFailureListener {
                // Si hay un error de lectura, asume true para asegurar el modo Play Store.
                isPlayStoreMode = true
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

    fun getDeviceId(): String {
        return authRepository.getDeviceId(getApplication())
    }

    fun updateRemainingPdfs() {
        viewModelScope.launch {
            try {
                val deviceId = authRepository.getDeviceId(getApplication())
                val pdfCount = authRepository.getPdfCount(deviceId)
                _remainingPdfs.value = (30 - pdfCount).coerceAtLeast(0)
            } catch (e: Exception) {
                // Si falla, se usa -1 para indicar "No disponible"
                _remainingPdfs.value = -1
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
