package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import javax.inject.Inject

class QuitarDelCarritoUseCase @Inject constructor(
    private val carritoRepository: CarritoRepository
) {
    suspend operator fun invoke(clave: String) = carritoRepository.quitar(clave)
}
