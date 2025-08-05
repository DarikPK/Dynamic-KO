package com.example.dynamiccollage.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface SunatApiService {
    @GET("ruc")
    suspend fun getRucData(@Query("numero") ruc: String): RucData

    @GET("dni")
    suspend fun getDniData(@Query("numero") dni: String): DniData
}

interface SunatData {
    val nombre: String
    val numeroDocumento: String
}

data class RucData(
    override val nombre: String,
    override val numeroDocumento: String,
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
    val distrito: String
) : SunatData

data class DniData(
    override val nombre: String,
    val tipoDocumento: String,
    override val numeroDocumento: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val nombres: String
) : SunatData
