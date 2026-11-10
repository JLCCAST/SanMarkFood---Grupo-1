package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCrearPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PedidoCreado
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import javax.inject.Inject

class CrearPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository,
    private val carritoRepository: CarritoRepository,
    private val ubicarDireccionEntrega: UbicarDireccionEntregaUseCase,
) {
    suspend operator fun invoke(
        modalidad: Modalidad,
        metodoPago: MetodoPago,
        direccion: Direccion? = null,
    ): PedidoCreado {
        val carrito = carritoRepository.leerCarrito()?.takeIf { it.items.isNotEmpty() }
            ?: throw ErrorCrearPedido.DatosInvalidos
        val entrega = if (modalidad == Modalidad.DELIVERY) {
            ubicarDireccionEntrega(direccion ?: throw ErrorCrearPedido.SinDireccion)
        } else {
            null
        }

        val pedido = pedidosRepository.crearPedido(carrito, modalidad, metodoPago, entrega)
        carritoRepository.vaciar()
        return pedido
    }
}
