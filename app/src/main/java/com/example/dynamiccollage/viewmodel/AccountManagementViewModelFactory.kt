package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dynamiccollage.data.repository.AuthRepository
import com.example.dynamiccollage.data.repository.UserRepository

class AccountManagementViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountManagementViewModel(authRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
