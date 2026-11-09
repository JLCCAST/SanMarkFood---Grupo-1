package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DireccionEntrega
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCrearPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PedidoCreado
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import javax.inject.Inject

class CrearPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository,
    private val carritoRepository: CarritoRepository,
) {
    suspend operator fun invoke(
        modalidad: Modalidad,
        metodoPago: MetodoPago,
        direccion: DireccionEntrega? = null,
    ): PedidoCreado {
        val carrito = carritoRepository.leerCarrito()?.takeIf { it.items.isNotEmpty() }
            ?: throw ErrorCrearPedido.DatosInvalidos
        if (modalidad == Modalidad.DELIVERY && direccion == null) throw ErrorCrearPedido.DatosInvalidos

        val pedido = pedidosRepository.crearPedido(carrito, modalidad, metodoPago, direccion)
        carritoRepository.vaciar()
        return pedido
    }
}