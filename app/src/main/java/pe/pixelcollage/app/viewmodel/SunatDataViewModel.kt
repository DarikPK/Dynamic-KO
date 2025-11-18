package pe.pixelcollage.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pe.pixelcollage.app.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import kotlinx.coroutines.launch

// Nueva clase de datos para la UI, desacoplada de los modelos de red.
data class SunatDisplayData(
    val nombre: String,
    val numeroDocumento: String
)

// A sealed class to represent the state of the API call
sealed class SunatDataState {
    object Idle : SunatDataState()
    object Loading : SunatDataState()
    data class Success(val data: SunatDisplayData) : SunatDataState() // Usa la nueva clase
    data class Error(val message: String) : SunatDataState()
}

class SunatDataViewModel : ViewModel() {

    private val _sunatDataState = MutableStateFlow<SunatDataState>(SunatDataState.Idle)
    val sunatDataState: StateFlow<SunatDataState> = _sunatDataState

    fun getSunatData(documentType: String, documentNumber: String) {
        viewModelScope.launch {
            _sunatDataState.value = SunatDataState.Loading
            try {
                // El resultado de la API se mapea a la nueva clase de datos.
                val displayData: SunatDisplayData = when (documentType) {
                    "DNI" -> {
                        val dniData = ApiClient.instance.getDniData(documentNumber)
                        if (dniData.error != null) {
                            throw Exception(dniData.error)
                        }
                        SunatDisplayData(dniData.nombre, dniData.numeroDocumento)
                    }
                    "RUC10" -> {
                        val dniNumber = documentNumber.substring(2, 10)
                        val dniData = ApiClient.instance.getDniData(dniNumber)
                        if (dniData.error != null) {
                            throw Exception(dniData.error)
                        }
                        SunatDisplayData(dniData.nombre, documentNumber)
                    }
                    else -> { // RUC20
                        val rucData = ApiClient.instance.getRucData(documentNumber)
                        if (rucData.error != null) {
                            throw Exception(rucData.error)
                        }
                        SunatDisplayData(rucData.nombre, rucData.numeroDocumento)
                    }
                }
                _sunatDataState.value = SunatDataState.Success(displayData)
            } catch (e: HttpException) {
                if (e.code() == 422) {
                    _sunatDataState.value = SunatDataState.Error("DNI o RUC no encontrado o inválido.")
                } else {
                    _sunatDataState.value = SunatDataState.Error("Error de red: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e("SunatDataViewModel", "Error fetching SUNAT data", e)
                _sunatDataState.value = SunatDataState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _sunatDataState.value = SunatDataState.Idle
    }
}
