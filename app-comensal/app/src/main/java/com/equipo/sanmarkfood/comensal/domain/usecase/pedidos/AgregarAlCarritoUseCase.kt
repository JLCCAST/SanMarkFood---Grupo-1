package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ResultadoAgregarAlCarrito
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import javax.inject.Inject

class AgregarAlCarritoUseCase @Inject constructor(
    private val carritoRepository: CarritoRepository
) {
    suspend operator fun invoke(
        restauranteId: String,
        restauranteNombre: String,
        item: ItemCarrito,
        vaciarSiOtroLocal: Boolean = false
    ): ResultadoAgregarAlCarrito {
        var carrito = carritoRepository.leerCarrito()

        // Regla de SCRUM-93: el carrito es de un solo local.
        if (carrito != null && carrito.restauranteId != restauranteId) {
            if (!vaciarSiOtroLocal) return ResultadoAgregarAlCarrito.OtroLocal(carrito.restauranteNombre)
            carritoRepository.vaciar()
            carrito = null
        }

        val existente = carrito?.items?.firstOrNull { it.platoId == item.platoId }
        val cantidadFinal = (existente?.cantidad ?: 0) + item.cantidad
        val platosDistintos = carrito?.items?.size ?: 0
        val pasaElTope = cantidadFinal > Carrito.MAX_CANTIDAD_POR_PLATO ||
                (existente == null && platosDistintos >= Carrito.MAX_PLATOS_DISTINTOS)
        if (pasaElTope) return ResultadoAgregarAlCarrito.LimiteAlcanzado

        carritoRepository.agregar(restauranteId, restauranteNombre, item)
        return ResultadoAgregarAlCarrito.Agregado
    }
}