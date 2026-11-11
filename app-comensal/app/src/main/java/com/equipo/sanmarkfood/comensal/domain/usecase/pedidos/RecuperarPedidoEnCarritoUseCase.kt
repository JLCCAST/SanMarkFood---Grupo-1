package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.fechaDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import javax.inject.Inject

class RecuperarPedidoEnCarritoUseCase @Inject constructor(
    private val carritoRepository: CarritoRepository,
) {
    suspend operator fun invoke(pedido: Pedido): Boolean {
        val actual = carritoRepository.leerCarrito()
        if (actual != null && actual.items.isNotEmpty()) return false
        val items = pedido.items
            .filterNot { pedido.seAgoto(it) }
            .mapNotNull { it.aItemCarrito() }
        items.forEach { carritoRepository.agregar(pedido.restauranteId, pedido.restauranteNombre, it) }
        return items.isNotEmpty()
    }

    private fun ItemPedido.aItemCarrito(): ItemCarrito? = when {
        platoId != null -> PlatoDelCarrito(
            platoId = platoId,
            nombre = nombre,
            precio = precio,
            cantidad = cantidad,
        )
        entrada != null && segundo != null -> MenuDelCarrito(
            fecha = fechaDeHoy(),
            entrada = entrada,
            segundo = segundo,
            precio = precio,
            cantidad = cantidad,
        )
        else -> null
    }
}
