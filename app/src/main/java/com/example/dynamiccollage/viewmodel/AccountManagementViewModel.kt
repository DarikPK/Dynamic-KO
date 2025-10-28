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

    fun createChildAccount(nick: String, email: String, password: String, allowAutoLogin: Boolean) {
        viewModelScope.launch {
            _creationState.value = CreationState.Loading
            try {
                authRepository.createChildUser(nick, email, password, allowAutoLogin)
                _creationState.value = CreationState.Success
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
