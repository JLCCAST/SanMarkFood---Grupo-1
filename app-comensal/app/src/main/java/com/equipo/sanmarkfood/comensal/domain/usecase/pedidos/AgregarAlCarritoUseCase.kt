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

        if (carrito != null && carrito.restauranteId != restauranteId) {
            if (!vaciarSiOtroLocal) return ResultadoAgregarAlCarrito.OtroLocal(carrito.restauranteNombre)
            carritoRepository.vaciar()
            carrito = null
        }

        val cantidadActual = carrito?.cantidadDe(item.clave) ?: 0
        val itemsDistintos = carrito?.items?.size ?: 0
        val pasaElTope = cantidadActual + item.cantidad > Carrito.MAX_CANTIDAD_POR_ITEM ||
                (cantidadActual == 0 && itemsDistintos >= Carrito.MAX_ITEMS_DISTINTOS)
        if (pasaElTope) return ResultadoAgregarAlCarrito.LimiteAlcanzado

        carritoRepository.agregar(restauranteId, restauranteNombre, item)
        return ResultadoAgregarAlCarrito.Agregado
    }
}
