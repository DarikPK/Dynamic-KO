package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.data.model.User
import com.example.dynamiccollage.data.repository.AuthRepository
import com.example.dynamiccollage.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    init {
        checkUser()
    }

    fun checkUser() {
        viewModelScope.launch {
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

    fun logout() {
        authRepository.logout()
        _userState.value = UserState.Unauthenticated
    }
}

sealed class UserState {
    object Loading : UserState()
    data class Authenticated(val user: User) : UserState()
    object Unauthenticated : UserState()
    object Blocked : UserState()
}
