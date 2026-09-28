package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.RestaurantesDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class RestauranteRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val restaurantesDataSource: RestaurantesDataSource,
) : RestauranteRepository {

    override suspend fun obtener(): Restaurante? = restaurantesDataSource.leer(uid())

    override suspend fun guardarDatos(datos: DatosLocal) {
        val uid = uid()
        if (restaurantesDataSource.existeEnServidor(uid)) {
            restaurantesDataSource.actualizarDatos(uid, datos)
        } else {
            restaurantesDataSource.crear(uid, datos, EstadoRestaurante.BORRADOR)
        }
    }

    override suspend fun enviarARevision(horario: Horario) {
        val uid = uid()
        // Igual que al guardar los datos: la lectura al servidor avisa si no hay conexión antes de escribir.
        if (!restaurantesDataSource.existeEnServidor(uid)) throw ErrorRestaurante.Desconocido
        restaurantesDataSource.enviarARevision(uid, horario)
    }

    // Cada cuenta tiene un solo local, guardado en restaurantes/{uid}.
    private fun uid(): String = authDataSource.usuarioActual()?.uid ?: throw ErrorRestaurante.Desconocido
}
