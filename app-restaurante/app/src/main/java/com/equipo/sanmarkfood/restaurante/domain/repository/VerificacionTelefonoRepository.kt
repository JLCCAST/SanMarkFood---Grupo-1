package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EventoVerificacion
import kotlinx.coroutines.flow.Flow

interface VerificacionTelefonoRepository {
    fun telefonoVerificado(): String?

    fun enviarCodigo(telefono: String, reenviar: Boolean): Flow<EventoVerificacion>

    suspend fun verificarCodigo(codigo: String)
}
