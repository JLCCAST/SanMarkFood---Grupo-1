package com.equipo.sanmarkfood.comensal.data

import android.net.Uri
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
    suspend fun subirFoto(uid: String, uri: Uri): Result<String>
    suspend fun actualizarFoto(uid: String, fotoUrl: String): Result<Unit>
}