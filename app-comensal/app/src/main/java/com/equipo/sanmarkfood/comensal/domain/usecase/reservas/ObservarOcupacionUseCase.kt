package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarOcupacionUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository,
) {
    operator fun invoke(restauranteId: String, fecha: String): Flow<Map<String, Int>> =
        reservasRepository.observarOcupacion(restauranteId, fecha)
}
