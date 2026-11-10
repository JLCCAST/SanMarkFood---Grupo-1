package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.firebase.PedidosDataSource
import com.equipo.sanmarkfood.comensal.data.firebase.aErrorPedidos
import com.equipo.sanmarkfood.comensal.data.remote.ActualizarPedidoDto
import com.equipo.sanmarkfood.comensal.data.remote.CrearPedidoDto
import com.equipo.sanmarkfood.comensal.data.remote.DireccionDto
import com.equipo.sanmarkfood.comensal.data.remote.FuncionesApi
import com.equipo.sanmarkfood.comensal.data.remote.ItemPedidoDto
import com.equipo.sanmarkfood.comensal.data.remote.llamarFuncion
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DireccionEntrega
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCancelarPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PedidoCreado
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PedidosRepositoryImpl @Inject constructor(
    private val api: FuncionesApi,
    private val json: Json,
    private val dataSource: PedidosDataSource,
) : PedidosRepository {

    override fun observarPedido(pedidoId: String): Flow<Pedido?> =
        dataSource.observarPedido(pedidoId).catch { throw it.aErrorPedidos() }

    override suspend fun cancelarPedido(pedidoId: String) {
        llamarFuncion(json, ErrorCancelarPedido.SinConexion, ::errorDeCancelar) {
            api.actualizarPedido(ActualizarPedidoDto(pedidoId = pedidoId, accion = "cancelar"))
        }
    }

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
                items = carrito.items.map { it.aDto() },
                direccion = direccion
                    ?.takeIf { modalidad == Modalidad.DELIVERY }
                    ?.let { DireccionDto(it.texto, it.latitud, it.longitud) },
            )
        )
        PedidoCreado(pedidoId = respuesta.pedidoId, numero = respuesta.numero)
    }
}

private fun errorDeCancelar(codigo: String?): Exception = when (codigo) {
    "estado_invalido" -> ErrorCancelarPedido.YaAceptado
    else -> ErrorCancelarPedido.Desconocido
}

private fun ItemCarrito.aDto(): ItemPedidoDto = when (this) {
    is PlatoDelCarrito -> ItemPedidoDto(tipo = "plato", cantidad = cantidad, platoId = platoId)
    is MenuDelCarrito -> ItemPedidoDto(tipo = "menu", cantidad = cantidad, entrada = entrada, segundo = segundo)
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