package com.example.dynamiccollage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dynamiccollage.remote.ApiClient
import com.example.dynamiccollage.remote.SunatData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
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
                val data: SunatData = if (documentType == "DNI") {
                    val dniData = ApiClient.instance.getDniData(documentNumber)
                    if (dniData.error != null) {
                        throw Exception(dniData.error)
                    }
                    dniData
                } else {
                    val rucData = ApiClient.instance.getRucData(documentNumber)
                    if (rucData.error != null) {
                        throw Exception(rucData.error)
                    }
                    rucData
                }
                _sunatDataState.value = SunatDataState.Success(data)
            } catch (e: HttpException) {
                if (e.code() == 422) {
                    _sunatDataState.value = SunatDataState.Error("DNI o RUC no encontrado o inválido.")
                } else {
                    _sunatDataState.value = SunatDataState.Error("Error de red: ${e.message()}")
                }
            } catch (e: Exception) {
                _sunatDataState.value = SunatDataState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _sunatDataState.value = SunatDataState.Idle
    }
}
