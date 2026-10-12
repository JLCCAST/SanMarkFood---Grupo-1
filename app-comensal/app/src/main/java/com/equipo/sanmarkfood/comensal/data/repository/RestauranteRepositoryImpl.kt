package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.CategoriaRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.MenuDelDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.RangoCarta
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.repository.RestauranteRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    override fun observarRestaurantesAprobados(): Flow<List<Restaurante>> = callbackFlow {
        // Mismo filtro que la consulta única: las reglas rechazan la lectura sin él.
        val registro = firestore.collection("restaurantes")
            .whereEqualTo("estado", "aprobado")
            .addSnapshotListener { instantanea, error ->
                if (error != null) {
                    close(error.aErrorDescubrimiento())
                    return@addSnapshotListener
                }
                trySend(instantanea?.documents.orEmpty().mapNotNull { it.aRestaurante() })
            }
        awaitClose { registro.remove() }
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
            totalResenas = (calificacion?.get("total") as? Number)?.toInt() ?: 0,
            menuHoy = (get("menuHoy") as? Map<*, *>)?.aMenuDelDia(),
            rangoCarta = (get("rangoCarta") as? Map<*, *>)?.aRangoCarta(),
            horario = (get("horario") as? Map<*, *>)?.aHorario().orEmpty()
        )
    }

    /** null si falta `fecha` o `precio`: sin esos dos no se puede usar el menú para filtrar. */
    private fun Map<*, *>.aMenuDelDia(): MenuDelDia? {
        val fecha = this["fecha"] as? String ?: return null
        val precio = (this["precio"] as? Number)?.toInt() ?: return null
        return MenuDelDia(
            fecha = fecha,
            precio = precio,
            horaFin = this["horaFin"] as? String ?: "",
            estado = EstadoMenuDelDia.desde(this["estado"] as? String)
        )
    }

    /** null si falta `min` o `max`. */
    private fun Map<*, *>.aRangoCarta(): RangoCarta? {
        val min = (this["min"] as? Number)?.toInt() ?: return null
        val max = (this["max"] as? Number)?.toInt() ?: return null
        return RangoCarta(min = min, max = max)
    }

    /** Lee `lun` a `dom`; un día sin `abre` o `cierra` se descarta y cuenta como sin horario. */
    private fun Map<*, *>.aHorario(): Map<String, HorarioDia> {
        val dias = mutableMapOf<String, HorarioDia>()
        for ((clave, valor) in this) {
            val dia = clave as? String ?: continue
            val datos = valor as? Map<*, *> ?: continue
            val abre = datos["abre"] as? String ?: continue
            val cierra = datos["cierra"] as? String ?: continue
            dias[dia] = HorarioDia(
                abierto = datos["abierto"] as? Boolean ?: false,
                abre = abre,
                cierra = cierra
            )
        }
        return dias
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