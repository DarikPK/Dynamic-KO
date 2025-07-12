package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class InnerPagesViewModelFactory(
    private val projectViewModel: ProjectViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InnerPagesViewModel::class.java)) {
            return InnerPagesViewModel(projectViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
