package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.CategoriaRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.repository.RestauranteRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RestauranteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RestauranteRepository {

    override suspend fun obtenerRestaurantesAprobados(): Result<List<Restaurante>> =
        conErrorDescubrimiento {
            // Las reglas de Firestore solo dejan leer a un comensal los locales aprobados:
            // sin este filtro la consulta se rechaza.
            firestore.collection("restaurantes")
                .whereEqualTo("estado", "aprobado")
                .get()
                .await()
                .documents
                .mapNotNull { it.aRestaurante() }
        }

    private fun DocumentSnapshot.aRestaurante(): Restaurante? {
        val nombre = getString("nombre") ?: return null
        val punto = getGeoPoint("ubicacion")
        val calificacion = get("calificacion") as? Map<*, *>

        return Restaurante(
            id = id,
            nombre = nombre,
            direccion = getString("direccion").orEmpty(),
            categoria = CategoriaRestaurante.desde(getString("categoria")),
            ubicacion = punto?.let { Coordenadas(it.latitude, it.longitude) },
            portadaUrl = getString("portadaUrl").orEmpty(),
            logoUrl = getString("logoUrl").orEmpty(),
            pausado = getBoolean("pausado") ?: false,
            calificacionPromedio = (calificacion?.get("promedio") as? Number)?.toDouble(),
            totalResenas = (calificacion?.get("total") as? Number)?.toInt() ?: 0
        )
    }

    private suspend fun <T> conErrorDescubrimiento(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.aErrorDescubrimiento())
        }

    private fun Throwable.aErrorDescubrimiento(): ErrorDescubrimiento = when {
        this is ErrorDescubrimiento -> this
        this is FirebaseNetworkException -> ErrorDescubrimiento.SinConexion
        this is FirebaseFirestoreException &&
                code == FirebaseFirestoreException.Code.UNAVAILABLE -> ErrorDescubrimiento.SinConexion
        else -> ErrorDescubrimiento.Desconocido
    }
}