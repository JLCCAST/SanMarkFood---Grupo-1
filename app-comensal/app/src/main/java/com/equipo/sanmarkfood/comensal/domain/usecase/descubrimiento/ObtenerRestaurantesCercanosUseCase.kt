package com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.RestaurantesCercanos
import com.equipo.sanmarkfood.comensal.domain.repository.RestauranteRepository
import com.equipo.sanmarkfood.comensal.domain.repository.UbicacionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class ObtenerRestaurantesCercanosUseCase @Inject constructor(
    private val restaurantes: RestauranteRepository,
    private val ubicaciones: UbicacionRepository
) {

    /** [usarUbicacion] es true cuando la app tiene el permiso de ubicación. */
    suspend operator fun invoke(usarUbicacion: Boolean): Result<RestaurantesCercanos> {
        val lista = restaurantes.obtenerRestaurantesAprobados().getOrElse {
            return Result.failure(it as? ErrorDescubrimiento ?: ErrorDescubrimiento.Desconocido)
        }

        val usuario = if (usarUbicacion) ubicaciones.obtenerUbicacionActual() else null
        return Result.success(ordenarPorDistancia(lista, usuario))
    }

    /**
     * Igual que [invoke], pero en vivo: la ubicación se obtiene una vez y la lista se
     * vuelve a emitir cada vez que cambia en Firestore. Si falla, el flujo termina con
     * un ErrorDescubrimiento.
     */
    fun observar(usarUbicacion: Boolean): Flow<RestaurantesCercanos> = flow {
        val usuario = if (usarUbicacion) ubicaciones.obtenerUbicacionActual() else null
        emitAll(
            restaurantes.observarRestaurantesAprobados()
                .map { lista -> ordenarPorDistancia(lista, usuario) }
        )
    }

    /** Calcula la distancia de cada local y ordena: con distancia primero, luego por nombre. */
    private fun ordenarPorDistancia(
        lista: List<Restaurante>,
        usuario: Coordenadas?
    ): RestaurantesCercanos {
        val conDistancia = lista.map { restaurante ->
            val local = restaurante.ubicacion
            val distancia = if (usuario != null && local != null) {
                distanciaEnMetros(usuario, local)
            } else {
                null
            }
            restaurante.copy(distanciaMetros = distancia)
        }

        val ordenada = conDistancia.sortedWith(
            compareBy<Restaurante> { it.distanciaMetros == null }
                .thenBy { it.distanciaMetros ?: 0 }
                .thenBy { it.nombre.lowercase() }
        )

        return RestaurantesCercanos(ordenada, conDistancia = usuario != null)
    }

    /** Distancia entre dos puntos sobre la esfera terrestre (fórmula de Haversine). */
    private fun distanciaEnMetros(a: Coordenadas, b: Coordenadas): Int {
        val radioTierra = 6_371_000.0
        val dLat = (b.latitud - a.latitud) * PI / 180.0
        val dLon = (b.longitud - a.longitud) * PI / 180.0
        val latA = a.latitud * PI / 180.0
        val latB = b.latitud * PI / 180.0

        val h = sin(dLat / 2) * sin(dLat / 2) +
                cos(latA) * cos(latB) * sin(dLon / 2) * sin(dLon / 2)
        return (radioTierra * 2 * atan2(sqrt(h), sqrt(1 - h))).roundToInt()
    }
}