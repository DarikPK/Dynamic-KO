package pe.pixelcollage.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pe.pixelcollage.app.remote.ApiClient
import pe.pixelcollage.app.remote.DniData
import pe.pixelcollage.app.remote.RucData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import kotlinx.coroutines.launch

// La clase SunatDisplayData se elimina.

// El estado ahora contendrá un tipo 'Any' para aceptar RucData o DniData.
sealed class SunatDataState {
    object Idle : SunatDataState()
    object Loading : SunatDataState()
    data class Success(val data: Any) : SunatDataState()
    data class Error(val message: String) : SunatDataState()
}

class SunatDataViewModel : ViewModel() {

    private val _sunatDataState = MutableStateFlow<SunatDataState>(SunatDataState.Idle)
    val sunatDataState: StateFlow<SunatDataState> = _sunatDataState

    fun getSunatData(documentType: String, documentNumber: String) {
        viewModelScope.launch {
            _sunatDataState.value = SunatDataState.Loading
            try {
                // La variable 'data' ahora será de tipo 'Any'.
                val data: Any = when (documentType) {
                    "DNI" -> {
                        val dniData = ApiClient.instance.getDniData(documentNumber)
                        if (dniData.error != null) {
                            throw Exception(dniData.error)
                        }
                        dniData
                    }
                    "RUC10" -> {
                        val dniNumber = documentNumber.substring(2, 10)
                        val dniData = ApiClient.instance.getDniData(dniNumber)
                        if (dniData.error != null) {
                            throw Exception(dniData.error)
                        }
                        // Devuelve DniData pero el ViewModel lo tratará como Any.
                        DniData(
                            nombre = dniData.nombre,
                            tipoDocumento = dniData.tipoDocumento,
                            numeroDocumento = documentNumber,
                            apellidoPaterno = dniData.apellidoPaterno,
                            apellidoMaterno = dniData.apellidoMaterno,
                            nombres = dniData.nombres,
                            error = dniData.error
                        )
                    }
                    else -> { // RUC20
                        val rucData = ApiClient.instance.getRucData(documentNumber)
                        if (rucData.error != null) {
                            throw Exception(rucData.error)
                        }
                        rucData
                    }
                }
                // Se emite el objeto de datos completo.
                _sunatDataState.value = SunatDataState.Success(data)
            } catch (e: HttpException) {
                if (e.code() == 422) {
                    _sunatDataState.value = SunatDataState.Error("DNI o RUC no encontrado o inválido.")
                } else {
                    _sunatDataState.value = SunatDataState.Error("Error de red: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e("SunatDataViewModel", "Error fetching SUNAT data", e)
                val errorMessage = if (e is java.net.UnknownHostException) {
                    "Se requiere conexión a internet para esta función."
                } else {
                    e.message ?: "Error desconocido"
                }
                _sunatDataState.value = SunatDataState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _sunatDataState.value = SunatDataState.Idle
    }
}
