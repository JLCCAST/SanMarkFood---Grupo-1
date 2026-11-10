package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.BuildConfig
import com.equipo.sanmarkfood.comensal.data.remote.GeocodingApi
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorUbicacion
import com.equipo.sanmarkfood.comensal.domain.repository.GeocodificacionRepository
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GeocodificacionRepositoryImpl @Inject constructor(
    private val api: GeocodingApi,
) : GeocodificacionRepository {

    override suspend fun coordenadasDe(direccion: String): Coordenadas {
        if (BuildConfig.GEOCODING_API_KEY.isBlank()) throw ErrorUbicacion.Desconocido
        val respuesta = try {
            api.geocodificar(direccion = direccion, clave = BuildConfig.GEOCODING_API_KEY)
        } catch (e: IOException) {
            throw ErrorUbicacion.SinConexion
        } catch (e: HttpException) {
            throw ErrorUbicacion.Desconocido
        } catch (e: SerializationException) {
            throw ErrorUbicacion.Desconocido
        }
        val lugar = respuesta.results.firstOrNull()
        return when {
            respuesta.status == ESTADO_OK && lugar != null ->
                Coordenadas(latitud = lugar.geometry.location.lat, longitud = lugar.geometry.location.lng)
            respuesta.status == ESTADO_SIN_RESULTADOS -> throw ErrorUbicacion.NoEncontrada
            else -> throw ErrorUbicacion.Desconocido
        }
    }

    private companion object {
        const val ESTADO_OK = "OK"
        const val ESTADO_SIN_RESULTADOS = "ZERO_RESULTS"
    }
}
