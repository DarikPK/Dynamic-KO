package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.User
import com.example.dynamiccollage.data.repository.AuthRepository
import com.example.dynamiccollage.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountManagementViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _creationState = MutableStateFlow<CreationState>(CreationState.Idle)
    val creationState: StateFlow<CreationState> = _creationState

    fun createChildAccount(email: String, password: String, allowAutoLogin: Boolean) {
        viewModelScope.launch {
            _creationState.value = CreationState.Loading
            try {
                val adminUser = authRepository.getCurrentUser()
                if (adminUser == null) {
                    _creationState.value = CreationState.Error("No se ha podido identificar al administrador.")
                    return@launch
                }

                val newFirebaseUser = authRepository.createUser(email, password)
                if (newFirebaseUser != null) {
                    val newUser = User(
                        uid = newFirebaseUser.uid,
                        email = email,
                        role = "child",
                        parentId = adminUser.uid,
                        allow_auto_login = allowAutoLogin
                    )
                    userRepository.updateUser(newUser)
                    _creationState.value = CreationState.Success
                } else {
                    _creationState.value = CreationState.Error("No se pudo crear el usuario en Firebase.")
                }
            } catch (e: Exception) {
                _creationState.value = CreationState.Error(e.message ?: "Ocurrió un error desconocido.")
            }
        }
    }

    fun resetCreationState() {
        _creationState.value = CreationState.Idle
    }
}

sealed class CreationState {
    object Idle : CreationState()
    object Loading : CreationState()
    object Success : CreationState()
    data class Error(val message: String) : CreationState()
}
