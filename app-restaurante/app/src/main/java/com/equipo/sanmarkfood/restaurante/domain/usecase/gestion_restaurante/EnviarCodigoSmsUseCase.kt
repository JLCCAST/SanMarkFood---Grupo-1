package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EventoVerificacion
import com.equipo.sanmarkfood.restaurante.domain.repository.VerificacionTelefonoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EnviarCodigoSmsUseCase @Inject constructor(
    private val verificacionTelefonoRepository: VerificacionTelefonoRepository
) {
    operator fun invoke(telefono: String, reenviar: Boolean): Flow<EventoVerificacion> {
        val normalizado = normalizarTelefono(telefono)
            ?: throw ErrorRestaurante.DatosInvalidos(setOf(CampoLocal.TELEFONO))
        return verificacionTelefonoRepository.enviarCodigo(normalizado, reenviar)
    }
}
