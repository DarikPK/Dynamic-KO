package pe.pixelcollage.app.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface SunatApiService {
    @GET("ruc")
    suspend fun getRucData(@Query("numero") ruc: String): RucData

    @GET("dni")
    suspend fun getDniData(@Query("numero") dni: String): DniData
}

data class RucData(
    val nombre: String,
    val numeroDocumento: String,
    val tipoDocumento: String,
    val estado: String,
    val condicion: String,
    val direccion: String,
    val ubigeo: String,
    val viaTipo: String,
    val viaNombre: String,
    val zonaCodigo: String,
    val zonaTipo: String,
    val numero: String,
    val interior: String,
    val lote: String,
    val dpto: String,
    val manzana: String,
    val kilometro: String,
    val distrito: String,
    val error: String? = null
)

data class DniData(
    val nombre: String,
    val tipoDocumento: String,
    val numeroDocumento: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val nombres: String,
    val error: String? = null
)
