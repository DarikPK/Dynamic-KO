package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.repository.AuthRepository
import com.example.dynamiccollage.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CreateAccountState {
    object Idle : CreateAccountState()
    object Loading : CreateAccountState()
    object Success : CreateAccountState()
    data class Error(val message: String) : CreateAccountState()
}

class CreateAccountViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.Idle)
    val createAccountState: StateFlow<CreateAccountState> = _createAccountState

    fun createAccount(nick: String, email: String, password: String) {
        viewModelScope.launch {
            _createAccountState.value = CreateAccountState.Loading
            try {
                val firebaseUser = authRepository.createAccount(email, password)
                if (firebaseUser != null) {
                    userRepository.createUser(firebaseUser.uid, nick, email)
                    _createAccountState.value = CreateAccountState.Success
                } else {
                    _createAccountState.value = CreateAccountState.Error("No se pudo crear el usuario.")
                }
            } catch (e: Exception) {
                _createAccountState.value = CreateAccountState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
