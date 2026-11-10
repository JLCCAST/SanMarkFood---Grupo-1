package com.equipo.sanmarkfood.comensal.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface FuncionesApi {
    @POST("crearPedido")
    suspend fun crearPedido(@Body pedido: CrearPedidoDto): PedidoCreadoDto

    @POST("actualizarPedido")
    suspend fun actualizarPedido(@Body cambio: ActualizarPedidoDto): EstadoDto
}

@Serializable
data class ActualizarPedidoDto(
    val pedidoId: String,
    val accion: String,
)

@Serializable
data class EstadoDto(val estado: String)

@Serializable
data class CrearPedidoDto(
    val restauranteUid: String,
    val modalidad: String,
    val metodoPago: String,
    val items: List<ItemPedidoDto>,
    val direccion: DireccionDto? = null,
)

@Serializable
data class ItemPedidoDto(
    val tipo: String,
    val cantidad: Int,
    val platoId: String? = null,
    val entrada: String? = null,
    val segundo: String? = null,
)

@Serializable
data class DireccionDto(
    val texto: String,
    val latitud: Double,
    val longitud: Double,
)

@Serializable
data class PedidoCreadoDto(
    val pedidoId: String,
    val numero: Int,
)

@Serializable
data class ErrorFuncionDto(val error: String)