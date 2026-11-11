package com.equipo.sanmarkfood.comensal.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface FuncionesApi {
    @POST("crearPedido")
    suspend fun crearPedido(@Body pedido: CrearPedidoDto): PedidoCreadoDto

    @POST("actualizarPedido")
    suspend fun actualizarPedido(@Body cambio: ActualizarPedidoDto): EstadoDto

    @POST("resenas")
    suspend fun calificarPedido(@Body calificacion: CalificacionDto): PedidoCalificadoDto

    @POST("reservas")
    suspend fun crearReserva(@Body reserva: CrearReservaDto): ReservaCreadaDto

    @POST("reservas")
    suspend fun cambiarReserva(@Body cambio: CambiarReservaDto): EstadoDto
}

@Serializable
data class CrearReservaDto(
    val accion: String,
    val restauranteUid: String,
    val fecha: String,
    val hora: String,
    val personas: Int,
)

@Serializable
data class ReservaCreadaDto(val reservaId: String)

@Serializable
data class CambiarReservaDto(
    val accion: String,
    val reservaId: String,
    val fecha: String? = null,
    val hora: String? = null,
)

@Serializable
data class CalificacionDto(
    val pedidoId: String,
    val estrellas: Int,
    val comentario: String? = null,
    val platos: Map<String, Boolean>? = null,
)

@Serializable
data class PedidoCalificadoDto(val pedidoId: String)

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