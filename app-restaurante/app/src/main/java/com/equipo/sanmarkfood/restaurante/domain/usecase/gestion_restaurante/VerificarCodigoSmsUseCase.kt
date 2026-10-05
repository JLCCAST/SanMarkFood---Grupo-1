package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.LARGO_CODIGO_SMS
import com.equipo.sanmarkfood.restaurante.domain.repository.VerificacionTelefonoRepository
import javax.inject.Inject

class VerificarCodigoSmsUseCase @Inject constructor(
    private val verificacionTelefonoRepository: VerificacionTelefonoRepository
) {
    suspend operator fun invoke(codigo: String) {
        if (codigo.length != LARGO_CODIGO_SMS || codigo.any { it !in '0'..'9' }) {
            throw ErrorRestaurante.CodigoIncorrecto
        }
        verificacionTelefonoRepository.verificarCodigo(codigo)
    }
}
