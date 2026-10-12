package com.equipo.sanmarkfood.comensal.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.repository.UbicacionRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class UbicacionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val contexto: Context
) : UbicacionRepository {

    @SuppressLint("MissingPermission")
    override suspend fun obtenerUbicacionActual(): Coordenadas? {
        if (!hayPermiso()) return null

        val cliente = LocationServices.getFusedLocationProviderClient(contexto)
        val fuenteCancelacion = CancellationTokenSource()

        return try {
            // Si el GPS no responde a tiempo, se usa la última ubicación conocida.
            val actual = withTimeoutOrNull(TIEMPO_LIMITE_MS) {
                cliente.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    fuenteCancelacion.token
                ).await()
            }
            val ubicacion = actual ?: cliente.lastLocation.await()

            ubicacion?.let { Coordenadas(it.latitude, it.longitude) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } finally {
            // Libera la petición pendiente si se agotó el tiempo.
            fuenteCancelacion.cancel()
        }
    }

    private fun hayPermiso(): Boolean =
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private companion object {
        const val TIEMPO_LIMITE_MS = 5_000L
    }
}