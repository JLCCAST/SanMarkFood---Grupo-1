package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.VerificacionTelefonoDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EventoVerificacion
import com.equipo.sanmarkfood.restaurante.domain.repository.VerificacionTelefonoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VerificacionTelefonoRepositoryImpl @Inject constructor(
    private val verificacionTelefonoDataSource: VerificacionTelefonoDataSource
) : VerificacionTelefonoRepository {

    override fun telefonoVerificado(): String? = verificacionTelefonoDataSource.telefonoVerificado()

    override fun enviarCodigo(telefono: String, reenviar: Boolean): Flow<EventoVerificacion> =
        verificacionTelefonoDataSource.enviarCodigo(telefono, reenviar)

    override suspend fun verificarCodigo(codigo: String) = verificacionTelefonoDataSource.verificarCodigo(codigo)
}
