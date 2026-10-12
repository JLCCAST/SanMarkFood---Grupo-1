package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.perfil.Comensal
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
import kotlinx.coroutines.flow.Flow

interface ComensalRepository {
    suspend fun obtenerPerfil(uid: String): Result<Comensal>
    fun observarPerfil(uid: String): Flow<Comensal>
    suspend fun actualizarPerfil(uid: String, nombre: String, telefono: String): Result<Unit>
    suspend fun actualizarDirecciones(uid: String, direcciones: List<Direccion>): Result<Unit>
    suspend fun actualizarPreferenciasNotificacion(
        uid: String,
        notificarReservas: Boolean,
        notificarResenas: Boolean
    ): Result<Unit>

    /** Sube la foto de perfil; rutaLocal es la ubicación de la imagen en el teléfono. */
    suspend fun subirFoto(uid: String, rutaLocal: String): Result<String>
    suspend fun actualizarFoto(uid: String, fotoUrl: String): Result<Unit>
}