package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class EnviarARevisionUseCase @Inject constructor(
    private val restauranteRepository: RestauranteRepository
) {
    suspend operator fun invoke(horario: Horario) {
        if (horario.dias.values.none { it.abierto }) throw ErrorRestaurante.NingunDiaAbierto
        val horasInvalidas = horario.dias.filterValues { it.abierto && it.cierra <= it.abre }.keys
        if (horasInvalidas.isNotEmpty()) throw ErrorRestaurante.HorasInvalidas(horasInvalidas)

        restauranteRepository.enviarARevision(horario)
    }
}
