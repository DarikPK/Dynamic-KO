package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.User
import com.example.dynamiccollage.data.repository.AuthRepository
import com.example.dynamiccollage.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ManageAccountsViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow<List<User>>(emptyList())
    val usersState: StateFlow<List<User>> = _usersState

    init {
        loadChildUsers()
    }

    private fun loadChildUsers() {
        viewModelScope.launch {
            val adminUser = authRepository.getCurrentUser()
            if (adminUser != null) {
                userRepository.getChildren(adminUser.uid).collect { users ->
                    _usersState.value = users
                }
            }
        }
    }

    fun toggleUserBlock(user: User, isEnabled: Boolean) {
        viewModelScope.launch {
            val updatedUser = user.copy(allow_auto_login = isEnabled)
            userRepository.updateUser(updatedUser)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                authRepository.deleteUser(user.uid)
            } catch (e: Exception) {
                // Manejar el error, por ejemplo, mostrando un Toast
                // En este caso, la lista se actualizará automáticamente si la eliminación es exitosa
                // y el usuario simplemente desaparecerá de la lista. Si falla, el usuario
                // permanecerá en la lista, lo que indica que algo salió mal.
            }
        }
    }
}
