package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.remote.ApiClient
import com.example.dynamiccollage.remote.SunatData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// A sealed class to represent the state of the API call
sealed class SunatDataState {
    object Idle : SunatDataState()
    object Loading : SunatDataState()
    data class Success(val data: SunatData) : SunatDataState()
    data class Error(val message: String) : SunatDataState()
}

class SunatDataViewModel : ViewModel() {

    private val _sunatDataState = MutableStateFlow<SunatDataState>(SunatDataState.Idle)
    val sunatDataState: StateFlow<SunatDataState> = _sunatDataState

    fun getSunatData(documentType: String, documentNumber: String) {
        viewModelScope.launch {
            _sunatDataState.value = SunatDataState.Loading
            try {
                val data = if (documentType == "DNI") {
                    ApiClient.instance.getDniData(documentNumber)
                } else {
                    ApiClient.instance.getRucData(documentNumber)
                }
                _sunatDataState.value = SunatDataState.Success(data)
            } catch (e: Exception) {
                _sunatDataState.value = SunatDataState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun resetState() {
        _sunatDataState.value = SunatDataState.Idle
    }
}
