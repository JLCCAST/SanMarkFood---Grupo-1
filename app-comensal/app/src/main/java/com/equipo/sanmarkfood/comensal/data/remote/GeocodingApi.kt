package com.equipo.sanmarkfood.comensal.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("maps/api/geocode/json")
    suspend fun geocodificar(
        @Query("address") direccion: String,
        @Query("key") clave: String,
        @Query("components") componentes: String = "country:PE",
        @Query("language") idioma: String = "es",
    ): RespuestaGeocodingDto
}

@Serializable
data class RespuestaGeocodingDto(
    val status: String,
    val results: List<ResultadoGeocodingDto> = emptyList(),
)

@Serializable
data class ResultadoGeocodingDto(
    @SerialName("formatted_address") val direccion: String = "",
    val geometry: GeometriaDto,
)

@Serializable
data class GeometriaDto(
    val location: UbicacionDto,
)

@Serializable
data class UbicacionDto(
    val lat: Double,
    val lng: Double,
)
