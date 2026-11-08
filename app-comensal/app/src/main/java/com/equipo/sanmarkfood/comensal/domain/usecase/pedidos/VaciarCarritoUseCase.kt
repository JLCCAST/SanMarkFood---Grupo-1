package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import javax.inject.Inject

class VaciarCarritoUseCase @Inject constructor(
    private val carritoRepository: CarritoRepository
) {
    suspend operator fun invoke() = carritoRepository.vaciar()
}