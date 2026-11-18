package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.firebase.RestaurantesDataSource
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.repository.RestauranteRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class RestauranteRepositoryImpl @Inject constructor(
    private val dataSource: RestaurantesDataSource
) : RestauranteRepository {

    override suspend fun obtenerRestaurantesAprobados(): Result<List<Restaurante>> =
        conErrorDescubrimiento { dataSource.obtenerAprobados() }

    override fun observarRestaurantesAprobados(): Flow<List<Restaurante>> =
        dataSource.observarAprobados()
            .catch { e -> throw e.aErrorDescubrimiento() }

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