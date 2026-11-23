package com.equipo.sanmarkfood.restaurante.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface FuncionesApi {
    @POST("actualizarPedido")
    suspend fun actualizarPedido(@Body accion: AccionPedidoDto): EstadoPedidoDto

    @POST("reservas")
    suspend fun reservas(@Body accion: AccionReservaDto): EstadoReservaDto

    @POST("eliminarCuenta")
    suspend fun eliminarCuenta(): CuentaEliminadaDto

    @POST("rankingPlatos")
    suspend fun rankingPlatos(@Body rango: RangoDto): RankingPlatosDto
}

@Serializable
data class RangoDto(val desde: Long, val hasta: Long)

@Serializable
data class RankingPlatosDto(val platos: List<PlatoPedidoDto>)

@Serializable
data class PlatoPedidoDto(val nombre: String, val cantidad: Int)

@Serializable
data class CuentaEliminadaDto(val eliminada: Boolean)

@Serializable
data class AccionReservaDto(
    val reservaId: String,
    val accion: String,
    val motivo: String? = null,
    val detalle: String? = null,
)

@Serializable
data class EstadoReservaDto(val estado: String)

@Serializable
data class AccionPedidoDto(
    val pedidoId: String,
    val accion: String,
    val minutos: Int? = null,
    val codigo: String? = null,
    val sinCodigo: Boolean? = null,
    val motivo: String? = null,
    val detalle: String? = null,
    val platoAgotado: String? = null,
)

@Serializable
data class EstadoPedidoDto(val estado: String)

@Serializable
data class ErrorFuncionDto(val error: String)
