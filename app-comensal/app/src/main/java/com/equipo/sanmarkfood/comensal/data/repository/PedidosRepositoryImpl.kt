package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.remote.CrearPedidoDto
import com.equipo.sanmarkfood.comensal.data.remote.DireccionDto
import com.equipo.sanmarkfood.comensal.data.remote.FuncionesApi
import com.equipo.sanmarkfood.comensal.data.remote.ItemPedidoDto
import com.equipo.sanmarkfood.comensal.data.remote.llamarFuncion
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DireccionEntrega
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PedidoCreado
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PedidosRepositoryImpl @Inject constructor(
    private val api: FuncionesApi,
    private val json: Json,
) : PedidosRepository {

    override suspend fun crearPedido(
        carrito: Carrito,
        modalidad: Modalidad,
        metodoPago: MetodoPago,
        direccion: DireccionEntrega?,
    ): PedidoCreado = llamarFuncion(json) {
        val respuesta = api.crearPedido(
            CrearPedidoDto(
                restauranteUid = carrito.restauranteId,
                modalidad = modalidad.valor(),
                metodoPago = metodoPago.valor(),
                items = carrito.items.map {
                    ItemPedidoDto(tipo = "plato", cantidad = it.cantidad, platoId = it.platoId)
                },
                direccion = direccion
                    ?.takeIf { modalidad == Modalidad.DELIVERY }
                    ?.let { DireccionDto(it.texto, it.latitud, it.longitud) },
            )
        )
        PedidoCreado(pedidoId = respuesta.pedidoId, numero = respuesta.numero)
    }
}

private fun Modalidad.valor(): String = when (this) {
    Modalidad.RECOJO -> "recojo"
    Modalidad.DELIVERY -> "delivery"
}

private fun MetodoPago.valor(): String = when (this) {
    MetodoPago.BILLETERA -> "billetera"
    MetodoPago.TARJETA -> "tarjeta"
    MetodoPago.EFECTIVO -> "efectivo"
}