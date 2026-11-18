package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarReservasUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository,
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<List<Reserva>> {
        val uid = authRepository.currentUser?.uid ?: return flowOf(emptyList())
        return reservasRepository.observarReservas(uid).map { reservas ->
            reservas.sortedWith(compareByDescending<Reserva> { it.fecha }.thenByDescending { it.hora })
        }
    }
}
