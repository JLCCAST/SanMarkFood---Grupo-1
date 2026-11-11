package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import javax.inject.Inject

class CambiarCantidadCarritoUseCase @Inject constructor(
    private val carritoRepository: CarritoRepository
) {
    suspend operator fun invoke(clave: String, cantidad: Int) {
        carritoRepository.cambiarCantidad(clave, cantidad.coerceAtMost(Carrito.MAX_CANTIDAD_POR_ITEM))
    }
}
